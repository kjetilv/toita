package vkode.toita.waka

import akka.actor.Actor
import scalaz.Options

class Casting extends Actor with Options {

  protected def receive = {
    case x => println("Look what I got: " + x)
  }
}