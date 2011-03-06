package vkode.toita.events

case class StreamItem[T <: Treeable](t: T, indent: Int, nodeCount: Int, latest: Long,
                                     names: Set[String], namesBelow: Set[String], namesOther: Set[String])
    extends TreeStat[T] {
  def items = List(this)
}















