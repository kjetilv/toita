package vkode.toita.comet

import scala.xml._

import net.liftweb.json.JsonParser
import net.liftweb.common._
import xml.Text
import net.liftweb.http.{CometListener, CometActor}

import vkode.toita.backend._
import net.liftweb.http.js.JsCmds._
import net.liftweb.util.Helpers._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Date
import scalaz.Options

case class UserSession(token: String, secret: String) {

  def key = token + "-" + secret
}

class UserStream extends CometActor with Options {

  val session = UserSession(System getProperty "token", System getProperty "apiSecret")

  implicit def date2Yoda (date: Date) = new DateTime(date)

  private val statuses = new scala.collection.mutable.ListBuffer[TwitterStatusUpdate]()

  private var friends = Option[TwitterFriends](null)

  override protected def localSetup() = Updater ! UserStreamUp(this)

  override protected def localShutdown() = Updater ! UserStreamDown(this)

  override def render = bind("us",
                             "tweets" -> (renderTable getOrElse <span>Initializing..</span>),
                             "friends" -> (Rendrer render friendIds))

  private def friendIds: Option[List[BigInt]] = friends map (_ friends) map (_.friends)

  def renderTable: Option[NodeSeq] = {
    try {
      Option(Rendrer render statuses.toList)
    } catch {
      case e =>
        e.printStackTrace
        None
    }
  }

  def rerenderTable {
    try {
      val table: Option[NodeSeq] = renderTable
      table map (t => {
        partialUpdate(SetHtml("tweets", t))
        reRender(false)
      })
    }
    catch {
      case e => e.printStackTrace
    }
  }

  override def lowPriority: PartialFunction[Any, Unit] = {
    case events: List[TwitterEvent] => {
      try {
        val updates = events filter (_.isInstanceOf[TwitterStatusUpdate]) map (_.asInstanceOf[TwitterStatusUpdate])
        println(this + ": Updates: " + updates)
        statuses ++= updates
        rerenderTable
        val friends = events filter (_.isInstanceOf[TwitterFriends]) map (_.asInstanceOf[TwitterFriends]) match {
          case Nil =>
          case friends => this.friends = Option(friends.last)
          partialUpdate(SetHtml("friends", Rendrer render friendIds))
          reRender(false)
        }
      } catch {
        case e => e.printStackTrace
      }
    }
  }

  override def toString = getClass.getSimpleName +
                          "[name:" + name + " " + statuses.size + " " + System.identityHashCode(this) + "]"
}
