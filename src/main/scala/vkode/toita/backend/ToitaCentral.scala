package vkode.toita.backend

import scalaz.Options
import akka.actor.{ActorRef, Actor}
import Actor._
import scala.collection.mutable.Map
import vkode.toita.comet.{PeopleComet, UserStream, DiagnosticsComet}

class ToitaCentral extends Actor with Options {

  override def receive = {
    case CometUp(actor: UserStream) =>
      setup(actor)
    case CometDown(actor: UserStream) =>
      dismantle(statusTrackers, actor.session)
    case CometUp(actor: PeopleComet) =>
      setup(actor)
    case CometDown(actor: PeopleComet) =>
      dismantle(peopleTrackers, actor.session)
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

  val peopleTrackers = Map[UserSession,ActorRef]()

  private def setup (userStream: UserStream) = statusTracker(userStream) ! Tracker.Add(userStream)

  private def statusTracker(userStream: UserStream) =
    statusTrackers getOrElseUpdate (userStream.session, newStatusTracker(getEmitter(userStream)))

  private def setup (people: PeopleComet) = peopleTracker(people) ! Tracker.Add(people)

  private def peopleTracker(people: PeopleComet) =
    followerTrackers getOrElseUpdate (people.session, newPeopleTracker(getEmitter(people)))

  private def getEmitter(sessionUser: ToitaSessionUser) = StreamEmitter(sessionUser.session)

  private def newStatusTracker(emitter: StreamEmitter) = {
    val statusTrackerRef = actorOf (new StatusTracker(emitter)).start
    emitter addReceiver(statusTrackerRef, classOf[TwitterStatusUpdate], classOf[TwitterStatusDelete])
    statusTrackerRef
  }

  private def newPeopleTracker(emitter: StreamEmitter) = {
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
