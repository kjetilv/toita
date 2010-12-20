package vkode.toita.backend

import vkode.toita.comet.UserStream
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST.{JArray, JValue}
import scalaz.Options
import akka.actor.Actor

class Updater extends Actor with Options {

  import scala.collection.mutable.Map

  val sessions: Map[String, TwitterSession] = Map[String, TwitterSession]()

  private def cleanup(session: Option[TwitterSession]) = session map (_.close)

  override def receive = {
    case UserStreamUp(actor) => {
      val session = TwitterSession(actor.session)
      cleanup (sessions put (actor.session.key, session))
      brief (actor, session)
      runActor (actor, session)
    }
    case UserStreamDown(actor) =>
      cleanup (sessions remove actor.session.key)
  }

  def homeTimeline(session: TwitterSession): String =
    session lookup "http://api.twitter.com/1/statuses/home_timeline.json?count=10&include_entities=true"

  private def brief (actor: UserStream, session: TwitterSession) = handle (actor, homeTimeline(session))

  def runActor (userStream: UserStream, session: TwitterSession) = Actor spawn {
    for (line <- session stream "https://userstream.twitter.com/2/user.json") handle (userStream, line)
  }

  private def handle (actor: UserStream, line: String) = events(line) foreach (actor ! _)

  private def events (line: String): List[TwitterEvent] =
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

  private def event(json: JValue): Option[TwitterEvent] = JsonTransformer (json)
}
