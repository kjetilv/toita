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

class UserStreamTemplatedComet extends UserStream with ToitaCSSComet {

  protected val area = "tweetarea"

  private def tweetCount = 5

  protected override def getNodeSeq: NodeSeq = stream map (_ items) map (_ take tweetCount) map (_ match {
    case Nil => <span>No tweets</span>
    case items => Rendrer renderStatusStream (items, defaultXml)
  }) getOrElse <span>Connecting ...</span>

  protected def updated {
    partialUpdate(SetHtml(area, getNodeSeq))
    reRender(false)
  }
}
