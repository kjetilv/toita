package vkode.toita.comet

import net.liftweb.common.Loggable
import net.liftweb.http.CometActor
import scalaz.Options
import net.liftweb.http.js.JsCmds.SetHtml
import vkode.toita.backend.{ToitaSessionUser, ToitaRegister}

class FolloweesComet
    extends CometActor with Options with ToitaRegister with ToitaSessionUser with Loggable {

  def render = bind ("f",
                     "list" -> renderFriends)

  override def lowPriority = {
    case list: List[BigInt] => ids = list
  }

  private var ids: List[BigInt] = Nil

  private def rerenderFriends = {
    partialUpdate(SetHtml("list", renderFriends))
    reRender
  }

  private def renderFriends =
    if (ids.isEmpty) <span>No friends!</span>
    else
      <ul>
        { ids.map (id => <li> { id }</li>) }
      </ul>
}
