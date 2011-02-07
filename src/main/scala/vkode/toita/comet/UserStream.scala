package vkode.toita.comet

import org.joda.time.DateTime
import java.util.Date
import net.liftweb.common.Loggable
import net.liftweb.http.CometActor
import scalaz.Options
import vkode.toita.backend.{TStream, TwitterStatusUpdate}

trait UserStream extends ToitaComet {

  protected var stream: Option[TStream[TwitterStatusUpdate]] = None

  override def lowPriority: PartialFunction[Any, Unit] = {
    case update: TStream[TwitterStatusUpdate] =>
      stream = Option(update)
      updated
  }

  protected def updated: Unit

  override def toString = getClass.getSimpleName + "[name:" + name + " " + System.identityHashCode(this) + "]"
}

