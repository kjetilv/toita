package vkode.toita.waka

import scala.collection.mutable.{Map => MutMap}

import akka.actor._
import scalaz.Options
import vkode.toita.events.UserRef
import akka.config.Supervision.OneForOneStrategy

class Casting extends Actor with Options {

  private val supervisors = MutMap[UserRef,Supervisor]()
  
  protected def receive = {
    case UserRef(name) => newSupervisor(UserRef(name))
    case x => println("Look what I got: " + x)
  }

  def newSupervisor(userRef: UserRef) = {
    val supervisor =
      supervisors.getOrElseUpdate(userRef, 
                                  new Supervisor(new OneForOneStrategy(trapExit = classOf[Exception] :: Nil,
                                                                       maxNrOfRetries = Some(3),
                                                                       withinTimeRange = Some(5000))))  
//    supervisor.link()
  }
}