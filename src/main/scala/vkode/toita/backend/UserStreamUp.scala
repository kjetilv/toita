package vkode.toita.backend

import net.liftweb.http.CometActor

case class UserStreamUp(actor: CometActor)

case class UserStreamDown(actor: CometActor)
