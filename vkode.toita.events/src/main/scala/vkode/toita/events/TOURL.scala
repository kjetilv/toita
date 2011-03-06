package vkode.toita.events

case class TOURL(indices: List[Int],
                 url: String) extends Indexed {
  val text = url
}
