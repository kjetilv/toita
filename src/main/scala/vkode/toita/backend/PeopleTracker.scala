package vkode.toita.backend

import vkode.toita.backend.Tracker.TrackerControl

class PeopleTracker (val twitterService: TwitterService) extends Tracker {

  protected def receive = {
    case msg: TrackerControl => control(msg)
    case TwitterFriends(TOFriends(list), _) => twitterService users list
    case TwitterFriend(user, _) => send(user)
  }
}
