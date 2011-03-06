package vkode.toita.backend

import akka.actor.Actors._
import vkode.toita.comet.ToitaComet
import net.liftweb.http.CometActor
import akka.actor.ActorRef

trait ToitaRegister {

  this: ToitaComet =>

  type EventTypes = List[Class[_ <: TwitterEvent]]

  val eventTypes: EventTypes = Nil

  val cometActor: CometActor = this

  def tracker (twitterService: TwitterService): Option[ActorRef] = None

  override protected def localSetup() = broadcast (CometUp(this))

  override protected def localShutdown() = broadcast (CometDown(this))

  private def broadcast (msg: Any) = registry.actorsFor[ToitaCentral] foreach (_ ! msg)
}
