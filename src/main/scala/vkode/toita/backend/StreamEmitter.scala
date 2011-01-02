package vkode.toita.backend

import akka.actor.{ActorRef, Actor}
import java.io.IOException
import akka.util.Logging
import java.util.concurrent.atomic.AtomicBoolean

class StreamEmitter(val twitterSession: TwitterSession, required: Class[_]*)
    extends Logging with JsonEvents {

  private val requiredClasses = required.toSet[Class[_]]

  private lazy val twitterStream = twitterSession.userStream

  private val streamStarted = new AtomicBoolean

  private var receivers: Map[Class[_], Set[ActorRef]] = Map()

  def withReceiver(ref: ActorRef, types: Class[_]*): StreamEmitter = {
    receivers = (receivers /: types) ((m, t) => {
      val set = m getOrElse (t, Set())
      m + (t -> (set + ref))
    })
    startStream
    this
  }

  def close = twitterStream.close

  private def startStream =
    if (requiredClasses.subsetOf(receivers.keySet) && streamStarted.compareAndSet(false, true))
      Actor spawn {
        message (twitterSession.homeTimeline)
        try {
          for (line <- twitterStream) message (line)
        } catch {
          case e: IOException =>
            log.info(this + " done", e)
        } finally {
          twitterStream.close
        }
      }

  private def message (line: String) = events(line) groupBy (_.getClass) foreach (_ match {
    case (eventType, events) => ship(eventType, events)
  })

  private def ship(eventType: Class[_], events: List[TwitterEvent]) {
    receivers get (eventType) match {
      case Some (receivers) => ship(receivers, events)
      case None =>
        log.warn("No receivers for " + events.size + " events of type " + eventType)
    }
  }

  private def ship(receivers: Set[ActorRef], events: List[TwitterEvent]) {
    receivers foreach (receiver => events foreach (receiver ! _))
  }
}
