package vkode.toita.backend

import net.liftweb.http.CometActor
import vkode.toita.comet.UserStream

sealed trait UserStreamEvent

case class UserStreamUp(actor: UserStream) extends UserStreamEvent

case class UserStreamDown(actor: UserStream) extends UserStreamEvent
