package vkode.toita.gui.backend

import scala.collection.mutable.ListBuffer
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{SHtml, SessionVar}
import xml.{Node, Text, NodeSeq}

class Pager(sessionVar: SessionVar[Long],
            size: Long,
            toDraw: Long,
            reDraw: () => JsCmd) {

  def apply(xhtml : NodeSeq): NodeSeq = {
    if (size < toDraw) {
      sessionVar(0)
      return Text("All shown")
    }
    val list = new ListBuffer[Node]
    makeRangeSeq(sessionVar, size, pos => {
      list += (if (sessionVar == pos) <span>{pos.toString}</span> else SHtml.ajaxButton(pos.toString, () => {
        sessionVar(pos)
        reDraw()
      }))
    })
    NodeSeq fromSeq list.toSeq
  }

  def log10 (i: Long): Long = i.toString.size

  def pow (i: Long, j:Long) = math.pow(i, j).asInstanceOf[Long]

  def makeRangeSeq(start: Long, pos: Long, size: Long, tens: Long, fun: (Long) => Unit): Unit = {
    val stepSize = pow(10, tens)
    for (i <- 1 to 10) {
      val notch = start + ((i - 1) * stepSize)
      if (notch > size) return
      else if (pos >= notch && pos < (notch + stepSize) && tens > 1) makeRangeSeq(notch, pos, size, tens - 1, fun)
      else fun(notch)
    }
  }

  def makeRangeSeq(pos: Long, size: Long, fun: (Long) => Unit): Unit = makeRangeSeq(0, pos, size, log10(size) - 1, fun)
}
