package vkode.toita.comet

import net.liftweb.http.CometActor
import actors.threadpool.AtomicInteger
import net.liftweb.http.js.JsCmds.SetHtml
import vkode.toita.backend.ToitaRegister
import xml.NodeSeq

object DiagnosticsComet {

  sealed trait DiagnosticsEvent {
    def timestamp: Long = System.currentTimeMillis
  }

  case object StreamUp extends DiagnosticsEvent

  case object StreamDown extends DiagnosticsEvent

  case object LookupStarted extends DiagnosticsEvent

  case object LookupEnded extends DiagnosticsEvent
}

class DiagnosticsComet extends ToitaCSSComet with ToitaRegister {

  val streamCount = new AtomicInteger

  val lookupCount = new AtomicInteger

  def streams = <span>{ streamCount.get }</span>

  def lookups = <span>{ lookupCount.get }</span>

  protected def getNodeSeq =
    ("#streams" #> streams &
    "#lookups" #> lookups) (defaultXml)

  def set(where: String, seq: => NodeSeq) {
    partialUpdate(SetHtml(where, seq))
    reRender(true)
  }

  override def lowPriority = {
    case DiagnosticsComet.StreamUp =>
      streamCount.incrementAndGet
      set("streams", streams)
    case DiagnosticsComet.StreamDown =>
      streamCount.decrementAndGet
      set("streams", streams)
    case DiagnosticsComet.LookupStarted =>
      lookupCount.incrementAndGet
      set("lookup", lookups)
    case DiagnosticsComet.LookupEnded =>
      lookupCount.decrementAndGet
      set("lookup", lookups)
  }
}
