package vkode.toita.backend

case class UserSession(token: String, secret: String) {

  def key = token + "-" + secret
}
