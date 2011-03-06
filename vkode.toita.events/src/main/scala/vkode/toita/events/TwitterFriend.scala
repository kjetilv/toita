package vkode.toita.events

import net.liftweb.json.JsonAST.JValue

case class TwitterFriend(friend: TOUser,
                         override val authenticatedUser: String,
                         override val json: JValue) extends TwitterEvent











