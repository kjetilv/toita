package vkode.toita.events

trait TreeStat[T <: Treeable] {
  def nodeCount: Int
  def latest: Long
  def names: Set[String]
  def namesBelow: Set[String]
  def namesOther: Set[String]
  def items: List[StreamItem[T]]
}















