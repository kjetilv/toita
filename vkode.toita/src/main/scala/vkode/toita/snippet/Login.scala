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
import vkode.toita.waka.UserSession

class Login extends StatefulSnippet {

  def dispatch = {
    case "render" =>
      "#authlink" #> authlink &
      "#pin" #> getpin &
      "#pinstatus" #> pinstatus &
      "#go" #> andWeReOff &
      "#connected" #> connected
  }

  private var auth: Option[TwitterSession.Authentication] = None

  private var pin: Option[String] = None

  private var goodPin = false

  private val PIN_LENGTH = 7

  private var userSession: Option[UserSession] = None

  private def newAuthData = {
    auth = Option(TwitterSession.authenticateData)
    println("AuthData: " + auth)
    auth
  }

  private val loginLinkFunction =
    (a: TwitterSession.Authentication) => link(a.url, () => {}, Text("Login"), "target" -> "_blank")

  private def authlink: NodeSeq =
    newAuthData map (loginLinkFunction) getOrElse Text("Could not initiate authentication")

  private def getpin = ajaxText(pin getOrElse "PIN", newPin => {
    println("Pin received: '" + newPin + "'")
    pin = Option(if (newPin == null) null else newPin.trim)
    SetHtml("pinstatus", pinstatus)
  })

  private def pinstatus = pin map (pin => {
    val len = pin.length
    if (len < PIN_LENGTH) {
      goodPin = false
      Text("Short pin, missing " + (PIN_LENGTH - len))
    } else if (len > PIN_LENGTH) {
      goodPin = false
      Text("Long pin, " + (len - PIN_LENGTH) + " too many")
    } else if (pin forall (Character isDigit _)) {
      goodPin = true
      Text("Good pin")
    } else {
      goodPin = false
      Text("Bad pin")
    }
  }) getOrElse Text("No pin")

  private def andWeReOff: NodeSeq = ajaxButton("Connect", () => {
    auth map (auth => {
      userSession = pin map (pin => {
        Option(TwitterSession.access(auth, pin))
      }) getOrElse None
      userSession map (session => {
        println("User Session: " + session)
      })
    })
    SetHtml("connected", connected)
  })

  private def connected =
      if (userSession.isDefined) Text("Connected: " + userSession.get) else Text("Not connected")
}
