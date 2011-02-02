package vkode.toita.comet

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

class UserStreamTemplatedComet extends UserStream {

  private var friends: Option[TwitterFriends] = None

  private def transformFun(item: StreamItem[TwitterStatusUpdate]): CssBindFunc = item match {
    case StreamItem(TwitterStatusUpdate(TOStatus(sid, text),
                                        meta,
                                        Some(TOUser(uid, screen_name, name, _, _, _, imageUrl)),
                                        retweet,
                                        _,
                                        _,
                                        deleted,
                                        _),
                    _, _, _, _, _, _) =>
      "#tweetimg" #> <img src={ imageUrl } alt={ name } width="48" height="48"/> &
      "#tweetname" #> Text(name) &
      "#tweettext" #> Text(text)
    case x =>
      "#tweetimg" #> Text("No logo") &
      "#tweetname" #> Text("No name") &
      "#tweettext" #> Text("No slogan")
  }

  private def renderOption: Option[NodeSeq] ={
    stream map (_ items) map (_ take 5) map (_ match {
      case Nil => Text("Loading")
      case items => items map (transformFun (_)) flatMap (_ (defaultXml))
    })
  }

  override def render = renderOption.get

  protected def updated {
    partialUpdate(SetHtml("tweetarea", renderOption.get))
    reRender(false)
  }
}
