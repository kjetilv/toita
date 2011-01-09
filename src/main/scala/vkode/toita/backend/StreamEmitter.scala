package vkode.toita.backend

import akka.actor.{ActorRef, Actor}
import java.io.IOException
import akka.util.Logging
import java.util.concurrent.atomic.AtomicBoolean
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST.{JNothing, JNull, JArray, JValue}

object StreamEmitter {

  import scala.collection.mutable._

  val streamEmitters: Map[UserSession,StreamEmitter] =
    new HashMap[UserSession,StreamEmitter] with SynchronizedMap[UserSession,StreamEmitter]

  def apply(session: UserSession) = streamEmitters getOrElseUpdate (session, new StreamEmitter (session))
}

class StreamEmitter(userSession: UserSession, required: Class[_]*)
    extends Logging with TwitterAsynchService {

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

  private def foreachEvent (line: String) (fun: TwitterEvent => Unit) = events (line) foreach (fun (_))

  private def events (line: String): List[TwitterEvent] =
    JsonParser parseOpt line match {
      case Some(array: JArray) => array.children flatMap (event (_))
      case Some(json: JValue) => event (json)
      case None => Nil
    }

  private def user(tsu: TwitterStatusUpdate, json: JValue): List[TwitterEvent] =
    tsu.user map (user => List(TwitterFriend(user, json))) getOrElse Nil

  private def retweetedUser(tsu: TwitterStatusUpdate, json: JValue): List[TwitterEvent] =
    tsu.retweeted map (tsu => user(tsu, json)) getOrElse Nil

  private def event(json: JValue): List[TwitterEvent] = json match {
    case JNull => Nil
    case JNothing => Nil
    case json => {
      JsonTransformer getEvent json match {
        case None => Nil
        case Some(event) =>
          List(event) ++ (if (event.isInstanceOf[TwitterStatusUpdate]) {
            val tsu = event.asInstanceOf[TwitterStatusUpdate]
            user(tsu, json) ++ retweetedUser (tsu, json)
          } else {
            Nil
          })
      }
    }
  }
}
