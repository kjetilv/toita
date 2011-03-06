package vkode.toita.gui.comet

import scalaz.Options
import net.liftweb.common.Loggable
import net.liftweb.http.CometActor
import java.util.Date
import org.joda.time.DateTime

trait ToitaComet extends CometActor with Loggable with Options {

  protected implicit def date2Yoda (date: Date) = new DateTime(date)
}
