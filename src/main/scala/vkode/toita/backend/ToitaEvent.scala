package vkode.toita.backend

import net.liftweb.http.CometActor

sealed trait ToitaEvent

case class CometUp(actor: CometActor, eventTypes: List[Class[_ <: TwitterEvent]]) extends ToitaEvent

case class CometDown(actor: CometActor) extends ToitaEvent
