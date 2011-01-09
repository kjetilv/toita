package vkode.toita.backend

import scalaz.Options
import akka.actor.{ActorRef, Actor}
import vkode.toita.comet.{FollowedComet, DiagnosticsComet, UserStreamComet}

class ToitaCentral extends Actor with Options {

  override def receive = {
    case CometUp(actor: UserStreamComet) =>
      userStreams = dismantle(userStreams, actor.session)
      setup(actor)
    case CometDown(actor: UserStreamComet) =>
      userStreams = dismantle(followeeses, actor.session)
    case CometUp(actor: FollowedComet) =>
      followeeses = dismantle(followeeses, actor.session)
      setup(actor)
    case CometDown(actor: FollowedComet) =>
      followeeses = dismantle(followeeses, actor.session)
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

  var userStreams = Map[UserSession,ActorRef]()

  var followeeses = Map[UserSession,ActorRef]()

  def getEmitter(sessionUser: ToitaSessionUser) = StreamEmitter(sessionUser.session)

  private def setup (userStream: UserStreamComet) {
    val emitter = getEmitter (userStream)
    val tracker = (Actor actorOf (new StatusTracker (userStream, emitter)))
    emitter.addReceiver (tracker, classOf[TwitterStatusUpdate], classOf[TwitterStatusDelete])
    tracker.start
    userStreams = userStreams + (userStream.session -> tracker)
  }

  private def setup (followees: FollowedComet) {
    val emitter = getEmitter (followees)
    val tracker = (Actor actorOf (new FollowerTracker (followees, emitter)))
    emitter.addReceiver (tracker, classOf[TwitterFriends], classOf[TwitterFriend])
    tracker.start
    followeeses = followeeses + (followees.session -> tracker)
  }

  private def dismantle (m: Map[UserSession,ActorRef], key: UserSession) =
    m get key match {
      case Some(actor) =>
        actor.stop
        m - key
      case None => m
    }
}
