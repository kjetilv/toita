package vkode.toita.backend

import net.liftweb.http.CometActor
import akka.util.Logging

object Tracker {

  trait TrackerControl

  case class Add(ca: CometActor) extends TrackerControl

  case class Remove(ca: CometActor) extends TrackerControl
}

trait Tracker {
  this: Logging =>

  import Tracker._

  private var cas: List[CometActor] = Nil

  protected def send(msg: Any) = cas foreach (_ ! msg)

  def control(msg: TrackerControl) = msg match {
    case Add(ca) =>
      cas = ca :: cas
      log.info(this + " added " + ca)
    case Remove(ca) =>
      cas = cas filterNot (_ == ca)
      log.info(this + " removed " + ca)
  }
}
