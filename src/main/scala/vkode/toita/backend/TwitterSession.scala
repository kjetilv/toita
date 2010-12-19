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
import io.{BufferedSource, Codec, Source}
import vkode.toita.comet.UserSession

object TwitterSession {

  private def authorizedRequest(userSession: UserSession, url: String) =
    newHttpRequest (url, newOauthRequest (url, userSession))

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
 * Handles connectivity with Twitter
 */
case class TwitterSession (userSession: UserSession) {
  import TwitterSession._

  implicit val charset = Codec("US-ASCII")

  implicit def urlToAuthorizedRequest(url: String) = authorizedRequest(userSession, url)

  def lookup(url: String): String =
    doWithURL (url, lineIterator(_, false)) mkString ("\n")

  def stream(url: String): Iterator[String] =
    doWithURL (url, lineIterator(_, true) filterNot (empty(_)) takeWhile (_ != null))

  def close {
    activeSource map (_ close)
  }

  private var activeSource: Option[BufferedSource] = None

  private def doWithURL[T](url: String, fun: InputStream => T): T =
    fun (response (url).getEntity.getContent)

  private def response(url: HttpGet): HttpResponse = new DefaultHttpClient execute url

  private def openSource(stream: InputStream, mainStream: Boolean) =
    if (mainStream) activeSource match {
      case None => {
        val source = Source fromInputStream stream
        activeSource = Option(source)
        source
      }
      case Some(source) => error("Source already set: " + source)
    } else {
      Source fromInputStream stream
    }

  private def lineIterator(stream: InputStream, mainStream: Boolean): Iterator[String] =
    openSource(stream, mainStream).getLines

  private def empty(line: String) = line == null || line.trim.isEmpty
}
