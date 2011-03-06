package vkode.toita.events

import org.joda.time.DateTime
import net.liftweb.json.JsonAST.JValue
import vkode.toita.events.Treeable

case class TwitterFollower(event: TOFollowEvent, 
                           override val authenticatedUser: String,
                           override val json: JValue) extends TwitterEvent











