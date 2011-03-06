package vkode.toita.events

import org.joda.time.DateTime
import net.liftweb.json.JsonAST.JValue
abstract class TwitterEvent {

  val authenticatedUser: String
  
  val time: DateTime = new DateTime

  val json: JValue
}

