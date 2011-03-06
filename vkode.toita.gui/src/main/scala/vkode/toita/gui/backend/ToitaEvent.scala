package vkode.toita.gui.backend

import net.liftweb.http.CometActor
import vkode.toita.gui.comet.ToitaComet

sealed trait ToitaEvent

case class CometUp(actor: ToitaComet) extends ToitaEvent

case class CometDown(actor: ToitaComet) extends ToitaEvent
