package vkode.toita.gui.backend

import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import net.liftweb.util._
import Helpers._
import xml.{Elem, NodeSeq}
import vkode.toita.events._

object Rendrer {

  def renderUsers(items: List[TOUser], xml: NodeSeq): NodeSeq =
    NodeSeq fromSeq (items map (transformFun (_)) flatMap (_ (xml)) toList)

  def renderStatusStream(items: List[StreamItem[TwitterStatusUpdate]], xml: NodeSeq): NodeSeq =
    items map (transformFun _) flatMap (_ (xml))

  private implicit def date2Yoda(date: Date) = new DateTime(date)

  private val formatter = DateTimeFormat forPattern ("yyyy-MM-dd hh:mm:ss")

  private def time(dt: DateTime) = formatter print dt

  private var loadCount = 0

  private val emptyUserRender = "#tweet-img" #> <span>No logo</span> &
                                "#tweet-name" #> <span>No name</span> &
                                "#tweet-text" #> <span>No slogan</span> &
                                "#tweet-retweeter" #> <span/>

  private def transformFun(user: TOUser): (NodeSeq => NodeSeq) =
    user match {
      case TOUser(UserData(id, screenName, name, desc), deco) =>
        val replacer =
          "#people-name" #> screenNameLink (name, screenName, deco) &
          "#people-img" #> img(deco, name)
        (xml: NodeSeq) =>
          <div style={deco.tweetStyle}>
            { replacer(xml) }
            <br/>
          </div>
    }

  private def transformFun(item: StreamItem[TwitterStatusUpdate]): (NodeSeq => NodeSeq) = item match {
    case StreamItem(TwitterStatusUpdate(_, _, user, Some(retweet), _, _, _, _, _) , indent, _, _, _, _, _) =>
      renderTweet(retweet, indent, Some(user))
    case StreamItem(tweet, indent, _, _, _, _, _) =>
      renderTweet(tweet, indent, None)
    case x =>
      emptyUserRender
  }

