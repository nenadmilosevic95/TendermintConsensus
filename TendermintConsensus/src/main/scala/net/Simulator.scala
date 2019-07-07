package net

import akka.actor.ActorSystem
import akka.actor.Props
import main.scala.consensus.ExecutorCore
import main.scala.consensus.Actors
import akka.actor.Actor
import main.scala.types._
import main.scala.consensus.util

object Simulator extends App {

  {
    sendMessages()
  }

  def sendMessages() = {
    Actors.executorCore ! MessageProposal(0, 0, Some("Poruka 1"), -1, 0)
    //Actors.executorCore ! MessageVote(0, 0, Some(util.md5HashString("Poruka 1")), Prevote, 0)
    Actors.executorCore ! MessageVote(0, 0, Some(util.md5HashString("Poruka 1")), Prevote, 2)
    Actors.executorCore ! MessageVote(0, 0, None, Prevote, 0)
    //Actors.executorCore ! MessageVote(0, 0, Some(util.md5HashString("Poruka 1")), Precommit, 0)
    Actors.executorCore ! MessageVote(0, 0, Some(util.md5HashString("Poruka 1")), Precommit, 2)
  }

  class NetSimulator extends Actor {
    def receive() = {
      case m: Message => println(m)
    }
  }
}