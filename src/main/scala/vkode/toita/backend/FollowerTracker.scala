package vkode.toita.backend

import net.liftweb.http.CometActor
import akka.util.Logging
import akka.actor.{ActorRef, Actor}
import vkode.toita.comet.DiagnosticsComet.{LookupEnded, LookupStarted}

class FollowerTracker (followees: CometActor, twitterSession: TwitterSession, diagnostics: ActorRef)
    extends Actor with Logging with JsonEvents {

  private def retrieveUsers(ids: List[BigInt]) = ids.sliding(25, 25) foreach { window =>
    foreachEvent (twitterSession getFriends window) { user =>
      diagnostics ! LookupStarted
      followees ! user
      diagnostics ! LookupEnded }
  }

  protected def receive = {
    case TwitterFriends(TOFriends(list)) => retrieveUsers(list)
    case TwitterFriend(user) => followees ! user
  }
}
