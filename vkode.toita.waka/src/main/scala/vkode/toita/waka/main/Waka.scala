package vkode.toita.waka.main

import akka.actor.Actors._

object Waka {

  val module = remote.start
  
  def main(args: Array[String]) {
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      def run = remote.shutdown
    }))
  }
}  