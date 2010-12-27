package vkode.toita.backend

import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST.{JArray, JValue}
import scalaz.Options
import akka.actor.{ActorRef, Actor}
import vkode.toita.comet.{DiagnosticsComet, UserStreamComet}

class ToitaCentral extends Actor with Options {

  override def receive = {
    case CometUp(actor: UserStreamComet) =>
      dismantle(actor.session.key)
      setup(actor)
    case CometDown(actor: UserStreamComet) =>
      dismantle(actor.session.key)
    case CometUp(diagnostics: DiagnosticsComet) =>
      diagnosticians = diagnosticians :+ diagnostics
    case CometDown(diagnostics: DiagnosticsComet) =>
      diagnosticians = diagnosticians filterNot (_ == diagnostics)
    case DiagnosticsComet.StreamUp => diagnostic(DiagnosticsComet.StreamUp)
    case DiagnosticsComet.StreamDown => diagnostic(DiagnosticsComet.StreamDown)
  }

  def diagnostic(msg: Any) = diagnosticians foreach (_ ! msg)

  var diagnosticians = List[DiagnosticsComet]()

  var sessions = Map[String,ActorRef]()

  private def dismantle (key: String) =
    sessions = sessions get key match {
      case Some(actor) =>
        actor.stop
        diagnostic(DiagnosticsComet.StreamDown)
        sessions - key
      case None => sessions
    }

  private def setup (userStream: UserStreamComet) {
    val twitterSession = new TwitterSession(userStream.session)
    val tracker = (Actor actorOf (new StatusTracker (userStream, twitterSession, self))).start
    tracker ! StatusTracker.Boot

    diagnostic(DiagnosticsComet.StreamUp)
    sessions = sessions + (userStream.session.key -> tracker)
  }
}
