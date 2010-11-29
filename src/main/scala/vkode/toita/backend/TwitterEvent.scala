package vkode.toita.backend

import org.joda.time.DateTime

abstract sealed class TwitterEvent(time: DateTime = new DateTime)

case class TwitterFriends(friends: TOFriends) extends TwitterEvent

case class TwitterStatusDelete(status: TOStatusRef) extends TwitterEvent

case class TwitterStatusUpdate(status: TOStatus, user: Option[TOUser], entities: TOEntities, reply: Option[TOReply])
  extends TwitterEvent
