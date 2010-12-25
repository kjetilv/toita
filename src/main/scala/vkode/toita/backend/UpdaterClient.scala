package vkode.toita.backend

import akka.actor.ActorRegistry

trait UpdaterClient {

  def broadcast (msg: Any) = ActorRegistry.actorsFor[TwitterEventSource] foreach (_ ! msg)
}
