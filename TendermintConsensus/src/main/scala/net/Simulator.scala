package net

import akka.actor.ActorSystem
import akka.actor.Props
import main.scala.consensus.ExecutorCore
import main.scala.consensus.Actors
import akka.actor.Actor
import main.scala.types.MessageProposal
import main.scala.types.Message

object Simulator extends App {

  {
    sendMessages()
  }

  def sendMessages() = {
    Actors.executorCore ! "Ola"
  }

  class NetSimulator extends Actor {
    def receive() = {
      case m: Message => println(m)
    }
  }
}