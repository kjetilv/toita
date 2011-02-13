package vkode.toita.snippet

import net.liftweb._
import util.Helpers._
import vkode.toita.backend.ToitaSessionUser

object User extends ToitaSessionUser {

  lazy val user = theTwitter.user
}
