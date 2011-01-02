package vkode.toita.comet

import net.liftweb.common.Loggable
import net.liftweb.http.CometActor
import scalaz.Options
import net.liftweb.http.js.JsCmds.SetHtml
import vkode.toita.backend.{Rendrer, TOUser, ToitaSessionUser, ToitaRegister}

class FolloweesComet
    extends CometActor with Options with ToitaRegister with ToitaSessionUser with Loggable {

  def render = bind ("f",
                     "list" -> renderFriends)

  private var users = Map[BigInt,TOUser]()

  override def lowPriority = {
    case users: List[TOUser] => {
      this.users = (this.users /: users) ((m, u) => m + (u.id -> u))
      rerenderFriends
    }
    case user: TOUser =>
      this.users = this.users + (user.id -> user)
      rerenderFriends
  }

  private def rerenderFriends = {
    partialUpdate(SetHtml("list", renderFriends))
    reRender
  }

  private def renderFriends =
    if (users.isEmpty)
      <span>No friends!</span>
    else
      <ul>
        { users.values.map (id => <li> { Rendrer renderSmall id } </li>) }
      </ul>
}
