package vkode.toita.backend

case class UserSession(name: Option[String], token: String, secret: String) {

  override def toString = getClass.getSimpleName + "[" + (name map(_ + ": ") getOrElse "") + token + "/***]"
}
