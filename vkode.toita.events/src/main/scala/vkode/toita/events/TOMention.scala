package vkode.toita.events

case class TOMention(id: BigInt,
                     name: String,
                     screen_name: String,
                     indices: List[Int]) extends Indexed {
  val text = screen_name
}
