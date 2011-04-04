package vkode.toita.events

object TwitterService {
  
  case object HomeTimeline
  
  case class Users(ids: List[BigInt])
  
  case class Status(id: BigInt)
  
  case class User
  
  case class UserName
}

trait TwitterService {

  val userName: String

  def homeTimeline()

  def users (ids: List[BigInt])

  def status (id: BigInt)

  def user: Option[TOUser]
}