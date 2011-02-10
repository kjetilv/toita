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

class UserStreamTemplatedComet extends UserStream with ToitaCSSComet {

  protected val area = "tweetarea"

  protected override def getNodeSeq: NodeSeq = stream map (_ items) map (_ take 5) map (_ match {
    case Nil => Text("No tweets")
    case items => items map (transformFun (_)) flatMap (_ (defaultXml))
  }) getOrElse <span>Loading...</span>

  private def transformFun(item: StreamItem[TwitterStatusUpdate]): CssBindFunc = item match {
    case StreamItem(twitterStatusUpdate, _, _, _, _, _, _) =>
      twitterStatusUpdate match {
        case TwitterStatusUpdate(TOStatus(sid, text),
                                 meta,
                                 Some(TOUser(uid, screen_name, name, description, imageUrl, deco)),
                                 retweet,
                                 _,
                                 _,
                                 deleted,
                                 _) =>
          "#tweet-img" #> <img src={ imageUrl } alt={ name } width="48" height="48"/> &
          "#tweet-name" #> <span>{name}</span> &
          "#tweet-text" #> (NodeSeq fromSeq (Rendrer textOf twitterStatusUpdate))
      }
    case x =>
      "#tweet-img" #> <span>No logo</span> &
      "#tweet-name" #> Text("No name") &
      "#tweettext" #> Text("No slogan")
  }

  protected def updated {
    partialUpdate(SetHtml(area, getNodeSeq))
    reRender(false)
  }
}
