package vkode.toita.gui.backend

import net.liftweb.http.CometActor
import akka.actor.ActorRef
import vkode.toita.waka.DB
import vkode.toita.events._

trait ToitaTrackable {

  val cometActor: CometActor

  val sessions: List[UserSession] = DB("sjetilv", "kjetilv") 

  val eventTypes: List[Class[_ <: TwitterEvent]]

  def tracker (twitterService: TwitterService): Option[ActorRef]
}
