package vkode.toita.backend

import akka.util.Logging
import akka.actor.Actor
import vkode.toita.backend.Tracker.TrackerControl

class FollowerTracker (twitterSession: TwitterAsynchService)
    extends Tracker with Actor with Logging {

  protected def receive = {
    case msg: TrackerControl => control(msg)
    case TwitterFriends(TOFriends(list), _) => twitterSession users list
    case TwitterFriend(user, _) => send(user)
  }
}
