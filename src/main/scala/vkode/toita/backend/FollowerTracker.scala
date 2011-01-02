package vkode.toita.backend

import net.liftweb.http.CometActor
import akka.util.Logging
import akka.actor.Actor

class FollowerTracker (followees: CometActor, twitterSession: TwitterService)
    extends Actor with Logging {

  protected def receive = {
    case TwitterFriends(TOFriends(list)) => twitterSession users list
    case TwitterFriend(user) => followees ! user
  }
}
