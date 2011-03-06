package vkode.toita.backend

import akka.actor.{ActorRef, Actor}
import java.io.IOException
import akka.util.Logging
import java.util.concurrent.atomic.AtomicBoolean
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST.{JNothing, JNull, JArray, JValue}
import reflect.{ClassManifest, Manifest}
import vkode.toita.events.{TwitterEvent, TwitterFriend, TwitterStatusUpdate}
import vkode.toita.waka.UserSession

object StreamEmitter {

  import scala.collection.mutable._

  val streamEmitters: Map[UserSession,StreamEmitter] =
    new HashMap[UserSession,StreamEmitter] with SynchronizedMap[UserSession,StreamEmitter]

  def apply(session: UserSession) = streamEmitters getOrElseUpdate (session, new StreamEmitter (session))
}

class StreamEmitter(userSession: UserSession, required: Class[_ <: TwitterEvent]*)
    extends Logging with TwitterService {

  def userName = userSession.user getOrElse ""

  def user = events (twitterSession.latestUserTimeline) filter (_.isInstanceOf[TwitterStatusUpdate]) match {
    case (tweet: TwitterStatusUpdate) :: _ => Some(tweet.user)
    case _ => None
  }

  def homeTimeline = message(twitterSession.homeTimeline)

  def users(ids: List[BigInt]) = ids.sliding(25, 25) foreach (window => { 
    message(twitterSession getFriends window) 
  })

  def status(id: BigInt) = message(twitterSession lookup id)

  def addReceiver(ref: ActorRef, types: List[Class[_ <: TwitterEvent]]) {
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

  private val requiredClasses = required.toSet[Class[_ <: TwitterEvent]]

  private lazy val twitterStream = twitterSession.userStream

  private val streamStarted = new AtomicBoolean

  private var receivers: Map[Class[_ <: TwitterEvent], Set[ActorRef]] = Map()

  private def shouldStartStream =
    !streamStarted.get && requiredClasses.subsetOf(receivers.keySet) && streamStarted.compareAndSet(false, true)

  private def feedFromStream {
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

  private def startStream =
    new Thread(new Runnable {
      def run = feedFromStream
    }, "Emitter for " + twitterSession).start

  private def message (line: String) = events(line) groupBy (_.getClass) foreach (_ match {
    case (eventType, events) => ship(eventType asSubclass classOf[TwitterEvent], events)
  })

  private def ship(eventType: Class[_ <: TwitterEvent], events: List[TwitterEvent]) {
    receivers get (eventType) match {
      case Some (receivers) =>
        ship(receivers, events)
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

  private def user(tsu: TwitterStatusUpdate, json: JValue): List[TwitterFriend] = List(TwitterFriend(tsu.user, userSession.user.get, json))

  private def retweetedUser(tsu: TwitterStatusUpdate, json: JValue): List[TwitterFriend] =
    tsu.retweeted map (tsu => user(tsu, json)) getOrElse Nil

  private def event(json: JValue): List[TwitterEvent] = json match {
    case JNull => Nil
    case JNothing => Nil
    case json => {
      JsonTransformer getEvent (userSession.user.get, json) match {
        case None =>
          Nil
        case Some(event) =>
          List(event) ++ (if (event.isInstanceOf[TwitterStatusUpdate]) bothEvents(event, json) else Nil)
      }
    }
  }

  def bothEvents[T <: TwitterEvent](event: TwitterEvent, json: JValue): List[TwitterEvent] = {
    val tsu = event.asInstanceOf[TwitterStatusUpdate]
    user(tsu, json) ++ retweetedUser(tsu, json)
  }
}
