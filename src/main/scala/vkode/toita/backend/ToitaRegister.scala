package vkode.toita.backend

import akka.actor.Actors._
import net.liftweb.http.CometActor

trait ToitaRegister {

  this: CometActor =>

  type EventTypes = List[Class[_ <: TwitterEvent]]

  protected val eventTypes: EventTypes = Nil

  override protected def localSetup() = broadcast (CometUp(this, eventTypes))

  override protected def localShutdown() = broadcast (CometDown(this))

  private def broadcast (msg: Any) = registry.actorsFor[ToitaCentral] foreach (_ ! msg)
}
