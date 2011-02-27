package vkode.toita.backend

trait TwitterService {

  def homeTimeline: Unit

  def users (ids: List[BigInt]): Unit

  def status (id: BigInt): Unit

  def user: Option[TOUser]
  
  def userName: String
}
