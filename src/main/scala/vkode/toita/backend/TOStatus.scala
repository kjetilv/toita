package vkode.toita.backend

import java.util.Date

trait TopLevel

case class TOFriends(friends: List[BigInt])

case class TOStatus(id: BigInt, text: String)
    extends TopLevel

case class TOStatusRef(id: BigInt, user_id: BigInt)

case class TOStatusGeo(geo: Option[String],
                       coordinates: Option[String],
                       place: Option[String])

object TOMeta {

  val Null = TOMeta(false, None, false, "Unknown Source", false, new Date(0), "")
}

case class TOMeta(retweeted: Boolean,
                  retweeted_count: Option[BigInt],
                  truncated: Boolean,
                  source: String,
                  favorited: Boolean,
                  created_at: Date,
                  contributors: String)

case class TOEntities(hashtags: List[TOHashtag],
                      mentions: List[TOMention],
                      urls: List[TOURL])

trait Indexed {
  val indices: List[Int]
  val text: String

  def a = indices(0)
  def b = indices(1)

  def sameIndexAs (idx: Indexed) = a == idx.a && b == idx.b

  def before(indexed: Indexed) = a < indexed.b
}

case class TOHashtag(indices: List[Int],
                     text: String) extends Indexed

case class TOURL(indices: List[Int],
                 url: String) extends Indexed {
  val text = url
}

case class TOMention(id: BigInt,
                     name: String,
                     screen_name: String,
                     indices: List[Int]) extends Indexed {
  val text = screen_name
}

case class TOReply(in_reply_to_status_id: BigInt,
                   in_reply_to_user_id: BigInt,
                   in_reply_to_screen_name: String)

case class TOUser(id: BigInt,
                  screen_name: String,
                  name: String,
                  lang: Option[String],
                  statuses_count: Long,
                  profile_image_url: String)

