package vkode.toita.events

import net.liftweb.json.JsonAST.JValue

case class TwitterStatusDelete(status: TOStatusRef,
                               override val authenticatedUser: String,
                               override val json: JValue) extends TwitterEvent











