package vkode.toita.backend

import org.joda.time.DateTime
import java.net.URL
import java.util.Date

trait TopLevel

case class TOFriends(friends: List[BigInt])

case class TOStatus(id: BigInt,
                    text: String,
                    truncated: Boolean,
                    source: Option[String],
                    favorited: Boolean,
                    created_at: Date
                    //                    retweeted_count: Int
                     ) extends TopLevel

case class TOStatusRef(id: BigInt, user_id: BigInt)

case class TOStatusGeo(geo: Option[String],
                       coordinates: Option[String],
                       place: Option[String])

case class TOStatusMeta(retweeted: Boolean,
                        truncated: Boolean,
                        source: String,
                        favorited: Boolean,
                        created_at: String,
                        contributors: String)

case class TOEntities(hashtags: List[TOHashtag],
                      mentions: List[TOMention],
                      urls: List[TOURL])

case class TOHashtag(indices: List[Int],
                     text: String);

case class TOURL(indices: List[Int],
                 url: String)

case class TOMention(id: BigInt,
                     name: String,
                     screen_name: String,
                     indices: List[Int])

case class TOReply(in_reply_to_status_id: BigInt,
                   in_reply_to_user_id: BigInt,
                   in_reply_to_screen_name: String)

case class TOUser(id: BigInt,
                  screen_name: String,
                  name: String,
                  lang: Option[String],
                  statuses_count: Long,
                  profile_image_url: String)

