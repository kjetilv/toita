package vkode.toita.toita

import io.Source
import vkode.toita.events.{UserRef, UserSession}

object DB {
  
  def apply (user: UserRef): Option[UserSession] = 
    this.users find (trpl => {
      trpl._1 == user.screenName
    }) map (trpl => {
      UserSession(Some(user.screenName), trpl._2, trpl._3)
    })
  
  def apply (users: String*) = this.users filter (trpl => {
    users contains trpl._1
  }) map (trpl => {
    UserSession (Option(trpl._1), trpl._2, trpl._3)
  })
  
  private val dbLine = """(.*):(.*)=(.*)""".r
  
  private val shortDbLine = """(.*)=(.*)""".r
  
  private val rows = Source.fromFile("/Users/kjetil/Development/git/toita/db.txt").getLines().map(_.trim).toList
  
  val apiKey: (String, String) = rows(0) match {
    case dbLine("api", key, secret) => (key, secret)
    case shortDbLine(key, secret) => (key, secret)
    case _ => error("API key not in database")
  } 
  
  val users: List[(String, String, String)] = rows.tail map {
    _ match {
      case dbLine(user, token, secret) => (user, token, secret) 
      case shortDbLine(key, secret) => (null, key, secret)
    }
  }
}



