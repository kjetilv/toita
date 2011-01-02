package vkode.toita.backend

import net.liftweb.json.JsonAST.{JValue, JArray, JNull, JNothing}
import net.liftweb.json.JsonParser

trait JsonEvents {

  protected def foreachEvent (line: String) (fun: TwitterEvent => Unit) = events (line) foreach (fun (_))

  protected def events (line: String): List[TwitterEvent] =
    JsonParser parseOpt line match {
      case Some(array: JArray) => array.children flatMap (event (_))
      case Some(json: JValue) => event (json)
      case None => Nil
    }

  private def user(tsu: TwitterStatusUpdate): List[TwitterEvent] =
    tsu.user map (user => List(TwitterFriend(user))) getOrElse Nil

  private def retweetedUser(tsu: TwitterStatusUpdate): List[TwitterEvent] =
    tsu.retweeted map (tsu => user(tsu)) getOrElse Nil

  private def event(json: JValue): List[TwitterEvent] = json match {
    case JNull => Nil
    case JNothing => Nil
    case json => {
      JsonTransformer getEvent json match {
        case None => Nil
        case Some(event) =>
          List(event) ++ (if (event.isInstanceOf[TwitterStatusUpdate]) {
            val tsu = event.asInstanceOf[TwitterStatusUpdate]
            user(tsu) ++ retweetedUser (tsu)
          } else {
            Nil
          })
      }
    }
  }
}
