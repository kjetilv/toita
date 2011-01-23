package vkode.toita.comet

import scala.xml._
import scala.math._

import net.liftweb.common._
import vkode.toita.backend._
import net.liftweb.http.js.JsCmds._
import java.util.Date
import scalaz.Options
import org.joda.time.DateTime
import net.liftweb.http.{DispatchSnippet, RequestVar, S, SHtml, SessionVar, CometActor}
import S._
import SHtml._

object Index extends SessionVar[Int](0)

object Count extends SessionVar[Int](20)

class UserStreamComet
    extends CometActor with Options with ToitaRegister with ToitaSessionUser with Loggable {

  implicit def date2Yoda (date: Date) = new DateTime(date)

  private var friends: Option[TwitterFriends] = None

  private var conversation: Option[Stream[TwitterStatusUpdate]] = None

  override def render =
    bind("us",
         "next" -> ajaxButton("Next", () => {
           Index(math.min(conversation.map(_.total - Count.get) getOrElse 0,
                          Index.get + Count.get))
           SetHtml ("tweets", renderTable)
         }),
         "index" -> Text(Index.get.toString),
         "total" -> Text(conversation.map(_.total.toString) getOrElse "0"),
         "previous" -> ajaxButton("Previous", () => {
           Index(math.max(Index.get - 1, 0))
           SetHtml ("tweets", renderTable)
         }),
         "count" -> ajaxSelect(List(("20" → "20"),
                                    ("40" → "40"),
                                    ("60" → "60"),
                                    ("80" → "80"),
                                    ("100" → "100")),
                               Full("20"),
                               value => {
                                 Count(value.toInt)
                                 SetHtml ("tweets", renderTable)
                               }),
         "tweets" -> renderTable)

  def renderTable: NodeSeq =
    try {
      conversation map (conv => {
        Rendrer render (conv.items drop (Index.get * 10) take Count.get)
      }) getOrElse <span>Waiting for conversation...</span>
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
    case update: Stream[TwitterStatusUpdate] =>
      conversation = Option(update)
      rerenderTable
  }

  override def toString = getClass.getSimpleName + "[name:" + name + " " + System.identityHashCode(this) + "]"
}
