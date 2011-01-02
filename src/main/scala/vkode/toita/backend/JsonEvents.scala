package vkode.toita.backend

import net.liftweb.json.JsonAST.{JValue, JArray, JNull, JNothing}
import net.liftweb.json.JsonParser

trait JsonEvents {

  protected def foreachEvent (line: String) (fun: TwitterEvent => Unit) = events (line) foreach (fun (_))

  protected def events (line: String): List[TwitterEvent] =
    JsonParser parseOpt line match {
      case Some(array: JArray) =>
        array.children map (event (_)) filter (_.isDefined) map (_.get)
      case Some(json: JValue) =>
        event (json) match {
          case Some(event) => List(event)
          case None => Nil
        }
      case None =>
        Nil
    }

  private def event(json: JValue): Option[TwitterEvent] = json match {
    case JNull => None
    case JNothing => None
    case json => JsonTransformer.getEvent(json)
  }
}
