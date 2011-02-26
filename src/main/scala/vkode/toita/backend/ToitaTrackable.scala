package vkode.toita.backend

import net.liftweb.http.CometActor
import akka.actor.ActorRef

trait ToitaTrackable {

  val cometActor: CometActor

  val session: UserSession = UserDB("sjetilv", "kjetilv").head 
//  getOrElse (throw new IllegalStateException("No user")) 
//    UserSession (System getProperty "token", System getProperty "apiSecret")

  val eventTypes: List[Class[_ <: TwitterEvent]]

  def tracker (twitterService: TwitterAsynchService): Option[ActorRef]
}
