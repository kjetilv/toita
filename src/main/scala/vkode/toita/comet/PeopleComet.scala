package vkode.toita.comet

import vkode.toita.backend.TOUser
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

class PeopleComet extends ToitaCSSComet {

  private var users = Map[BigInt,TOUser]()

  private def withUser(m: Map[BigInt, TOUser], u: TOUser) =
    if (m contains u.id) m else m + (u.id -> u)

  override def lowPriority = {
    case newUsers: List[TOUser] =>
      users = (users /: newUsers) (withUser(_, _))
      partialUpdate(SetHtml(area, getNodeSeq))
      reRender(false)
    case user: TOUser =>
      users = withUser(users, user)
      partialUpdate(SetHtml(area, getNodeSeq))
      reRender(false)
  }

  protected val area = "people-area"

  protected override def getNodeSeq: NodeSeq =
    if (!users.isEmpty)
      NodeSeq fromSeq (users.values map (transformFun (_)) flatMap (_ (defaultXml)) toList)
    else
      <span>No friends!</span>

  private def transformFun(user: TOUser): CssBindFunc = user match {
    case TOUser(id, screen_name, name, desc, imageurl, userDeco) =>
      userDeco match {
        case Some(TOUserDecoration(profile_use_background_image,
                                   profile_background_image_url,
                                   profile_text_color,
                                   profile_link_color)) =>
          val style = "color:#" + (profile_link_color getOrElse "3333ff")
          "#people-name" #> <span style={ style }>{ screen_name }</span> &
          "#people-img" #> <img src={ imageurl } alt={ name }/>
        case None =>
          "#people-name" #> <span>{ screen_name }</span> &
          "#people-img" #> <img src={ imageurl } alt={ name }/>
      }
    case x =>
      println(x)
      "#people-name" #> <span>No name</span> &
      "#people-img" #> <span>No logo</span>
  }
}
