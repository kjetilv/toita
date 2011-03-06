package vkode.toita.events

case class UserDeco(profile_image_url: Option[String],
                    profile_use_background_image: Boolean,
                    profile_background_image_url: Option[String],
                    profile_text_color: Option[String],
                    profile_link_color: Option[String],
                    profile_sidebar_fill_color: Option[String],
                    profile_sidebar_border_color: Option[String]) {

  def tweetStyle = "padding:2;" +
                   " border-width:1;" +
                   " border-color:#" + profile_sidebar_border_color.get + ";" +
                   " border-style:solid;" +
                   " color:#" + profile_text_color.get //+ ";" +
                   //" background-color:#" + profile_sidebar_fill_color.get

  def textColor = profile_text_color getOrElse "333333"

  def textStyle = "color:#" + textColor

  def linkColor = profile_link_color getOrElse "3333DD"

  def linkStyle = "color:#" + linkColor
}
