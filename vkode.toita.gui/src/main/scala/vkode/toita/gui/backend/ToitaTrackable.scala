package vkode.toita.gui.backend

import net.liftweb.http.CometActor
import akka.actor.ActorRef
import vkode.toita.events._

trait ToitaTrackable {

  val cometActor: CometActor

  val sessions: List[UserRef] = List(UserRef("sjetilv"), UserRef("kjetilv")) 

  val eventTypes: List[Class[_ <: TwitterEvent]]

  def tracker (twitterService: TwitterService): Option[ActorRef]
}
