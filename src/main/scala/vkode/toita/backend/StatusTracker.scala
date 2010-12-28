package vkode.toita.backend

import net.liftweb.http.CometActor
import net.liftweb.json.JsonAST.{JValue, JArray}
import net.liftweb.json.JsonParser
import java.util.concurrent.atomic.AtomicBoolean
import akka.actor.{ActorRef, Actor}
import vkode.toita.comet.DiagnosticsComet
import akka.util.Logging

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

  private var totalRepliesCount = Map[BigInt, Int]()

  private var repliesTo = Map[BigInt, List[BigInt]]()

  private def hasReplies (id: BigInt) = repliesTo contains id

  private def roots = statusMap.keys.toList diff replies.toList sortWith (_ > _)

  def statuses: List[TwitterStatusUpdate] = this.statusMap.values.toList

  def receive = {
    case StatusTracker.Boot => boot
    case TwitterStatusDelete(TOStatusRef(id, _)) =>
      statusMap = statusMap get id match {
        case None => statusMap
        case Some(tsu) => statusMap + (id -> tsu.copy(deleted = true))
      }
      updateConversation
    case TwitterFriends(TOFriends(ids)) =>
      log.info("Friends: " + ids.mkString(" "))
    case statusUpdate: TwitterStatusUpdate =>
      val id = statusUpdate.id
      if (statusMap contains id) {
        log.info("Id received again: " + id)
      } else {
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
        updateConversation
      }
  }

  private def updateConversation =
    userStream ! Conversation(roots, statusMap, repliesTo)

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
