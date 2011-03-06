package vkode.toita.events

trait Indexed {
  val indices: List[Int]
  val text: String

  def a = indices(0)
  def b = indices(1)

  def sameAs (idx: Indexed) = a == idx.a && b == idx.b

  def before(indexed: Indexed) = a < indexed.a
}
