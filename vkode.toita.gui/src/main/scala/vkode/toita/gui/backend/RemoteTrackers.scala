package vkode.toita.gui.backend

import akka.actor.ActorRef
import akka.actor.Actors._

object RemoteTrackers {
  
  private val localModule = remote.start("localhost", 4002)
  
  private val casting = remote.actorFor("casting", "localhost", 4020)
  
  def update(name: String, tracker: Option[ActorRef]) = tracker match {
    case Some(tracker) => localModule.register(name, tracker)
    case None => localModule unregister name
  }
}