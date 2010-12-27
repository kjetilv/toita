package vkode.toita.backend

trait Treeable {
  def id: BigInt
  def name: String
  def timestamp: Long
}

trait TreeStat[T <: Treeable] {
  def nodeCount: Int
  def latest: Long
  def names: List[String]
  def items: List[ConversationItem[T]]
}

object ConversationNode {

  def apply[T <: Treeable](t: T): ConversationNode[T] = ConversationNode[T](t, Nil)
}

case class ConversationItem[T](t: T, indent: Int)

case class ConversationNode[T <: Treeable](t: T, subnodes: List[ConversationNode[T]]) extends TreeStat[T] {

  def nodeCount: Int = 1 + (subnodes match {
    case Nil => 0
    case list => list map (_.nodeCount) reduceLeft (_ + _)
  })

  def latest: Long = (t.timestamp :: (subnodes flatMap (_.timestamps)) sortWith (_ > _)) (0)

  def names: List[String] = t.name :: (subnodes flatMap (_.names))

  def items: List[ConversationItem[T]] = ConversationItem(t, 0) :: (subnodes flatMap (_.subItems(1)))

  private def subItems(depth: Int): List[ConversationItem[T]] =
    ConversationItem(t, depth) :: (subnodes flatMap (_.subItems(depth + 1)))

  private def timestamps: List[Long] = t.timestamp :: (subnodes flatMap (_.timestamps))
}

case class Tree[T <: Treeable](nodes: List[ConversationNode[T]]) extends TreeStat[T] {

  def nodeCount = nodes match {
    case Nil => 0
    case nodes => nodes map (_.nodeCount) reduceLeft (_ + _)
  }

  def latest = nodes match {
    case Nil => 0
    case nodes => (nodes map (_.latest) sortWith (_ > _)) (0)
  }

  def names = nodes flatMap (_.names)

  def items = nodes flatten (_.items)
}

case class TreeBuilder[T <: Treeable](roots: List[BigInt],
                                      statusMap: Map[BigInt, T],
                                      repliesTo: Map[BigInt, List[BigInt]]) {

  private def nodes (ids: List[BigInt]) = ids map (statusMap(_)) map (ConversationNode(_))

  private def addSubs(node: ConversationNode[T]): ConversationNode[T] = if (repliesTo contains node.t.id) {
    node copy (subnodes = nodes (repliesTo (node.t.id)) map (addSubs (_)))
  } else {
    node
  }

  def build: Tree[T] = Tree(nodes(roots) map (addSubs (_)))
}
