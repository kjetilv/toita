package vkode.toita.backend

import net.liftweb.http.CometActor
import akka.actor.ActorRef

trait ToitaTrackable {

  val cometActor: CometActor

  val sessions: List[UserSession] = UserDB("sjetilv", "kjetilv") 

  val eventTypes: List[Class[_ <: TwitterEvent]]

  def tracker (twitterService: TwitterService): Option[ActorRef]
}
