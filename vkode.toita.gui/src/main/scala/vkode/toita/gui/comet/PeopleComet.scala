package vkode.toita.gui.comet

import scala.xml._
import vkode.toita.gui.backend._
import net.liftweb.http.js.JsCmds._
import akka.actor.Actor
import vkode.toita.events._

class PeopleComet extends ToitaCSSComet with ToitaRegister with ToitaTrackable {

  override val eventTypes = classOf[TwitterFriends] :: classOf[TwitterFriend] :: Nil

  override def tracker(twitterService: TwitterService) = Some(Actor.actorOf(new PeopleTracker(twitterService)))

  private var users = Map[BigInt,TOUser]()

  override def lowPriority = {
    case newUsers: List[TOUser] =>
      users = (users /: newUsers) (withUser(_, _))
      partialUpdate(SetHtml("people-area", getNodeSeq))
      reRender(false)
    case user: TOUser =>
      users = withUser(users, user)
       partialUpdate(SetHtml("people-area", getNodeSeq))
      reRender(false)
  }

  private def withUser(m: Map[BigInt, TOUser], u: TOUser) =
    if (m contains u.data.id) m else m + (u.data.id -> u)

  protected override def getNodeSeq: NodeSeq =
    if (users.isEmpty) <span>No friends!</span> else (Rendrer renderUsers (users.values.toList, defaultXml))
}
