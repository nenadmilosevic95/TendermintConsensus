package main.scala.consensus
import akka.actor.Actor
import main.scala.types._
import akka.actor.Props
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import scala.collection.mutable.HashMap

object executor {

  var state: State = State(0, 0, RoundStepPropose, None, -1, None, -1, 5, 0, None)
  val votes = HashMap.empty[Int, HashMap[Int, VoteSet]]
  val proposals = HashMap.empty[Int, HashMap[Int, Block]]

  class MessageReceiver extends Actor {

    def receive() = {
      case message: MessageProposal if (message.height >= state.height) => {
        println("Received: " + message)
        val height = message.height
        val round = message.round
        if (!proposals.contains(height)) proposals(height) = HashMap.empty[Int, Block]
        if (!proposals(height).contains(round)) {
          proposals(height)(round) = message.block
          val newEvent = EventProposal(height, round, -1, message.block, message.polRound, message.validatorID)
          Actors.eventHandler ! newEvent
        }
        val event = generateEvent(height, round)
        if (event != None) {
           Actors.eventHandler ! event.get
        }
       
      }
      case message: MessageVote if (message.height >= state.height) => {
        println("Received: " + message)
        val height = message.height
        val round = message.round
        if (!votes.contains(height)) votes(height) = HashMap.empty[Int, VoteSet]
        if (!votes(height).contains(round)) votes(height)(round) = new VoteSet(NumberOfValidators)
        votes(height)(round).addVote(message)
        val event = generateEvent(height, round)
        if (event != None) {
           Actors.eventHandler ! event.get
        }
        
      }

    }

    def generateEvent(height: Int, round: Int): Option[Event] = {
      if (proposals.contains(height) && proposals(height).contains(round) && votes.contains(height) && votes(height).contains(round)) {
        votes(height)(round).generateEvent(height, round, proposals(height)(round))
      } else None
    }

  }

  class EventHandler extends Actor {

    def receive() = {
      case t: Timeout => {
        runConsensus(Some(t))
      }
      case event: Event => {
        runConsensus(Some(event))
      }

    }

    def runConsensus(event: Option[Event]) {
      if (event != None) {
        println("OVDE" + event)
        val (newState, newMessage, newTimeout, newEvent) = Consensus.consensus(event.get, state)

        state = newState

        if (newMessage != None) {
          Actors.netSimulator ! newMessage.get
        }

        if (newTimeout != None) {
          import Actors.system.dispatcher
          Actors.system.scheduler.scheduleOnce(FiniteDuration(newTimeout.get.duration, TimeUnit.MILLISECONDS), Actors.eventHandler, newTimeout.get.timeout)
        }

        runConsensus(newEvent)
      }
    }
  }
}

