package vkode.toita.backend

import scalaz.Options
import akka.actor.{ActorRef, Actor}
import Actor._
import scala.collection.mutable.Map
import vkode.toita.comet.{UserStream, FollowedComet, DiagnosticsComet}

class ToitaCentral extends Actor with Options {

  override def receive = {
    case CometUp(actor: UserStream) =>
      setup(actor)
    case CometDown(actor: UserStream) =>
      dismantle(statusTrackers, actor.session)
    case CometUp(actor: FollowedComet) =>
      setup(actor)
    case CometDown(actor: FollowedComet) =>
      dismantle(followerTrackers, actor.session)
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

  val statusTrackers = Map[UserSession,ActorRef]()

  val followerTrackers = Map[UserSession,ActorRef]()

  private def setup (userStream: UserStream) = statusTracker(userStream) ! Tracker.Add(userStream)

  private def setup (followees: FollowedComet) = followerTracker(followees) ! Tracker.Add(followees)

  private def statusTracker(userStream: UserStream) =
    statusTrackers getOrElseUpdate (userStream.session, newStatusTracker(getEmitter(userStream)))

  private def followerTracker(followees: FollowedComet) =
    followerTrackers getOrElseUpdate (followees.session, newFollowerTracker(getEmitter(followees)))

  private def getEmitter(sessionUser: ToitaSessionUser) = StreamEmitter(sessionUser.session)

  private def newStatusTracker(emitter: StreamEmitter) = {
    val statusTrackerRef = actorOf (new StatusTracker(emitter)).start
    emitter addReceiver(statusTrackerRef, classOf[TwitterStatusUpdate], classOf[TwitterStatusDelete])
    statusTrackerRef
  }

  private def newFollowerTracker(emitter: StreamEmitter) = {
    val followerTrackerRef = actorOf (new FollowerTracker (emitter)).start
    emitter addReceiver (followerTrackerRef, classOf[TwitterFriends], classOf[TwitterFriend])
    followerTrackerRef
  }

  private def dismantle (m: Map[UserSession,ActorRef], key: UserSession) =
    m get key match {
      case Some(actor) =>
        actor.stop
        m - key
      case None => m
    }
}
