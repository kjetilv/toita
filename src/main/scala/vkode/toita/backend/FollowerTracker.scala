package vkode.toita.backend

import net.liftweb.http.CometActor
import akka.util.Logging
import akka.actor.Actor

class FollowerTracker (followed: CometActor, twitterSession: TwitterAsynchService)
    extends Actor with Logging {

  protected def receive = {
    case TwitterFriends(TOFriends(list), _) => twitterSession users list
    case TwitterFriend(user, _) => followed ! user
  }
}
