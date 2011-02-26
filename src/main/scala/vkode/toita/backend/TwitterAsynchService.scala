package vkode.toita.backend

object TwitterAsynchService {

  def apply(userSession: UserSession): TwitterAsynchService = StreamEmitter (userSession)
}

trait TwitterAsynchService {

  def homeTimeline: Unit

  def users (ids: List[BigInt]): Unit

  def status (id: BigInt): Unit
}
