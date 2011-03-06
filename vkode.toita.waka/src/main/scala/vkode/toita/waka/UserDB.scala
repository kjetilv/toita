package vkode.toita.waka

object UserDB {
  
  def apply (users: String*) = DB.users filter (trpl => {
    users contains trpl._1
  }) map (trpl => {
    UserSession (Option(trpl._1), trpl._2, trpl._3)
  })
}