package main.scala.consensus

import akka.actor.ActorSystem
import akka.actor.Props


object Actors {
  val system = ActorSystem("System")
  val messageReceiver = system.actorOf(Props[executor.MessageReceiver], "messageReceiver")
  val eventHandler = system.actorOf(Props[executor.EventHandler], "eventHandler")
  val messageSender = system.actorOf(Props[executor.MessageSender], "messageSender")
}