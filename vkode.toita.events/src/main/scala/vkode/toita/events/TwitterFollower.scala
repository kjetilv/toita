package vkode.toita.events

import net.liftweb.json.JsonAST.JValue

case class TwitterFollower(event: TOFollowEvent, 
                           override val authenticatedUser: String,
                           override val json: JValue) extends TwitterEvent











