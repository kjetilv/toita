package vkode.toita.backend

case class UserSession(token: String, secret: String) {

  override def toString = getClass.getSimpleName + "[" + token + "/***]"
}
