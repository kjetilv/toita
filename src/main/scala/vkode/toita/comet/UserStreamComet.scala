package vkode.toita.comet

import scala.xml._
import scala.math._

import net.liftweb.common._
import net.liftweb.util._
import Helpers._
import vkode.toita.backend._
import net.liftweb.http.js.JsCmds._
import java.util.Date
import scalaz.Options
import org.joda.time.DateTime
import net.liftweb.http._
import S._
import SHtml._

object Index extends SessionVar[Int](0)

object Count extends SessionVar[Int](20)


