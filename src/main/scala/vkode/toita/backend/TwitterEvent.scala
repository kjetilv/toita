package vkode.toita.backend

import org.joda.time.DateTime
import net.liftweb.json.JsonAST.JValue

abstract sealed class TwitterEvent {

  val time: DateTime = new DateTime

  val json: JValue
}

case class TwitterFriends(friends: TOFriends,
                          override val json: JValue) extends TwitterEvent

case class TwitterFriend(friend: TOUser,
                         override val json: JValue) extends TwitterEvent

case class TwitterFollower(event: TOFollowEvent,
                           override val json: JValue) extends TwitterEvent

case class TwitterStatusDelete(status: TOStatusRef,
                               override val json: JValue) extends TwitterEvent

case class TwitterStatusUpdate(status: TOStatus,
                               meta: TOMeta,
                               user: Option[TOUser],
                               retweeted: Option[TwitterStatusUpdate],
                               entities: TOEntities,
                               reply: Option[TOReply],
                               deleted: Boolean,
                               override val json: JValue)
    extends TwitterEvent with Treeable {

  def name = user map (_.screen_name) getOrElse "anonymous"

  def timestamp = meta.created_at.getTime

  def id = status.id

  def repliedTo = Option(reply map (_.in_reply_to_status_id) getOrElse null)
}

