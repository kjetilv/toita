package vkode.toita.comet

import net.liftweb.http.CometActor
import actors.threadpool.AtomicInteger
import net.liftweb.http.js.JsCmds.SetHtml
import vkode.toita.backend.ToitaRegister
import xml.NodeSeq

object DiagnosticsComet {

  case object StreamUp

  case object StreamDown

  case object LookupStarted

  case object LookupEnded
}

class DiagnosticsComet extends CometActor with ToitaRegister {

  val streamCount = new AtomicInteger

  val lookupCount = new AtomicInteger

  def streams = <span>{ streamCount.get }</span>

  def lookups = <span>{ lookupCount.get }</span>

  def render = bind("dia",
                    "streams" -> streams,
                    "lookups" -> lookups)

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
