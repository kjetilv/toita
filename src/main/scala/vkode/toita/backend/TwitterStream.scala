package vkode.toita.backend

import java.io.InputStream
import akka.util.Logging

case class TwitterStream(private val stream: Iterator[String],
                         private val source: InputStream)
    extends Logging with Iterator[String] {

  def next() = stream.next

  def hasNext = stream.hasNext

  def close = try {
    source.close
  } catch {
    case e => log.warn(this + " failed to close stream!", e)
  }
}
