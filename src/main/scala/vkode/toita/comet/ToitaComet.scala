package vkode.toita.comet

import scalaz.Options
import net.liftweb.common.Loggable
import vkode.toita.backend.{ToitaRegister, ToitaSessionUser}
import net.liftweb.http.CometActor
import xml.NodeSeq

trait ToitaComet
    extends CometActor with ToitaRegister with ToitaSessionUser with Loggable with Options
