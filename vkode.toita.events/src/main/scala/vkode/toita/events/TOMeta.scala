package vkode.toita.events

import java.util.Date

case class TOMeta(retweeted: Boolean,
                  retweeted_count: Option[BigInt],
                  truncated: Boolean,
                  source: String,
                  favorited: Boolean,
                  created_at: Date,
                  contributors: String){

  override def productElement(n: Int) = null
}

object TOMeta {

  val Null = TOMeta(false, None, false, "Unknown Source", false, new Date(0), "")
}
