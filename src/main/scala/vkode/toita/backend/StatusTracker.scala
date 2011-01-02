package vkode.toita.backend

import net.liftweb.http.CometActor
import java.util.concurrent.atomic.AtomicBoolean
import akka.actor.{ActorRef, Actor}
import vkode.toita.comet.DiagnosticsComet
import akka.util.Logging

object StatusTracker {
  case object Boot
}

class StatusTracker (userStream: CometActor, twitterSession: TwitterSession, diagnostics: ActorRef)
    extends Actor with Logging with JsonEvents {

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

  private val booted = new AtomicBoolean

  private def boot = message (twitterSession.homeTimeline)

  private var statusMap = Map[BigInt, TwitterStatusUpdate]()

  private var replies = Set[BigInt]()

  private var totalRepliesCount = Map[BigInt, Int]()

  private var repliesTo = Map[BigInt, List[BigInt]]()

  private def hasReplies (id: BigInt) = repliesTo contains id

  private def roots = statusMap.keys.toList diff replies.toList sortWith (_ > _)

  private def message(line: String): Unit = {
    foreachEvent(line) {
      self ! _
    }
  }

  private def updateConversation = userStream ! Conversation(roots, statusMap, repliesTo)
}
