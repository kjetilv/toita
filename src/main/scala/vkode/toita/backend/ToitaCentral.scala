package vkode.toita.backend

import scalaz.Options
import akka.actor.{ActorRef, Actor}
import Actor._
import scala.collection.mutable.{Map => MutMap}
import vkode.toita.comet.DiagnosticsComet

class ToitaCentral extends Actor with Options {

  implicit def trackerToKey (trackable: ToitaTrackable) = trackable.session → trackable.eventTypes

  override def receive = {
    case msg: DiagnosticsComet.DiagnosticsEvent => diagnostic(msg)
    case CometUp(diagnostics: DiagnosticsComet) =>
      diagnosticians = diagnosticians :+ diagnostics
    case CometDown(diagnostics: DiagnosticsComet) =>
      diagnosticians = diagnosticians filterNot (_ == diagnostics)
    case CometUp(cometActor: ToitaTrackable) =>
      setup(cometActor)
    case CometDown(cometActor: ToitaTrackable) =>
      dismantle(cometActor)
    case x =>
      log.warn("Unhandled: " + x)
  }

  def diagnostic(msg: Any) = diagnosticians foreach (_ ! msg)

  var diagnosticians = List[DiagnosticsComet]()

  val trackerRefs = MutMap[(UserSession, List[Class[_ <: TwitterEvent]]),ActorRef]()

  private def setup (trackable: ToitaTrackable) =
    trackerRefFor(trackable) foreach (_ ! Tracker.Add(trackable.cometActor))

  private def trackerRefFor(trackable: ToitaTrackable): Option[ActorRef] =
    trackerRefs get trackable match {
      case None =>
        newTrackerRef(trackable, getEmitter(trackable)) match {
          case None =>
            log.warn("No tracker could be constructed for " + trackable)
            None
          case newTrackerRef =>
            trackerRefs (trackable) = newTrackerRef.get
            newTrackerRef
        }
      case trackerRef => trackerRef
    }

  private def getEmitter(trackable: ToitaTrackable) = StreamEmitter(trackable.session)

  private def newTrackerRef(trackable: ToitaTrackable, emitter: StreamEmitter): Option[ActorRef] = {
    trackable.tracker(emitter) match {
      case Some(trackerRef) =>
        trackerRef.start
        emitter addReceiver(trackerRef, trackable.eventTypes)
        log.info("Tracker for " + trackable + " started: " + trackerRef +
                 ", events of " + trackable.eventTypes.mkString(","))
        Some(trackerRef)
      case None =>
        log.warn("No tracker for " + trackable);
        None
    }
  }

  private def dismantle (trackable: ToitaTrackable) =
    trackerRefs get trackable match {
      case Some(trackerRef) =>
        log.info("Dismantling tracker for " + trackable + ": " + trackerRef)
        trackerRefs -= trackable
        trackerRef ! Tracker.Remove(trackable.cometActor)
        trackerRef.stop
      case None =>
        log.warn("No tracker for " + trackable + " was registered");
    }
}
