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

  private def bindItem(item: StreamItem[TwitterStatusUpdate]): NodeSeq = item match {
    case StreamItem(TwitterStatusUpdate(TOStatus(sid, text),
                                        meta,
                                        Some(TOUser(uid, screen_name, name, _, _, _, imageUrl)),
                                        retweet,
                                        _,
                                        _,
                                        deleted,
                                        _),
                    _, _, _, _, _, _) =>
      bind("us", defaultXml,
           "img" -> <img src={ imageUrl } alt={ name } width="48" height="48"/>,
           "name" -> Text(name),
           "text" -> Text(text))
    case x => <span>Ooops: {x.toString}</span>
  }

  private def renderStream(items: List[StreamItem[TwitterStatusUpdate]]): NodeSeq = items flatMap (bindItem _)

  private def renderOption: NodeSeq = stream map (_ items) map (_ take 5) map (renderStream _) getOrElse Text("Loading ...")

  override def render = renderOption

  protected def updated {
    partialUpdate(SetHtml("tweetarea", renderOption))
    reRender(false)
  }
}
