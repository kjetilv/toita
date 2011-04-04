package vkode.toita.gui.backend

import akka.actor.ActorRef
import vkode.toita.events.{TOUser, UserRef, TwitterService}

abstract class RemoteTwitterService(userRef: UserRef, ref: ActorRef) extends TwitterService {

  override val userName = userRef.screenName

  override def homeTimeline() {
    ref ! TwitterService.HomeTimeline
  }

  override def status(id: BigInt) {
    ref ! TwitterService.Status(id)
  }

  def users(ids: List[BigInt]) = ref ! TwitterService.Users(ids)

  override def user = ref !! TwitterService.User match {
    case Some(user: TOUser) => Some(user)
    case x => None
  }
}