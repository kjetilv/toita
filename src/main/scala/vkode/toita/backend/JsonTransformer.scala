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

  private lazy val doWrite = System getProperty ("writestream", "false") equalsIgnoreCase "true"

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

  def entities[T](name: String, jo: JValue)(implicit m: Manifest[T]): List[T] =
    ((jo \ "entities" \ name) match {
      case JField(_, JArray(objects)) => objects map (_ extractOpt)
      case x => Nil
    }) filter (_ isDefined) map (_ get)

  private val nn = UserData(0, "unknown", "N/A", Some("N/A"))

  private val nnDeco = UserDeco(None, false, None, Some("1C351D"), Some("005500"), Some("A0C5C7"), Some("000000"))

  private def parseStatus(json: JValue): Option[TwitterStatusUpdate] = {
    json.extractOpt[TOStatus] map (status => {
      val dec = (json \ "user").extractOpt[UserDeco] getOrElse nnDeco
      val data = (json \ "user").extractOpt[UserData] getOrElse nn
      val user = TOUser(data, dec)
      val meta = json.extract[TOMeta]
      val retweeted = json \ "retweeted_status" match {
        case json: JValue => parseStatus(json)
        case _ => None
      }
      val hashtags = entities[TOHashtag]("hashtags", json)
      val mentions = entities[TOMention]("user_mentions", json)
      val urls = entities[TOURL]("urls", json)
      val reply = json.extractOpt[TOReply]
      val toEntities = TOEntities(hashtags, mentions, urls)

      TwitterStatusUpdate(status,
                          meta,
                          user,
                          retweeted,
                          toEntities,
                          reply, false, json)
    })
  }

  private lazy val transformers: Map[String, JValue => Option[TwitterEvent]] =
    Map("text" -> (json => parseStatus(json)),
        "delete" -> (json => (json \ "delete" \ "status").extractOpt[TOStatusRef] map (TwitterStatusDelete (_, json))),
        "friends" -> (json => json.extractOpt[TOFriends] map (TwitterFriends (_, json))),
        "screen_name" -> (json => json.extractOpt[TOUser] map (TwitterFriend(_, json))),
        "event" -> (json => json.extractOpt[TOFollowEvent] map (TwitterFollower(_, json))))
}
