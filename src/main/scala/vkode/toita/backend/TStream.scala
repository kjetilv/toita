package vkode.toita.backend

object TStream {

  def apply[T <: Treeable](roots: List[BigInt],
                           tMap: Map[BigInt, T],
                           children: Map[BigInt, List[BigInt]]): TStream[T] = {
    val tree = TreeBuilder(roots, tMap, children).build
    TStream(tree, tree.items)
  }
}

case class TStream[T <: Treeable](tree: Tree[T], items: List[StreamItem[T]]) {

  def total = (0 /: items) (_ + _.nodeCount)
}

trait Treeable {
  def id: BigInt
  def name: String
  def timestamp: Long
}

trait TreeStat[T <: Treeable] {
  def nodeCount: Int
  def latest: Long
  def names: Set[String]
  def namesBelow: Set[String]
  def namesOther: Set[String]
  def items: List[StreamItem[T]]
}

case class StreamItem[T <: Treeable](t: T, indent: Int, nodeCount: Int, latest: Long,
                                     names: Set[String], namesBelow: Set[String], namesOther: Set[String])
    extends TreeStat[T] {
  def items = List(this)
}

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

case class Tree[T <: Treeable](nodes: List[StreamNode[T]]) extends TreeStat[T] {

  lazy val nodeCount = nodes match {
    case Nil => 0
    case nodes => nodes map (_.nodeCount) reduceLeft (_ + _)
  }

  lazy val latest = nodes match {
    case Nil => 0
    case nodes => (nodes map (_.latest) sortWith (_ > _)) (0)
  }

  lazy val names = (nodes flatten (_.names)).toSet

  lazy val namesOther = namesBelow

  lazy val namesBelow = names

  lazy val items = nodes flatten (_.items)
}

case class TreeBuilder[T <: Treeable](roots: List[BigInt],
                                      tMap: Map[BigInt, T],
                                      children: Map[BigInt, List[BigInt]]) {

  private def nodes (ids: List[BigInt]) = ids map (tMap(_)) map (StreamNode(_))

  private def addSubs(node: StreamNode[T]): StreamNode[T] =
    if (children contains node.t.id) node copy (subnodes = nodes (children (node.t.id)) map (addSubs (_)))
    else node

  def build: Tree[T] = Tree(nodes(roots) map (addSubs (_)))
}
