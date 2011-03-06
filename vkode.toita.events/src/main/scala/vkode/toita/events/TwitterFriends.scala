package vkode.toita.events

import net.liftweb.json.JsonAST.JValue

case class TwitterFriends(friends: TOFriends,
                          override val authenticatedUser: String,
                          override val json: JValue) extends TwitterEvent











