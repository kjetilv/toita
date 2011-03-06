package vkode.toita.events

import net.liftweb.json.JsonAST.JValue

case class TwitterStatusUpdate(status: TOStatus,
                               meta: TOMeta,
                               user: TOUser,
                               retweeted: Option[TwitterStatusUpdate],
                               entities: TOEntities,
                               reply: Option[TOReply],
                               deleted: Boolean,
                               override val authenticatedUser: String,
                               override val json: JValue)
    extends TwitterEvent with Treeable {

  def name = user.data.screen_name

  def timestamp = meta.created_at.getTime

  def id = status.id

  def repliedTo = Option(reply map (_.in_reply_to_status_id) getOrElse null)
}











