package vkode.toita.backend

import vkode.toita.comet.UserStream
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST.{JArray, JValue}
import scalaz.Options
import akka.actor.{ActorRef, Actor}

class TwitterEventSource extends Actor with Options {

  override def receive = {
    case UserStreamUp(actor) =>
      dismantle(actor.session.key)
      setup(actor)
    case UserStreamDown(actor) =>
      dismantle(actor.session.key)
  }

  var sessions = Map[String,(ActorRef,TwitterStream)]()

  private def dismantle (key: String) =
    sessions = sessions get key match {
      case Some((_, stream)) =>
        stream.close
        sessions - key
      case None => sessions
    }

  private def setup (userStream: UserStream) {
    val session = TwitterSession(userStream.session)

    val key = userStream.session.key
    val stream = session stream "https://userstream.twitter.com/2/user.json"
    val tracker = (Actor actorOf (new StatusTracker (userStream))).start

    sessions = sessions + (key -> (tracker -> stream))

    message (tracker, homeTimeline(session))
    Actor spawn {
      for (line <- stream.stream) message (tracker, line)
    }
  }

  private def homeTimeline (session: TwitterSession): String =
    session lookup "http://api.twitter.com/1/statuses/home_timeline.json?count=10&include_entities=true"

  private def message (actor: ActorRef, line: String) = events(line) foreach (actor ! _)

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
