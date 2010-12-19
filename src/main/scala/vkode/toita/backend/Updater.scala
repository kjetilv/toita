package vkode.toita.backend

import vkode.toita.comet.UserStream
import net.liftweb.json.JsonParser
import actors.Actor
import net.liftweb.actor.LiftActor
import net.liftweb.http.CometActor
import net.liftweb.json.JsonAST.{JArray, JValue}
import net.liftweb.common.Logger
import scalaz.Options

object Updater extends LiftActor with Options {

  private val log = Logger(getClass)

  import scala.collection.mutable.Map

  val sessions: Map[String, TwitterSession] = Map[String, TwitterSession]()

  private def cleanup(session: Option[TwitterSession]) = session map (_.close)

  protected def messageHandler = {
    case UserStreamUp(actor) => {
      val session = TwitterSession(actor.session)
      cleanup (sessions put (actor.session.key, session))
      brief (actor, session)
      runActor (actor, session)
    }
    case UserStreamDown(actor) =>
      cleanup (sessions remove actor.session.key)
    case x =>
      error("Unhandled event: " + x)
  }

  def homeTimeline(session: TwitterSession): String =
    session lookup "http://api.twitter.com/1/statuses/home_timeline.json?count=10&include_entities=true"

  private def brief (actor: UserStream, session: TwitterSession) = handle (actor, homeTimeline(session))

  def runActor (userStream: UserStream, session: TwitterSession) =
    Actor actor {
      for (line <- session stream "https://userstream.twitter.com/2/user.json") handle (userStream, line)
    }

  private def handle (actor: UserStream, line: String) {
    println("Recv: " + line)
    actor ! events (line)
  }

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
        println("Not a JSON input: " + line)
        Nil
    }

  private def event(json: JValue): Option[TwitterEvent] = json match {
    case json: JValue =>
      println("Json: " + json)
      val event = JsonTransformer (json)
      if (event.isEmpty) {
        println("No JSON parsed: " + event)
      }
      event
    case x =>
      println ("Unsupported structure: " + x)
      None
  }
}
