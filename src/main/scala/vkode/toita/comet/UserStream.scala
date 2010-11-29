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

class UserStream extends CometActor {

  implicit def date2Yoda (date: Date) = new DateTime(date)

  private val statuses = new scala.collection.mutable.ListBuffer[TwitterStatusUpdate]()

  private var friends = Option[TwitterFriends](null)

  override protected def localSetup() = Updater ! UserStreamUp(this)

  override protected def localShutdown() = Updater ! UserStreamDown(this)

  override def render = bind("us",
    "tweets" -> renderList,
    "friends" -> renderFriends)

  private def renderFriends =
    <span> { friendIds map (_ mkString ",") getOrElse "No friends!" } </span>

  private def friendIds: Option[List[BigInt]] = friends map (_ friends) map (_.friends)

  private def renderList =
    <table>
      { bulletPoints }
    </table>

  private def newestFirst (tsu1: TwitterStatusUpdate, tsu2: TwitterStatusUpdate) =
    tsu1.status.created_at isAfter tsu2.status.created_at

  private def bulletPoints = statuses sortWith (newestFirst (_, _)) map (row (_)) takeWhile (_.isDefined) take 10 map (_.get)

  private def row(event: TwitterEvent): Option[NodeSeq] =
    event match {
      case TwitterStatusUpdate(status, Some(user), _, _) =>
        Some(
          <tr>
            <td width="50">
                <img src={ user.profile_image_url } width="48" height="48"/>
            </td>
            <td width="300">
              { status.text }
            </td>
          </tr>
          <tr>
            <td>{ user.screen_name }</td>
            <td>{ user.name } @ { time(status.created_at) }</td>
          </tr>)
      case _ => None
    }

  private val formatter = DateTimeFormat forPattern ("yyyy-MM-dd hh:mm:ss")

  private def time(dt: DateTime) = formatter print dt

  private def addUpdate (update: TwitterStatusUpdate) = {
    update +=: statuses
  }

  override def highPriority: PartialFunction[Any, Unit] = {
    case update : TwitterStatusUpdate => {
      update +=: statuses
      partialUpdate (SetHtml ("tweets", renderList))
      reRender (false)
    }
    case friends: TwitterFriends => {
      this.friends = Some(friends)
      partialUpdate (SetHtml ("friends", renderFriends))
      reRender (false)
    }
  }

  private def listSize = Text(statuses.size.toString)
}
