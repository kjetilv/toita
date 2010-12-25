package vkode.toita.backend

import java.io.InputStream
import com.weiglewilczek.slf4s.Logging

case class TwitterStream(stream: Iterator[String], private val source: InputStream) extends Logging {

  def close = try {
    source.close
  } catch {
    case e => logger.warn(this + " failed to close stream!", e)
  }
}
