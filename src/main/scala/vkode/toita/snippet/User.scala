package vkode.toita.snippet

import net.liftweb._
import util.Helpers._
import vkode.toita.backend.ToitaSessionUser

object User extends ToitaSessionUser {

  lazy val user = theTwitter.user

  def render = ("#screenName" #> (user map (_.screen_name) getOrElse "[unknown]") &
                "#name" #> (user map (_.name) getOrElse "[unknown]") &
                "#description" #> (user map (_.description) getOrElse None getOrElse "[unknown]"))
}
