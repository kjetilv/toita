package vkode.toita.backend

import net.liftweb.json.JsonAST.{JValue, JField, JArray, JNothing, JNull}
import reflect.Manifest
import java.util.Date
import net.liftweb.json._
import java.io.{File, PrintWriter, FileWriter}
import akka.util.Logging

object JsonTransformer extends Logging {

  private lazy val file = {
    val file = new File("stream.json")
    log.info("JSON file: " + file.getAbsolutePath)
    file
  }

  private lazy val doWrite = System getProperty ("writestream") equalsIgnoreCase "true"

  private lazy val writer = new PrintWriter(new FileWriter(file, true))

  private def write(json: JValue) = if (doWrite) json match {
    case JNothing =>
      writer println ("# @" + new Date + ": Nothing heard")
      writer println
    case json => try {
      writer println ("# @" + new Date)
      writer println (Printer.pretty(JsonAST.render(json)))
      writer println
    } finally {
      writer.flush
    }
  }

  private implicit val fmtz = new DefaultFormats {

    override protected def dateFormatter = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy")
  }

  def getEvent(json: JValue): Option[TwitterEvent] = {
    write(json)
    transformers.iterator find (_ match {
      case (tag, _) => json \ tag != JNothing
    }) map (_ match {
      case (_, fun) => fun (json)
    }) getOrElse None
  }

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
        val meta = extract (json, classOf[TOMeta]) get
        val retweeted = json \ "retweeted_status" match {
          case JNull => None
          case JNothing => None
          case json => getEvent(json) match {
            case tsu: TwitterStatusUpdate => Some(tsu)
            case _ => None
          }
        }
        val hashtags = entities("hashtags", json, classOf[TOHashtag])
        val mentions = entities("user_mentions", json, classOf[TOMention])
        val urls = entities("urls", json, classOf[TOURL])
        val reply = extract (json, classOf[TOReply])
        val toEntities = TOEntities(hashtags, mentions, urls)
        TwitterStatusUpdate(status, meta, user, retweeted, toEntities, reply, false, json)
      })
    }),
        "delete" -> (json => {
          extract (json \ "delete" \ "status", classOf[TOStatusRef]) map (TwitterStatusDelete (_, json))
        }),
        "friends" -> (json => {
          extract (json, classOf[TOFriends]) map (TwitterFriends (_, json))
        }),
        "screen_name" -> (json => {
          extract(json, classOf[TOUser]) map (TwitterFriend(_, json))
        }),
        "event" -> (json => {
          extract(json, classOf[TOFollowEvent]) map (TwitterFollower(_, json))
        }))
}
