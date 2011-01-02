package vkode.toita.backend

import net.liftweb.http.CometActor
import akka.actor.Actor
import akka.util.Logging

class FollowerTracker (followees: CometActor, twitterSession: TwitterSession)
    extends Actor with Logging {

  protected def receive = {
    case TwitterFriends(TOFriends(list)) => followees ! list
  }
}
