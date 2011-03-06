package vkode.toita.events

import vkode.toita.events._

case class TStream[T <: Treeable](user: String, tree: Tree[T], items: List[StreamItem[T]]) {

  def takeItems(n: Int) = items take n

  def total = (0 /: items) (_ + _.nodeCount)
}

object TStream {

  def apply[T <: Treeable](user: String,
                           rootIds: List[BigInt],
                           idMap: Map[BigInt, T],
                           children: (BigInt, List[BigInt])*): TStream[T] = {
    apply(user, rootIds, idMap, Map(children: _*))
  }

  def apply[T <: Treeable](user: String,
                           rootIds: List[BigInt],
                           idMap: Map[BigInt, T],
                           children: Map[BigInt, List[BigInt]]): TStream[T] = {
    def nodes (ids: List[BigInt]) = ids map (idMap(_)) map (StreamNode(_))

    def addSubs(node: StreamNode[T]): StreamNode[T] =
      if (children contains node.t.id) node copy (subnodes = nodes (children (node.t.id)) map (addSubs (_)))
      else node

    val tree = Tree(nodes(rootIds) map (addSubs (_)))

    TStream(user, tree, tree.items)
  }
}


