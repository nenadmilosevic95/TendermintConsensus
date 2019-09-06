package main.scala.consensus

import akka.actor.ActorSystem
import akka.actor.Props
import net.Simulator.NetSimulator

object Actors {
  val system = ActorSystem("System")
  val messageReceiver = system.actorOf(Props[executor.MessageReceiver], "messageReceiver")
  val eventHandler = system.actorOf(Props[executor.EventHandler], "eventHandler")
  val netSimulator = system.actorOf(Props[NetSimulator], "netSimulator")
}