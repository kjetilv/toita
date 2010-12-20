package vkode.toita.comet

import scala.xml._

import net.liftweb.common._
import net.liftweb.http.CometActor

import vkode.toita.backend._
import net.liftweb.http.js.JsCmds._
import java.util.Date
import scalaz.Options
import akka.actor.ActorRegistry
import org.joda.time.{Duration, DateTime}

trait UpdaterClient {

  def update (msg: Any) = ActorRegistry.actorsFor[Updater] foreach (_ ! msg)
}

case class UserSession(token: String, secret: String) {

  def key = token + "-" + secret
}

class UserStream extends CometActor with Options with UpdaterClient with Loggable {

  val session = UserSession(System getProperty "token", System getProperty "apiSecret")

  implicit def date2Yoda (date: Date) = new DateTime(date)

  private val statuses = new scala.collection.mutable.ListBuffer[TwitterStatusUpdate]()

  private var friends = Option[TwitterFriends](null)

  private var lastTableRerender = new DateTime(0)

  override protected def localSetup() = update (UserStreamUp(this))

  override protected def localShutdown() = update (UserStreamDown(this))

  override def render = bind("us",
                             "tweets" -> renderTable,
                             "friends" -> renderFriends)

  private def friendIds: Option[List[BigInt]] = friends map (_ friends) map (_.friends)

  def renderFriends = Rendrer render friendIds

  def renderTable: NodeSeq =
    try {
      Rendrer render statuses.toList
    } catch {
      case e =>
        logger.warn("Failed to render table!", e)
        <table>
          <tr>
            <td>Internal error rendering table {e}</td>
          </tr>
        </table>
    }

  def rerenderTable = {
    val now = new DateTime()
    if (new Duration(lastTableRerender, now).getMillis > 1000) {
      try {
        partialUpdate(SetHtml("tweets", renderTable))
      } catch {
        case e =>
          logger.warn("Failed to render table!", e)
          partialUpdate(SetHtml("tweets",
                                <table>
                                  <tr>
                                    <td>Internal error updating table: {e}</td>
                                  </tr>
                                </table>))
      } finally {
        reRender(false)
        lastTableRerender = now
      }
    }
  }

  override def lowPriority: PartialFunction[Any, Unit] = {
    case update: TwitterStatusUpdate =>
      statuses += update
      rerenderTable
    case friends: TwitterFriends =>
      partialUpdate(SetHtml("friends", Rendrer render friendIds))
      reRender(false)
  }

  override def toString = getClass.getSimpleName +
                          "[name:" + name + " " + statuses.size + " " + System.identityHashCode(this) + "]"
}
