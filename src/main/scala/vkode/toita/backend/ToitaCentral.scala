package vkode.toita.backend

import scalaz.Options
import akka.actor.{ActorRef, Actor}
import Actor._
import scala.collection.mutable.Map
import vkode.toita.comet.{UserStreamComet, PeopleComet, DiagnosticsComet}

class ToitaCentral extends Actor with Options {

  type EventTypes = List[Class[_ <: TwitterEvent]]

  override def receive = {
    case CometUp(actor: UserStreamComet, eventTypes) =>
      setup(actor, eventTypes)
    case CometDown(actor: UserStreamComet) =>
      dismantle(statusTrackers, actor.session)
    case CometUp(actor: PeopleComet, eventTypes) =>
      setup(actor)
    case CometDown(actor: PeopleComet) =>
      dismantle(peopleTrackers, actor.session)
    case CometUp(diagnostics: DiagnosticsComet, eventTypes) =>
      diagnosticians = diagnosticians :+ diagnostics
    case CometDown(diagnostics: DiagnosticsComet) =>
      diagnosticians = diagnosticians filterNot (_ == diagnostics)
    case msg: DiagnosticsComet.Timed => diagnostic(msg)
    case x =>
      log.warn("Unhandled: " + x)
  }

  def diagnostic(msg: Any) = diagnosticians foreach (_ ! msg)

  var diagnosticians = List[DiagnosticsComet]()

  val trackers = Map[UserSession,ActorRef]()

  val statusTrackers = Map[UserSession,ActorRef]()

  val peopleTrackers = Map[UserSession,ActorRef]()

  private def setup (userStream: UserStreamComet, eventTypes: EventTypes) =
    statusTracker(userStream) ! Tracker.Add(userStream)

  private def setup (people: PeopleComet) = peopleTracker(people) ! Tracker.Add(people)

  private def statusTracker(userStream: UserStreamComet) =
    statusTrackers getOrElseUpdate (userStream.session, newStatusTracker(getEmitter(userStream)))

  private def peopleTracker(people: PeopleComet) =
    peopleTrackers getOrElseUpdate (people.session, newPeopleTracker(getEmitter(people)))

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
