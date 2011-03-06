package vkode.toita.events

object StreamNode {

  def apply[T <: Treeable](t: T): StreamNode[T] = StreamNode[T](t, Nil)
}

case class StreamNode[T <: Treeable](t: T, subnodes: List[StreamNode[T]]) extends TreeStat[T] {

  lazy val nodeCount: Int = 1 + (subnodes match {
    case Nil => 0
    case list => list map (_.nodeCount) reduceLeft (_ + _)
  })

  lazy val latest: Long = (t.timestamp :: (subnodes flatMap (_.timestamps)) sortWith (_ > _)) (0)

  lazy val names: Set[String] = namesBelow + t.name

  lazy val namesBelow: Set[String] = (subnodes flatMap (_.names)).toSet

  lazy val namesOther: Set[String] = namesBelow - t.name

  lazy val items: List[StreamItem[T]] =
    toItem(0) :: (subnodes flatMap (_.subItems(1)))

  def isDiscussion = !subnodes.isEmpty

  private def subItems(depth: Int): List[StreamItem[T]] =
    toItem(depth) :: (subnodes flatMap (_.subItems(depth + 1)))

  private def toItem(depth: Int) = StreamItem(t, depth, nodeCount, latest, names, namesBelow, namesOther)

  private def timestamps: List[Long] = t.timestamp :: (subnodes flatMap (_.timestamps))
}