  private def renderTweet(twitterStatusUpdate: TwitterStatusUpdate,
                          indent: Int,
                          retweeter: Option[TOUser]) =
    twitterStatusUpdate match {
      case TwitterStatusUpdate(TOStatus(sid, text),
                               meta,
                               TOUser(UserData(uid, screenName, name, description), deco),
                               _,
                               _,
                               _,
                               deleted,
                               _,
                               _) => val replacer =
        "#tweet-img" #> img(deco.profile_image_url,name) &
        "#tweet-name" #> screenNameLink (name, screenName, deco) &
        "#tweet-text" #> (NodeSeq fromSeq (Rendrer renderText twitterStatusUpdate)) &
        "#tweet-retweeter" #> (retweeter match {
          case Some(TOUser(UserData(_, screenName, name, _), deco)) => retweetedScreenNameLink(name, screenName, deco)
          case _ => <span/>
        })
        (xml: NodeSeq) =>
          <div style={deco.tweetStyle}>
            { if (indent > 1) NodeSeq fromSeq (0 to indent map (i => <span>&#8618;</span>)) else <span/> }
            { replacer(xml) }
            <br/>
          </div>
    }

  private def renderText(tsu: TwitterStatusUpdate): Elem =
    <span> {
      tsu match {
        case TwitterStatusUpdate(status, meta, user, _, TOEntities(hashtags, mentions, urls), reply, deleted, _, _) =>
          nodes(user, status, reply, hashtags, mentions, urls) :+ deleteFlag(deleted)
        case _ => <span/>
      }
      } </span>

  private case class Insert(a: Int, b: Int, node: NodeSeq) {

    def before(insert: Insert) = a < insert.a
  }

  private case class TextIndex(override val a: Int,
                               override val b: Int,
                               text: String) extends Indexed {

    override val indices = List(a, b)
  }

  private def textInsert(text: String, deco: UserDeco, idx: (Int, Int)) = idx match {
    case (a, b) =>
      Insert (a, b, NodeSeq fromSeq <span style={ deco.textStyle }>{text.substring(a, b)}</span>)
  }

  private def computeTextIndices (text: String, indexed: List[Indexed], inserts: List[Insert]): List[(Int, Int)] =
    indexed match {
      case Nil => List(0 → text.length)
      case single :: Nil =>
        List((0 → single.a), (single.b → text.length))
      case indexed => prefixed(text,
                               inserts,
                               suffixed(text,
                                        inserts,
                                        indexed map (_.b) zip (indexed.tail map (_.a))))
    }

  private def nodes(user: TOUser, status: TOStatus, reply: Option[TOReply], indexeds: List[Indexed]*): List[NodeSeq] =
    user match {
      case TOUser(UserData(_, screenName, _, _), deco) =>
        (<span style={ deco.linkStyle }>{ screenName }</span>).toList ++ (inserts(status, reply, deco, indexeds: _*) map (_.node))
    }

  private def inserts(status: TOStatus,
                      reply: Option[TOReply],
                      deco: UserDeco,
                      indexeds: List[Indexed]*): List[Insert] = {
    val entityIndices = (List[Indexed]() /: indexeds) (_ ++ _) sortWith (_ before _)
    val replyIndex = indexed(reply, status.text)
    val entityInserts = resolveEntities (replyIndex, entityIndices) map (entityInsert (_, deco))
    val textIndices = computeTextIndices (status.text, entityIndices, entityInserts)
    val textInserts = textIndices map (textInsert (status.text, deco, _))
    (entityInserts ++ textInserts) sortWith (_ before _)
  }

  private def deleteFlag(deleted: Boolean): Elem = (if (deleted) <span>[DELETED]</span> else <span/>)

  private def resolveEntities(replyIndex: Option[TOMention], indices: List[Indexed]) =
    (replyIndex → indices) match {
      case (None, indices) =>
        indices
      case (Some(reply), indices) => if (indices exists (_ sameAs reply)) indices else reply :: indices
    }

  private def indexed(reply: Option[TOReply], text: String): Option[TOMention] = reply match {
    case None => None
    case Some(TOReply(null, null, null)) => None
    case Some(reply) => Some(TOMention(reply.in_reply_to_user_id,
                                       "",
                                       reply.in_reply_to_screen_name,
                                       List(0, reply.in_reply_to_screen_name.length + 1)))
  }

  private def prefixed (text: String, inserts: List[Insert], idxs: List[(Int, Int)]) =
    (inserts, idxs) match {
      case (insert :: _, (x, _) :: _) if (x > 0) =>
        (0, insert.a) :: idxs
      case _ => idxs
    }

  private def suffixed (text: String, inserts: List[Insert], idxs: List[(Int, Int)]) =
    (text.length, inserts.reverse) match {
      case (lastIdx, Insert(_, lastInsertIdx, _) :: _) if (lastIdx != lastInsertIdx) =>
        idxs :+ (lastInsertIdx, lastIdx)
      case _ => idxs.reverse
    }

  private def entityInsert (entity: Indexed, deco: UserDeco) =
    Insert(entity.a, entity.b, entity match {
      case TOMention(_, name, screenName, _) => screenNameLink(name, screenName, deco)
      case TOURL(_, url) => urlLink(url, deco)
      case TOHashtag(_, text) => hashTag(text, deco)
    })

  private def hashTag(tag: String, deco: UserDeco): NodeSeq =
    <a style={ deco.linkStyle } name={ tag } href={ "http://twitter.com/search?q=%23" + tag }>#{ tag }</a>

  private def img(deco: UserDeco, alt: String): NodeSeq = img(deco.profile_image_url, alt)

  private def img(url: Option[String], alt: String): NodeSeq = img(url getOrElse "no image", alt)

  private def img(url: String, alt: String): NodeSeq =
      <img src={ url } alt={ alt }/>

  private def urlLink(url: String, deco: UserDeco): NodeSeq =
    <a style={ deco.linkStyle } href={ url }>{ url }</a>

  private def retweetedScreenNameLink(name: String, screenName: String, deco: UserDeco): NodeSeq =
    <span style={ deco.textStyle }>Retweeted by { screenNameLink(name, screenName, deco) }</span>

  private def screenNameLink(name: String, screenName: String, deco: UserDeco): NodeSeq =
    <a style={ deco.linkStyle } name={ name } href={ "http://twitter.com/" + screenName }>@{ screenName }</a>
}
