package vkode.toita.backend

import vkode.toita.events.TOUser

trait TwitterService {

  def homeTimeline: Unit

  def users (ids: List[BigInt]): Unit

  def status (id: BigInt): Unit

  def user: Option[TOUser]
  
  def userName: String
}