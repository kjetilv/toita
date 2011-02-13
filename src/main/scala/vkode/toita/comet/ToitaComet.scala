package vkode.toita.comet

import scalaz.Options
import net.liftweb.common.Loggable
import vkode.toita.backend.ToitaSessionUser
import net.liftweb.http.CometActor
import java.util.Date
import org.joda.time.DateTime

trait ToitaComet extends CometActor with ToitaSessionUser with Loggable with Options {

  protected implicit def date2Yoda (date: Date) = new DateTime(date)
}
