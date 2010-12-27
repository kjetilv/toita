package vkode.toita.backend

import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import xml.{Node, NodeSeq, Text}

object Rendrer {

  implicit def date2Yoda(date: Date) = new DateTime(date)

  private val formatter = DateTimeFormat forPattern ("yyyy-MM-dd hh:mm:ss")

  private def time(dt: DateTime) = formatter print dt

  private var loadCount = 0

  def render(friendIds: Option[List[BigInt]])=
    <span> { friendIds map (_ mkString ",") getOrElse "No friends!" } </span>

  def renderStatuses (statuses: List[TwitterStatusUpdate]) = render (statuses map (RenderableStatus(_, 0)))

  def render (statuses: List[RenderableStatus]) =
    <table border="2" rules="rows">
      { bulletPoints (statuses) }
    </table>

  private def bulletPoints(statuses: List[RenderableStatus]): List[NodeSeq] = statuses flatMap (row (_))

  private def row (event: RenderableStatus): List[NodeSeq] =
    try {
      Rendrer render event
    } catch {
      case e => {
        e.printStackTrace()
        List(<tr><td>Bad render: { event }</td></tr>)
      }
    }

  def render(renderableStatus: RenderableStatus): List[Node]=
    renderableStatus match {
      case RenderableStatus(TwitterStatusUpdate(status, meta, Some(user), entities, reply, _), indent) =>
        val spaces = (1 to indent).toList.map((x: Int) => <span>&nbsp;&nbsp;</span>)
        val arrows = (1 to indent).toList.map((x: Int) => <span>&#8618;</span>)
        List(<tr>
               <td width="50">
                 { spaces }
                 <img src={user.profile_image_url} width="48" height="48"/>
               </td>
               <td width="300">
                 { arrows :+ textOf(renderableStatus.update) }
               </td>
             </tr>,
             <tr>
               <td>{spaces} { user.screen_name}</td>
               <td>{arrows} { user.name } @ { time(status.created_at)} ({ status.id })</td>
             </tr>)
      case _ => List(<span>Not parsed: {renderableStatus}</span>)
    }

  private case class Insert(a: Int, b: Int, node: NodeSeq) {
    def before(insert: Insert) = a < insert.a
  }

  private case class TextIndex(override val a: Int,
                               override val b: Int,
                               text: String) extends Indexed {
    override val indices = List(a, b)
  }

  private def textInsert(text: String, idx: (Int, Int)) = idx match {
    case (a, b) => Insert (a, b, Text (text.substring(a, b)))
  }

  def computeTextIndices (text: String, indexed: List[Indexed], inserts: List[Insert]): List[(Int, Int)] =
    indexed match {
      case Nil => List(0 -> text.length)
      case single :: Nil =>
        List((0 -> single.a), (single.b -> text.length))
      case indexed => prefixed(text,
                               inserts,
                               suffixed(text,
                                        inserts,
                                        indexed map (_.b) zip (indexed.tail map (_.a))))
    }

  private def textOf(tsu: TwitterStatusUpdate) = {
    tsu match {
      case TwitterStatusUpdate(status, _, _, TOEntities(Nil, Nil, Nil), None, _) =>
        <span>
          { status.text }
        </span>
      case TwitterStatusUpdate(status, _, _, TOEntities(hashtags, mentions, urls), reply, _) =>
        val entityIndices = (hashtags ++ mentions ++ urls) sortWith (_ before _)
        val replyIndex = indexed(reply, status.text)
        val entityInserts = resolveEntities (replyIndex, entityIndices) map (entityInsert _)
        val textIndices = computeTextIndices (status.text, entityIndices, entityInserts)
        val textInserts = textIndices map (textInsert (status.text, _))
        val allInserts = (entityInserts ++ textInserts) sortWith (_ before _)
        <span>
          { allInserts map (_.node) }
        </span>
      case _ => <span/>
    }
  }

  private def resolveEntities(replyIndex: Option[TOMention], indices: List[Indexed]) = (replyIndex -> indices) match {
    case (None, indices) => indices
    case (Some(mention), indices) if (indices exists (_ sameIndexAs mention)) => indices
    case (Some(mention), indices) => mention :: indices
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

  private def entityInsert (entity: Indexed) = Insert(entity.a, entity.b, entity match {
    case TOMention(_, name, id, _) => <a name={name} href={"http://twitter.com/" + id}>@{id}</a>
    case TOURL(_, url) => <a href={url}>{url}</a>
    case TOHashtag(_, text) => <a name={text} href={"http://twitter.com/search?q=%23" + text}>#{text}</a>
  })
}
