package vkode.toita.backend

import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.TwitterApi
import org.apache.http.client.methods.HttpGet
import org.scribe.model.{OAuthConstants, Verb, OAuthRequest, Token}
import org.scribe.extractors.HeaderExtractorImpl
import org.apache.http.message.BasicHeader
import xml.XML
import scala.collection.JavaConversions._
import java.io.{Reader, LineNumberReader, InputStreamReader, InputStream}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpResponse
import io.{Codec, Source}

object TwitterSession {

  private def authorizedRequest(token: String, secret: String, url: String) =
    newHttpRequest (url, newOauthRequest (url, token, secret))

  private val key = System getProperty "key"

  private val sec = System getProperty "secret"

  private val service = new ServiceBuilder provider classOf[TwitterApi] apiKey key apiSecret sec build

  private val requestToken = service.getRequestToken

  private def newOauthRequest(url: String, token: String, secret: String): OAuthRequest = {
    val oauthRequest = new OAuthRequest(Verb.GET, url)
    service.signRequest(new Token(token, secret), oauthRequest)
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

case class TwitterSession (token: String, secret: String) {
  import TwitterSession._

  implicit val charset = Codec("US-ASCII")

  implicit def urlToAuthorizedRequest(url: String) = authorizedRequest(token, secret, url)

  def fromStream[T](inputStream: InputStream, fun: InputStream => T) =
    try fun(inputStream) finally inputStream close

  def readLine (reader: LineNumberReader): String = {
    val line = try
    {
      reader.readLine
    } catch {
      case e =>
        e.printStackTrace
        null
    }
    if (line == null) {
      try {
        reader.close
      }
      catch {
        case e =>
          e.printStackTrace
      }
    }
    line
  }

  def lookup(url: String): String =
    doWithURL (url, lineIterator(_)) mkString ("\n")

  def stream(url: String): Iterator[String] =
    doWithURL (url, lineIterator(_) filterNot (empty(_)) takeWhile (_ != null))

  private def doWithURL[T](url: String, fun: InputStream => T): T = fun (response (url).getEntity.getContent)

  private def response(url: HttpGet): HttpResponse = new DefaultHttpClient execute url

  private def lineIterator(stream: InputStream): Iterator[String] = (Source fromInputStream stream) getLines

  private def empty(line: String) = line == null || line.trim.isEmpty
}
