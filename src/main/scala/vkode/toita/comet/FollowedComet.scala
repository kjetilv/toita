package vkode.toita.comet

import net.liftweb.http.js.JsCmds.SetHtml
import vkode.toita.backend.{Rendrer, TOUser}

class FollowedComet extends ToitaComet {

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
