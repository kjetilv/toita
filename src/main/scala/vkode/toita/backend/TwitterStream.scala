package vkode.toita.backend

import java.io.InputStream
import com.weiglewilczek.slf4s.Logging

case class TwitterStream(private val stream: Iterator[String],
                         private val source: InputStream) extends Logging with Iterator[String] {

  def next() = stream.next

  def hasNext = stream.hasNext

  def close = try {
    source.close
  } catch {
    case e => logger.warn(this + " failed to close stream!", e)
  }
}
