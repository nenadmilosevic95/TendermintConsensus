package main.scala.consensus

import akka.actor.ActorSystem
import akka.actor.Props
import net.Simulator.NetSimulator

object Actors {
  val system = ActorSystem("System")
  val executorCore = system.actorOf(Props[ExecutorCore], "executorCore")
  val eventHandler = system.actorOf(Props[EventHandler], "eventHandler")
  val timeoutHandler = system.actorOf(Props[TimeoutHandler], "timeoutHandler")
  val netSimulator = system.actorOf(Props[NetSimulator], "netSimulator")
}