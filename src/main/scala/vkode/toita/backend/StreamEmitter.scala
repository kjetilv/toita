package vkode.toita.backend

import akka.actor.{ActorRef, Actor}
import java.io.IOException
import akka.util.Logging
import java.util.concurrent.atomic.AtomicBoolean

class StreamEmitter(userSession: UserSession, required: Class[_]*)
    extends Logging with JsonEvents with TwitterService {

  def homeTimeline = message(twitterSession.homeTimeline)

  def users(ids: List[BigInt]) = ids.sliding(25, 25) foreach (window => { message(twitterSession getFriends window) })

  def status(id: BigInt) = message(twitterSession lookup id)

  def addReceiver(ref: ActorRef, types: Class[_]*) {
    receivers = (receivers /: types) ((m, t) => {
      val set = m getOrElse (t, Set())
      m + (t -> (set + ref))
    })
    if (shouldStartStream) {
      startStream
    }
  }

  def close = twitterStream.close

  private val twitterSession= new TwitterSession(userSession)

  private val requiredClasses = required.toSet[Class[_]]

  private lazy val twitterStream = twitterSession.userStream

  private val streamStarted = new AtomicBoolean

  private var receivers: Map[Class[_], Set[ActorRef]] = Map()

  private def shouldStartStream =
      !streamStarted.get && requiredClasses.subsetOf(receivers.keySet) && streamStarted.compareAndSet(false, true)

  private def startStream =
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
