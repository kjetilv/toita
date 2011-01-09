package vkode.toita.backend

object TwitterService {

  def apply(userSession: UserSession): TwitterService = StreamEmitter (userSession)
}

trait TwitterService {

  def user: Option[TOUser]
}
