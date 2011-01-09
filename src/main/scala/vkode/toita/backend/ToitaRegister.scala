package vkode.toita.backend

import akka.actor.Actors._
import net.liftweb.http.CometActor

trait ToitaRegister {

  this: CometActor =>

  override protected def localSetup() = broadcast (CometUp(this))

  override protected def localShutdown() = broadcast (CometDown(this))

  private def broadcast (msg: Any) =
    registry.actorsFor[ToitaCentral] foreach (_ ! msg)
}
