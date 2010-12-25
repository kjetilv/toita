package vkode.toita.backend

import akka.actor.Actor
import vkode.toita.comet.UserStream

class StatusTracker (userStream: UserStream) extends Actor {

  private var statusMap = Map[BigInt, TwitterStatusUpdate]()

  private var origins = List[BigInt]()

  private var repliedTo = Set[BigInt]()

  private var repliesTo = Map[BigInt, List[BigInt]]()

  def receive = {
    case statusUpdate: TwitterStatusUpdate =>
      val id = statusUpdate.id
      statusUpdate.replyRef match {
        case None =>
          origins = origins :+ id
        case Some(reply) =>
          repliedTo = repliedTo + id
          repliesTo = repliesTo + (id -> (reply :: (repliesTo get id getOrElse Nil))) + (reply -> Nil)
      }
      statusMap = statusMap + (id -> statusUpdate)
      userStream ! statuses
    case twitterFriends: TwitterFriends =>

  }

  def statuses: List[TwitterStatusUpdate] = this.statusMap.values.toList
}
