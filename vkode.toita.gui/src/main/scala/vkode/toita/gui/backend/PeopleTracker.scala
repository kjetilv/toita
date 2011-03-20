package vkode.toita.gui.backend

import vkode.toita.gui.backend.Tracker.TrackerControl
import vkode.toita.events._

class PeopleTracker (val twitterService: TwitterService) extends Tracker {

  protected def receive = {
    case msg: TrackerControl => control(msg)
    case TwitterFriends(TOFriends(list), _, _) => twitterService users list
    case TwitterFriend(user, _, _) => send(user)
  }
}
