package vkode.toita.backend

import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import xml.{Elem, NodeSeq, Text}

object Rendrer {

  implicit def date2Yoda(date: Date) = new DateTime(date)

  private val formatter = DateTimeFormat forPattern ("yyyy-MM-dd hh:mm:ss")

  private def time(dt: DateTime) = formatter print dt

  private var loadCount = 0

  def render(friendIds: Option[List[BigInt]]) =
    <span> { friendIds map (_ mkString ",") getOrElse "No friends!" } </span>

  def render (statuses: List[StreamItem[TwitterStatusUpdate]]) =
    <table border="2" rules="rows">
      { bulletPoints (statuses) }
    </table>

  private def bulletPoints(statuses: List[StreamItem[TwitterStatusUpdate]]): List[NodeSeq] =
    statuses flatMap (row (_))

  private def row (event: StreamItem[TwitterStatusUpdate]): List[NodeSeq] =
    try {
      Rendrer render event
    } catch {
      case e => {
        e.printStackTrace()
        List(<tr><td>Bad render: { event }</td></tr>)
      }
    }

  def renderSmall(user: TOUser) =
    <span><img src={user.profile_image_url} height="12" width="12"/> {user.screen_name}</span>

  def render(item: StreamItem[TwitterStatusUpdate]): List[NodeSeq] =
    item match {
      case StreamItem(TwitterStatusUpdate(status, meta,
                                          Some(TOUser(_, screen_name, name, _, imageUrl, _)),
                                          retweeted, entities, reply, deleted, json),
                      indent,
                      _, _, _, _, _) =>
        val arrows = (1 to indent).toList.map(x => <span>&#8618;</span>)
        val spaces = (1 to indent).toList.map(x => <span>&nbsp;&nbsp;</span>)
        List(<tr>
               <td width="20" align="right">
                 { spaces } { img(imageUrl, math.max(12, 48 - (indent * 8))) }
               </td>
               <td width="200" valign="top">
                 { arrows :+ textOf(retweeted getOrElse item.t) }
               </td>
             </tr>,
             <tr>
               <td></td>
               <td>
                 {arrows} { screen_name } @ { time(meta.created_at) } [{ status.id }]
                 { retweeted match {
                 case Some(TwitterStatusUpdate
                               (TOStatus(id, _),
                                TOMeta(_, _, _, _, _, date, _),
                                Some(TOUser(_, screen_name, name, _, _, _)),
                                _,
                                _,
                                _,
                                _,
                                _)) =>
                   <span><br/>{arrows} Retweeted by { screen_name + "/" + name } @ { time(date) } ({ id })</span>
                 case _ =>
                     <span/>
               }}
               </td>
             </tr>) ++ (if (item.nodeCount > 1)
          <tr>
            <td></td>
            <td>
              {item.nodeCount - 1}
              { if (item.nodeCount > 2) "replies" else "reply" }
              downstream: { (item.namesOther) mkString ", "}</td>
          </tr>
        else Nil)
      case _ => List(<span>Not parsed: {item}</span>)
    }

  private def img(url: String, size: Int = 24) =
      <img src={url} width={ "" + size } height={ "" + size }/>

  private case class Insert(a: Int, b: Int, node: NodeSeq) {
    def before(insert: Insert) = a < insert.a
  }

  private case class TextIndex(override val a: Int,
                               override val b: Int,
                               text: String) extends Indexed {
    override val indices = List(a, b)
  }

  private def textInsert(text: String, col: Option[String], idx: (Int, Int)) = idx match {
    case (a, b) =>
      val style = "color:#" + (col getOrElse "333333")
      Insert (a, b, NodeSeq fromSeq <span>{text.substring(a, b)}</span>)
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
      case TOUser(_, screenName, _, _, _, Some(TOUserDecoration(backgr, backgrUrl, textCol, linkCol))) =>
        val userStyle = "color:#" + (linkCol getOrElse "333333")
        (<span style={ userStyle }>{ screenName }</span>).toList ++ (inserts(status, reply, textCol, linkCol, indexeds: _*) map (_.node))
    }

  private def inserts(status: TOStatus,
                      reply: Option[TOReply],
                      textCol: Option[String],
                      linkCol: Option[String],
                      indexeds: List[Indexed]*): List[Insert] = {
    val entityIndices = (List[Indexed]() /: indexeds) (_ ++ _) sortWith (_ before _)
    val replyIndex = indexed(reply, status.text)
    val entityInserts = resolveEntities (replyIndex, entityIndices) map (entityInsert (_, linkCol))
    val textIndices = computeTextIndices (status.text, entityIndices, entityInserts)
    val textInserts = textIndices map (textInsert (status.text, textCol, _))
    (entityInserts ++ textInserts) sortWith (_ before _)
  }

  def deleteFlag(deleted: Boolean): Elem = (if (deleted) <span>[DELETED]</span> else <span/>)

  def textOf(tsu: TwitterStatusUpdate): Elem =
    <span> {
      tsu match {
        case TwitterStatusUpdate(status, meta, Some(usr), _, TOEntities(hashtags, mentions, urls), reply, deleted, _) =>
          nodes(usr, status, reply, hashtags, mentions, urls) :+ deleteFlag(deleted)
        case _ => <span/>
      }
      }
    </span>

  def textRender(tsu: TwitterStatusUpdate): Elem =
    <span> {
      tsu match {
        case TwitterStatusUpdate(status,
                                 meta,
                                 Some(user),
                                 /*                                 Some(TOUser(id,
           screenName,
           name,
           description,
           profileImage,
           Some(TOUserDecoration(useBackgroundImage,
                                 backgroundImageUrl,
                                 profileTextColor,
                                 profileLinkColor)))),*/
                                 rt,
                                 TOEntities(hashtags, mentions, urls),
                                 reply,
                                 deleted,
                                 json) =>
          rt match {
            case Some(TwitterStatusUpdate(rtStatus,
                                          rt,
                                          Some(rtUser),
                                          /*                                          Some(TOUser(rtId,
                    rtScreenName,
                    rtName,
                    rtDescription,
                    rtProfileImage,
                    Some(TOUserDecoration(rtUseBackgroundImage,
                                          rtBackgroundImageUrl,
                                          rtProfileTextColor,
                                          rtProfileLinkColor)))),*/
                                          _,
                                          TOEntities(rtHashtags,
                                                     rtMentions,
                                                     rtUrls),
                                          rtReply,
                                          rtDeleted,
                                          _)) =>
              (nodes(rtUser,
                     rtStatus,
                     rtReply,
                     rtHashtags,
                     rtMentions,
                     rtUrls) :+ deleteFlag(rtDeleted))
            case None =>
              (nodes(user,
                     status,
                     reply,
                     hashtags,
                     mentions,
                     urls) :+ deleteFlag(deleted))
          }
        case _ => <span/>
      }
      }
    </span>
  //
  //  private def user(usr: Option[TOUser]) = usr match {
  //    case Some(usr) => <strong>{ usr.screen_name } </strong>
  //    case None => <span/>
  //  }

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

  private def entityInsert (entity: Indexed, linkCol: Option[String]) = {
    val color = "color:#" + (linkCol getOrElse "3333FF")
    Insert(entity.a, entity.b, entity match {
      case TOMention(_, name, id, _) =>
        <a style={color} name={name} href={"http://twitter.com/" + id}>@{id}</a>
      case TOURL(_, url) =>
        <a style={color} href={url}>{url}</a>
      case TOHashtag(_, text) =>
        <a style={color} name={text} href={"http://twitter.com/search?q=%23" + text}>#{text}</a>
    })
  }
}
