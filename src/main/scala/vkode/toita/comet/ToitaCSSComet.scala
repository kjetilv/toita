package vkode.toita.comet

import xml.NodeSeq
import net.liftweb.http.CometActor
import net.liftweb.http.js.JsCmds._
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

trait ToitaCSSComet {

  this: CometActor =>

  protected val area: String

  override def render: RenderOut = getNodeSeq

  protected def getNodeSeq: NodeSeq

  protected def updated {
    partialUpdate(SetHtml(area, getNodeSeq))
    reRender(false)
  }
}
