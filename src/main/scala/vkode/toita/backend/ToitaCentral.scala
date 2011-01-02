package vkode.toita.backend

import scalaz.Options
import akka.actor.{ActorRef, Actor}
import vkode.toita.comet.{FolloweesComet, DiagnosticsComet, UserStreamComet}

class ToitaCentral extends Actor with Options {

  override def receive = {
    case CometUp(actor: UserStreamComet) =>
      userStreams = dismantle(userStreams, actor.session.key)
      setup(actor)
    case CometDown(actor: UserStreamComet) =>
      userStreams = dismantle(followeeses, actor.session.key)
    case CometUp(actor: FolloweesComet) =>
      followeeses = dismantle(followeeses, actor.session.key)
      setup(actor)
    case CometDown(actor: FolloweesComet) =>
      followeeses = dismantle(followeeses, actor.session.key)
    case CometUp(diagnostics: DiagnosticsComet) =>
      diagnosticians = diagnosticians :+ diagnostics
    case CometDown(diagnostics: DiagnosticsComet) =>
      diagnosticians = diagnosticians filterNot (_ == diagnostics)
    case msg: DiagnosticsComet.Timed => diagnostic(msg)
    case x =>
      log.warn("Unhandled: " + x)
  }

  def diagnostic(msg: Any) = diagnosticians foreach (_ ! msg)

  var diagnosticians = List[DiagnosticsComet]()

  var streamEmitters = Map[String,StreamEmitter]()

  var userStreams = Map[String,ActorRef]()

  var followeeses = Map[String,ActorRef]()

  def getEmitter(userStream: ToitaSessionUser) =
    streamEmitters get (userStream.session.key) getOrElse (new StreamEmitter(new TwitterSession(userStream.session), classOf[TwitterFriends]))

  private def setup (userStream: UserStreamComet) {
    val emitter = getEmitter (userStream)
    val tracker = (Actor actorOf (new StatusTracker (userStream, emitter.twitterSession, self)))
    tracker.start
    val expandedEmitter = emitter.withReceiver (tracker, classOf[TwitterStatusUpdate], classOf[TwitterStatusDelete])

    streamEmitters = streamEmitters + (userStream.session.key -> expandedEmitter)
    userStreams = userStreams + (userStream.session.key -> tracker)
  }

  private def setup (followees: FolloweesComet) {
    val emitter = getEmitter (followees)
    val tracker = (Actor actorOf (new FollowerTracker (followees, emitter.twitterSession, self)))
    tracker.start
    val expandedEmitter = emitter.withReceiver (tracker, classOf[TwitterFriends], classOf[TwitterFriend])

    streamEmitters = streamEmitters + (followees.session.key -> expandedEmitter)
    followeeses = followeeses + (followees.session.key -> tracker)
  }

  private def dismantle (m: Map[String,ActorRef], key: String) =
    m get key match {
      case Some(actor) =>
        actor.stop
        m - key
      case None => m
    }
}
