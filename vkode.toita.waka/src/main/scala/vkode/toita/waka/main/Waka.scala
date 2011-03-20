package vkode.toita.waka.main

import akka.actor.Actors._
import akka.util.Logging

import vkode.toita.waka.Casting

object Waka extends Logging {

  val module = remote.start("localhost", 4020)

  module.register("casting", actorOf(classOf[Casting]))
  
  Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
    def run = remote.shutdown
  }))

  def main(args: Array[String]) {
    log.info("Started: " + module);
  }
}  