package vkode.toita.backend

object Conversation {

  def apply[T <: Treeable](roots: List[BigInt],
                           tMap: Map[BigInt, T],
                           children: Map[BigInt, List[BigInt]]): Conversation[T] = {
    val tree = TreeBuilder(roots, tMap, children).build
    Conversation(tree, tree.items)
  }
}

case class Conversation[T <: Treeable](tree: Tree[T], items: List[ConversationItem[T]]) {

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
  def items: List[ConversationItem[T]]
}

case class ConversationItem[T <: Treeable](t: T, indent: Int, nodeCount: Int, latest: Long,
                                           names: Set[String], namesBelow: Set[String], namesOther: Set[String])
    extends TreeStat[T] {
  def items = List(this)
}

object ConversationNode {

  def apply[T <: Treeable](t: T): ConversationNode[T] = ConversationNode[T](t, Nil)
}

case class ConversationNode[T <: Treeable](t: T, subnodes: List[ConversationNode[T]]) extends TreeStat[T] {

  lazy val nodeCount: Int = 1 + (subnodes match {
    case Nil => 0
    case list => list map (_.nodeCount) reduceLeft (_ + _)
  })

  lazy val latest: Long = (t.timestamp :: (subnodes flatMap (_.timestamps)) sortWith (_ > _)) (0)

  lazy val names: Set[String] = namesBelow + t.name

  lazy val namesBelow: Set[String] = (subnodes flatMap (_.names)).toSet

  lazy val namesOther: Set[String] = namesBelow - t.name

  lazy val items: List[ConversationItem[T]] =
    toItem(0) :: (subnodes flatMap (_.subItems(1)))

  private def subItems(depth: Int): List[ConversationItem[T]] =
    toItem(depth) :: (subnodes flatMap (_.subItems(depth + 1)))

  private def toItem(depth: Int) = ConversationItem(t, depth, nodeCount, latest, names, namesBelow, namesOther)

  private def timestamps: List[Long] = t.timestamp :: (subnodes flatMap (_.timestamps))
}

case class Tree[T <: Treeable](nodes: List[ConversationNode[T]]) extends TreeStat[T] {

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

  private def nodes (ids: List[BigInt]) = ids map (tMap(_)) map (ConversationNode(_))

  private def addSubs(node: ConversationNode[T]): ConversationNode[T] =
    if (children contains node.t.id) node copy (subnodes = nodes (children (node.t.id)) map (addSubs (_)))
    else node

  def build: Tree[T] = Tree(nodes(roots) map (addSubs (_)))
}
