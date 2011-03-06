package vkode.toita.backend

import net.liftweb.http.CometActor
import vkode.toita.comet.ToitaComet

sealed trait ToitaEvent

case class CometUp(actor: ToitaComet) extends ToitaEvent

case class CometDown(actor: ToitaComet) extends ToitaEvent
