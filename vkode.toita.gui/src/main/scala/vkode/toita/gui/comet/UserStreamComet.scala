package vkode.toita.gui.comet

import scala.xml._
import scala.collection.mutable.{Map => MutMap}
import vkode.toita.gui.backend._
import net.liftweb.http.js.JsCmds._
import akka.actor.Actor
import vkode.toita.events._

class UserStreamComet extends ToitaCSSComet with ToitaRegister with ToitaTrackable {

  override val eventTypes = classOf[TwitterStatusUpdate] :: classOf[TwitterStatusDelete] :: Nil

  override def tracker(twitterService: TwitterService) = Some(Actor actorOf (new StatusTracker(twitterService)))

  private def tweetCount = 10

  protected var streams = MutMap[String,TStream[TwitterStatusUpdate]]()

  private def renderedItems: List[StreamItem[TwitterStatusUpdate]]  = {
    val items: List[StreamItem[TwitterStatusUpdate]] = streams.values.toList flatMap (_ takeItems tweetCount)   
    items sortWith (_.t.id < _.t.id) take (tweetCount)
  }
  
  protected override def getNodeSeq: NodeSeq = renderedItems match {
    case Nil => <span>No tweets</span>
    case items => Rendrer renderStatusStream (items, defaultXml)
  }

  override def lowPriority: PartialFunction[Any, Unit] = {
    case update: TStream[TwitterStatusUpdate] =>
      streams (update.user) = update
      partialUpdate(SetHtml("tweet-area", getNodeSeq))
      reRender(false)
  }

  override def toString = getClass.getSimpleName + "[name:" + name + " " + System.identityHashCode(this) + "]"
}
