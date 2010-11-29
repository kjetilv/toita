package vkode.toita.backend

import vkode.toita.comet.UserStream
import net.liftweb.json.JsonParser
import collection.mutable.ListBuffer
import actors.Actor
import org.joda.time.DateTime
import net.liftweb.actor.LiftActor
import net.liftweb.http.CometActor
import net.liftweb.json.JsonAST.{JArray, JValue, JObject}

object Updater extends LiftActor {

  val twitterSession = TwitterSession(System getProperty "token", System getProperty "apiSecret")

  val userStreams = ListBuffer[CometActor]()

  protected def messageHandler = {
    case UserStreamUp(actor) => briefed(actor) +=: userStreams
    case UserStreamDown(actor) => userStreams -= actor
  }

  def homeTimeline: String = {
    twitterSession lookup "http://api.twitter.com/1/statuses/home_timeline.json"
  }

  private def briefed (actor: CometActor): CometActor = {
    handleMany (List(actor), homeTimeline)
    actor
  }

  private def handleMany (actors: Iterable[CometActor], line: String) = {
    println("Recv many: " + line)
    JsonParser parseOpt line match {
      case Some(array: JArray) => array.children foreach (handleJson (actors, _))
      case Some(json: JValue) => handleJson (actors, json)
      case None => println("Not many: " + line)
    }
  }

  private def handle (actors: Iterable[CometActor], line: String) = {
    println("Recv: " + line)
    JsonParser parseOpt line map (handleJson (actors, _))
  }

  private def handleJson (actors: Iterable[CometActor], json: JValue) {
    json match {
      case json: JValue =>
        println("Json: " + json)
        val event = JsonTransformer (json)
        if (event.isEmpty) {
          println("No hits!")
        }
        event map (event => {
          println ("Done: " + userStreams.size + " <- " + event)
          actors foreach (_ ! event)
        })
      case x =>
        println ("Unsupported structure: " + x)
    }
  }

  Actor actor {
    for (line <- twitterSession stream "https://userstream.twitter.com/2/user.json") handle (userStreams, line)
  }
}
