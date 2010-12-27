package vkode.toita.backend

import org.joda.time.DateTime

abstract sealed class TwitterEvent(time: DateTime = new DateTime)

case class TwitterLookup(string: String)

case class TwitterStatusLookup(id: BigInt)

case class TwitterFriends(friends: TOFriends) extends TwitterEvent

case class TwitterStatusDelete(status: TOStatusRef) extends TwitterEvent

case class TwitterStatusUpdate(status: TOStatus,
                               meta: Option[TOMeta],
                               user: Option[TOUser],
                               entities: TOEntities,
                               reply: Option[TOReply],
                               deleted: Boolean = false)
  extends TwitterEvent {

  def timestamp = status.created_at.getTime

  def id = status.id

  def repliedTo = Option(reply map (_.in_reply_to_status_id) getOrElse null)
}

