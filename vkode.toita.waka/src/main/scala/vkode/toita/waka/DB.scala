package vkode.toita.waka

import io.Source

object DB {
  
  private val dbLine = """(.*):(.*)=(.*)""".r
  private val shortDbLine = """(.*)=(.*)""".r
  
  private val rows = Source.fromFile("/Users/kjetil/Development/git/toita/db.txt").getLines.map(_.trim).toList
  
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



