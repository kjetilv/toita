package vkode.toita.backend

import net.liftweb.json.JsonAST.{JValue, JField, JArray, JNothing, JString, JInt, JBool, JNull}
import net.liftweb.json.{Extraction, DefaultFormats, DateFormat}
import reflect.Manifest
import java.util.Date
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime

object JsonTransformer {
  //  def fields(fields: List[JField]): Map[String, Any] =
  //  private implicit val formats = net.liftweb.json.DefaultFormats

  private implicit val fmtz = new DefaultFormats {
    override val dateFormat = new DateFormat {
      private val f = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy")
      def parse(s: String) =
        try {
          Option(f parse s)
        } catch {
          case _ => None
        }
      def format(d: Date) = f format d
    }
  }

  def apply(json: JValue): Option[TwitterEvent] = transformers.iterator find (_ match {
    case (tag, _) => json \ tag != JNothing
  }) map (_ match {
    case (_, fun) => fun (json)
  }) getOrElse None

  def entities[T](name: String, jo: JValue, mt: Class[T]): List[T] = {
    implicit val imf = Manifest classType mt
    val options: List[Option[T]] = jo \ "entities" \ name match {
      case JField(_, JArray(objects)) =>
        objects map (Extraction extractOpt _)
      case x => Nil
    }
    options filter (_ isDefined) map (_ get)
  }

  private def extract[T](json: JValue, tp: Class[T]): Option[T] = {
    implicit val mf = Manifest classType tp
    Extraction extractOpt json
  }

  private lazy val transformers: Map[String, JValue => Option[TwitterEvent]] =
    Map("text" -> (json => {
      extract (json, classOf[TOStatus]) map (status => {
        val user = extract (json \ "user", classOf[TOUser])
        val hashtags = entities("hashtags", json, classOf[TOHashtag])
        val mentions = entities("user_mentions", json, classOf[TOMention])
        val urls = entities("urls", json, classOf[TOURL])
        val reply = extract (json, classOf[TOReply])
        TwitterStatusUpdate(status, user, TOEntities(hashtags, mentions, urls), reply)
      })
    }),
      "delete" -> (json => {
        extract (json \ "delete" \ "status", classOf[TOStatusRef]) map (TwitterStatusDelete (_))
      }),
      "friends" -> (json => {
        extract (json, classOf[TOFriends]) map (TwitterFriends (_))
      }))
}
