package main.scala.consensus

import akka.actor.ActorSystem
import akka.actor.Props
import net.Simulator.NetSimulator

object Actors {
  val system = ActorSystem("System")
  val executorCore = system.actorOf(Props[ExecutorCore], "executorCore")
  val netSimulator = system.actorOf(Props[NetSimulator], "netSimulator")
}