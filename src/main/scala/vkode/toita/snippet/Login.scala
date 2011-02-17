package vkode.toita.snippet

import vkode.toita.backend.TwitterSession
import net.liftweb.http.js.JsCmds.SetHtml
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
import S._

class Login extends StatefulSnippet {

  var auth: Option[TwitterSession.Authentication] = None

  var pin = "PIN"

  var userSession: Option[UserSession] = None

  def enterPin: Elem = {
    text(pin, pin = _)
  }

  def dispatch = {
    case "render" =>
      "#authlink" #> linkOutToTheWorld &
      "#pin" #> enterPin &
      "#go" #> andWeReOff
  }

  def andWeReOff: NodeSeq = {
    userSession = Option(TwitterSession.access(auth.get, pin))
    println(userSession)
    if (userSession.isDefined) Text("OK!") else Text("Damn")
  }

  def linkOutToTheWorld: NodeSeq = {
    auth = Option(TwitterSession.authenticateData)
    auth map (a => link(a.url, () => {}, Text("Login"), "target" -> "_blank")) getOrElse Text("Could not initiate authentication")
  }
}
