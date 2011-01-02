package vkode.toita.backend

trait ToitaSessionUser {

  lazy val session = UserSession(System getProperty "token", System getProperty "apiSecret")
}
