package vkode.toita.backend

import net.liftweb.http.CometActor
import akka.util.Logging
import akka.actor.Actor

object Tracker {

  trait TrackerControl

  case class Add(ca: CometActor) extends TrackerControl

  case class Remove(ca: CometActor) extends TrackerControl
}

trait Tracker extends Logging with Actor {

  import Tracker._

  val twitterService: TwitterAsynchService

  private var cometActors: List[CometActor] = Nil

  protected final def send(msg: Any) =
    cometActors foreach (_ ! msg)

  def control(msg: TrackerControl) = msg match {
    case Add(ca) =>
      cometActors = ca :: cometActors
      log.info(this + " added " + ca)
    case Remove(ca) =>
      cometActors = cometActors filterNot (_ == ca)
      log.info(this + " removed " + ca)
  }
}
