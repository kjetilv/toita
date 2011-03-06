package vkode.toita.comet

import scala.xml._
import net.liftweb.http._

trait ToitaCSSComet extends ToitaComet {

  override def render: RenderOut = getNodeSeq

  protected def getNodeSeq: NodeSeq
}
