package vkode.toita.backend

import net.liftweb.http.CometActor
import akka.actor.ActorRef

trait ToitaTrackable {

  val cometActor: CometActor

  val session: UserSession

  val eventTypes: List[Class[_ <: TwitterEvent]]

  def tracker (twitterService: TwitterAsynchService): Option[ActorRef]
}
