package vkode.toita.backend

import vkode.toita.backend.Tracker.TrackerControl
import vkode.toita.events.{TOFriends, TwitterFriend, TwitterFriends}

class PeopleTracker (val twitterService: TwitterService) extends Tracker {

  protected def receive = {
    case msg: TrackerControl => control(msg)
    case TwitterFriends(TOFriends(list), _, _) => twitterService users list
    case TwitterFriend(user, _, _) => send(user)
  }
}
