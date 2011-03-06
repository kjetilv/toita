package vkode.toita.gui.backend

import net.liftweb.http.CometActor
import akka.actor.ActorRef
import vkode.toita.events.TwitterEvent
import vkode.toita.waka.{UserDB, UserSession}

trait ToitaTrackable {

  val cometActor: CometActor

  val sessions: List[UserSession] = UserDB("sjetilv", "kjetilv") 

  val eventTypes: List[Class[_ <: TwitterEvent]]

  def tracker (twitterService: TwitterService): Option[ActorRef]
}
