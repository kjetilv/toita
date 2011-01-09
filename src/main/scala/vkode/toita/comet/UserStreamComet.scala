package vkode.toita.comet

import scala.xml._

import net.liftweb.common._
import net.liftweb.http.CometActor

import vkode.toita.backend._
import net.liftweb.http.js.JsCmds._
import java.util.Date
import scalaz.Options
import org.joda.time.DateTime

class UserStreamComet
    extends CometActor with Options with ToitaRegister with ToitaSessionUser with Loggable {

  implicit def date2Yoda (date: Date) = new DateTime(date)

  private var friends: Option[TwitterFriends] = None

  private var conversation: Option[Conversation[TwitterStatusUpdate]] = None

  override def render = bind("us",
                             "tweets" -> renderTable,
                             "friends" -> renderFriends)

  def renderFriends: NodeSeq =
    Rendrer render (friends map (_ friends) map (_.friends))

  def renderTable: NodeSeq =
    try {
      conversation map (conv => Rendrer render (conv.items take 250)) getOrElse <span>Waiting for conversation...</span>
    } catch {
      case e =>
        logger.warn("Failed to render table!", e)
        <span>Internal error rendering table {e}</span>
    }

  def rerenderTable =
      try {
        partialUpdate (SetHtml ("tweets", renderTable))
      } catch {
        case e => logger.warn("Failed to render table!", e)
      } finally {
        reRender
      }

  override def lowPriority: PartialFunction[Any, Unit] = {
    case update: Conversation[TwitterStatusUpdate] =>
      conversation = Option(update)
      rerenderTable
    case friends: TwitterFriends =>
      this.friends = Some(friends)
      partialUpdate(SetHtml("friends", renderFriends))
      reRender
  }

  override def toString = getClass.getSimpleName + "[name:" + name + " " + System.identityHashCode(this) + "]"
}
