package vkode.toita.events

case class UserSession(user: Option[String], token: String, secret: String) {

  override def toString = getClass.getSimpleName + "[" + (user map(_ + ": ") getOrElse "") + token + "/***]"
}
