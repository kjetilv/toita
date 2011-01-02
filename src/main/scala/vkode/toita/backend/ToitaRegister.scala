package vkode.toita.backend

import akka.actor.ActorRegistry
import net.liftweb.http.CometActor

trait ToitaRegister {

  this: CometActor =>

  override protected def localSetup() = broadcast (CometUp(this))

  override protected def localShutdown() = broadcast (CometDown(this))

  private def broadcast (msg: Any) =
    ActorRegistry.actorsFor[ToitaCentral] foreach (_ ! msg)
}
