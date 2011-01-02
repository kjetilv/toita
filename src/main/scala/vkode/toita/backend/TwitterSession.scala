package vkode.toita.backend

import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.TwitterApi
import org.apache.http.client.methods.HttpGet
import org.scribe.model.{OAuthConstants, Verb, OAuthRequest, Token}
import org.scribe.extractors.HeaderExtractorImpl
import org.apache.http.message.BasicHeader
import scala.collection.JavaConversions._
import java.io.InputStream
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpResponse
import io.{Codec, Source}
import scalaz.Options

object TwitterSession {

  private val key = System getProperty "key"

  private val sec = System getProperty "secret"

  private val service = new ServiceBuilder provider classOf[TwitterApi] apiKey key apiSecret sec build

  private val requestToken = service.getRequestToken

  private def newOauthRequest(url: String, userSession: UserSession): OAuthRequest = {
    val oauthRequest = new OAuthRequest(Verb.GET, url)
    service.signRequest(new Token(userSession.token, userSession.secret), oauthRequest)
    oauthRequest
  }

  private def newHttpRequest(url: String, oauthRequest: OAuthRequest): HttpGet = {
    val httpRequest = new HttpGet(url)
    httpRequest setHeader new BasicHeader(OAuthConstants.HEADER, new HeaderExtractorImpl extract oauthRequest)
    val params = httpRequest.getParams
    params.setParameter(OAuthConstants.TOKEN, requestToken.getToken)
    oauthRequest.getOauthParameters foreach (_ match {
      case (key, value) => {
        params.setParameter(key, value)
      }
    })
    httpRequest
  }
}

/**
 * Handles connectivity with Twitter.
 */
class TwitterSession (userSession: UserSession) extends Options {
  import TwitterSession._

  def lookup(id: BigInt): String =
    lookup ("http://api.twitter.com/1/statuses/show/" + id + ".json?include_entities=true")

  def homeTimeline =
    lookup ("http://api.twitter.com/1/statuses/home_timeline.json?count=10&include_entities=true")

  def userStream = stream ("https://userstream.twitter.com/2/user.json")

  def lookup(url: String): String = doWithURL (url, getStrings(_))

  def getFriends(ids: List[BigInt]) = lookup("http://api.twitter.com/1/users/lookup.json?user_id=" + (ids mkString ","))

  def stream(url: String): TwitterStream = doWithURL (url, getStream(_))

  private def getStream(inputStream: InputStream) = TwitterStream (lineIterator(inputStream), inputStream)

  private def getStrings (inputStream: InputStream) = lineIterator (inputStream) mkString ("\n")

  private implicit val charset = Codec("US-ASCII")

  private def doWithURL[T](url: String, fun: InputStream => T): T =
    fun (response (authorizedRequest(url)).getEntity.getContent)

  private def authorizedRequest(url: String) = newHttpRequest (url, newOauthRequest (url, userSession))

  private def response(url: HttpGet): HttpResponse = new DefaultHttpClient execute url

  private def lineIterator(stream: InputStream): Iterator[String] =
    (Source fromInputStream stream).getLines filterNot(_ == null) map (_.trim) filterNot (_.isEmpty)
}
