package main.scala.consensus
import akka.actor.Actor
import main.scala.types._
import akka.actor.Props
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

object InfoKeeper {
  var state: State = State(0, 0, RoundStepPropose, None, -1, None, -1, 0, 0, None)

}

class ExecutorCore extends Actor {

  def receive() = {
    case MessageProposal(height, round, blockID, polRound) => println(1)
    case MessageVote(height, round, blockID, voteType)     => println(2)
  }

}

class EventHandler extends Actor {
  def receive() = {
    case event: Event => {
      val (state, newMessage, newTimeout) = Consensus.consensus(event, InfoKeeper.state)

      InfoKeeper.state = state // ovde treba dodati i za EventNewRound i EventNewHeight

      if (newMessage != None) {
        Actors.netSimulator ! newMessage
      }

      if (newTimeout != None) {
        Actors.timeoutHandler ! newTimeout
      }

    }
  }
}

class TimeoutHandler extends Actor {
  def receive() = {
    case TriggerTimeout(height, round, duration, timeoutEvent) => {
      import Actors.system.dispatcher
      Actors.system.scheduler.scheduleOnce(FiniteDuration(duration, TimeUnit.MILLISECONDS), Actors.eventHandler, timeoutEvent)
    }
  }
}
