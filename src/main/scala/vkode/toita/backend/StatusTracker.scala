package vkode.toita.backend

import net.liftweb.http.CometActor
import net.liftweb.json.JsonAST.{JValue, JArray}
import net.liftweb.json.JsonParser
import java.util.concurrent.atomic.AtomicBoolean
import akka.actor.{ActorRef, Actor}
import vkode.toita.comet.DiagnosticsComet
import com.weiglewilczek.slf4s.Logging

object StatusTracker {
  case object Boot
}

class StatusTracker (userStream: CometActor, twitterSession: TwitterSession, diagnostics: ActorRef)
    extends Actor with Logging {

  private val booted = new AtomicBoolean

  private var twitterStream: Option[TwitterStream] = None

  private def boot =
    if (booted compareAndSet (false, true)) {
      Actor spawn {
        message (twitterSession.homeTimeline)
      }
      val stream = twitterSession.userStream
      this.twitterStream = Some(stream)
      Actor spawn {
        for (line <- stream) message (line)
      }
    }

  override def postStop = twitterStream foreach (_.close)

  private var statusMap = Map[BigInt, TwitterStatusUpdate]()

  private var replies = Set[BigInt]()

  private var repliesTo = Map[BigInt, List[BigInt]]()

  private def hasReplies (id: BigInt) = repliesTo contains id

  private object Noode {

    def apply(tsu: TwitterStatusUpdate): Noode = Noode(None, tsu, Nil)
  }

  private case class Noode(parent: Option[Noode], tsu: TwitterStatusUpdate, replies: List[Noode]) {

    def add(reply: TwitterStatusUpdate) = this copy (replies = Noode(Some(this), reply, Nil) :: this.replies)
  }

  def renderableStatuses: List[RenderableStatus] =
    statusMap.keys.toList.diff(replies.toList) sortWith (_ > _) flatMap (renderableStatuses(_, 0))

  private def renderableStatuses (id: BigInt, depth: Int): List[RenderableStatus] = {
    renderableStatus (id, depth) :: (repliesTo get id match {
      case Some(replies) if (!replies.isEmpty) =>
        replies flatMap (renderableStatuses (_, depth + 1))
      case _ =>
        Nil
    })
  }

  private def renderableStatus(id: BigInt, depth: Int) = RenderableStatus (statusMap(id), depth)

  def statuses: List[TwitterStatusUpdate] = this.statusMap.values.toList

  def receive = {
    case StatusTracker.Boot => boot
    case TwitterStatusDelete(TOStatusRef(id, _)) =>
      statusMap = statusMap get id match {
        case None => statusMap
        case Some(tsu) => statusMap + (id -> tsu.copy(deleted = true))
      }
    case TwitterFriends(TOFriends(ids)) =>
      logger.info("Friends: " + ids.mkString(" "))
    case statusUpdate: TwitterStatusUpdate =>
      val id = statusUpdate.id
      statusMap = statusMap + (id -> statusUpdate)
      statusUpdate.repliedTo foreach (repliedTo => {
        replies = replies + id
        repliesTo = repliesTo + (repliedTo -> (id :: (repliesTo get repliedTo getOrElse Nil)))
        if (!(statusMap contains repliedTo)) {
          Actor spawn {
            diagnostics ! DiagnosticsComet.LookupStarted
            val s = twitterSession lookup repliedTo
            diagnostics ! DiagnosticsComet.LookupEnded
            message (s)
          }
        }
      })
      println("Statuses: (replies: " + repliesTo.size + ")\n " + (renderableStatuses map (rs => rs.indent + rs.update.status.text) mkString "\n "))
      userStream ! renderableStatuses
  }

  private def message (line: String) = events(line) foreach (self ! _)

  private def events (line: String): List[TwitterEvent] =
    JsonParser parseOpt line match {
      case Some(array: JArray) =>
        array.children map (event (_)) filter (_.isDefined) map (_.get)
      case Some(json: JValue) =>
        event (json) match {
          case Some(event) => List(event)
          case None => Nil
        }
      case None =>
        Nil
    }

  private def event(json: JValue): Option[TwitterEvent] = JsonTransformer (json)
}
