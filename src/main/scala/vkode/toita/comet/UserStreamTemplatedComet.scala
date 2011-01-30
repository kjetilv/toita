package vkode.toita.comet

import scala.xml._
import net.liftweb.common._
import vkode.toita.backend._
import net.liftweb.http.js.JsCmds._
import java.util.Date
import scalaz.Options
import org.joda.time.DateTime
import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.util._
import Helpers._
import SHtml._

class UserStreamTemplatedComet extends UserStream {

  private var friends: Option[TwitterFriends] = None

  private def tweetarea = findElems (defaultXml) (_.attribute("id").map(_ == "tweetarea") getOrElse false)

  override def render = {
    logger.info(this + " found tweetarea: " + tweetarea)
    bind("us", tweetarea,
         "img" -> Text("img"),
         "name" -> Text("name"),
         "text" -> Text("text"))
  }

  protected def updated {
    stream map(_.items) map (_ flatMap (item => {
      Text(item.toString)
    }))
  }
}
