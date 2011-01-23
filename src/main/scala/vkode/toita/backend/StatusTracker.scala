package vkode.toita.backend

import net.liftweb.http.CometActor
import akka.actor.Actor
import akka.util.Logging

class StatusTracker (userStream: CometActor, twitterService: TwitterAsynchService)
    extends Actor with Logging {

  def receive = {
    case TwitterStatusDelete(TOStatusRef(id, _), _) =>
      statusMap = statusMap get id match {
        case None => statusMap
        case Some(tsu) => statusMap + (id -> tsu.copy(deleted = true))
      }
      updateConversation
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
            twitterService status repliedTo
          }
        })
        updateConversation
      }
  }

  private var statusMap = Map[BigInt, TwitterStatusUpdate]()

  private var replies = Set[BigInt]()

  private var totalRepliesCount = Map[BigInt, Int]()

  private var repliesTo = Map[BigInt, List[BigInt]]()

  private def hasReplies (id: BigInt) = repliesTo contains id

  private def roots = statusMap.keys.toList diff replies.toList sortWith (_ > _)

  private def updateConversation = userStream ! Stream(roots, statusMap, repliesTo)
}
