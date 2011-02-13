package vkode.toita.backend

trait ToitaSessionUser {

  val session = UserSession (System getProperty "token",
                             System getProperty "apiSecret")

  lazy val twitterAsynch = TwitterAsynchService (session)

  lazy val theTwitter = TwitterService (session)
}
