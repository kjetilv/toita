package vkode.toita.backend

import vkode.toita.comet.UserStream

sealed trait UserStreamEvent

case class UserStreamUp(actor: UserStream) extends UserStreamEvent

case class UserStreamDown(actor: UserStream) extends UserStreamEvent
