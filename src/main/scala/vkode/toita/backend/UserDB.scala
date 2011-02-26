package vkode.toita.backend

import io.Source

object UserDB {
  
  private val dbLine = """(.*):(.*)=(.*)""".r
  private val shortDbLine = """(.*)=(.*)""".r
  
  private val lines = Source.fromFile("/Users/kjetil/Development/git/toita/db.txt").getLines.toList
  
  private val api: (String, String) = {
    lines(0) match {
      case dbLine("api", key, secret) => (key, secret)
    } 
  }
  
  private val users: List[(String, String, String)] = lines.tail map {
    _ match {
      case dbLine(user, token, secret) => (user, token, secret) 
      case shortDbLine(key, secret) => (null, key, secret)
    }
  }
  
  def apply (users: String*) = this.users filter (trpl => {
    users contains trpl._1
  }) map (trpl => {
    UserSession (Option(trpl._1), trpl._2, trpl._3)
  })
}