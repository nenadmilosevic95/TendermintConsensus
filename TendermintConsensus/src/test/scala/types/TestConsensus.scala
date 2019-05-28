package types

import org.junit._
import org.junit.Assert._

import main.scala.types._
import main.scala.consensus._
import main.scala.consensus.consensus

class TestConsensus {

  @Test def eventNewHeightReceived {
    val height = 5
    val currentHeight = 4
    val validatorId: Int = 3
    val event = EventNewHeight(height, validatorId)
    val state = State(currentHeight, -1, RoundStepPropose, None, -1, None, -1, 5, 0)

    val result = consensus.consensus(event, state) match {
      case (newState, None, None) if height > currentHeight => (newState.height == height) && (newState.validatorID == validatorId)
      case (oldState, None, None) => (oldState.height == currentHeight)
      case _ => false
    }

    assertTrue(result)
  }

  @Test def eventNewRoundReceived {
    val height = 5
    val newRound = 4
    val currentRound = newRound - 1
    val event = EventNewRound(height, newRound)
    val state = State(height, currentRound, RoundStepPropose, None, -1, None, -1, 2, 0)
    val newState = State(height, newRound, RoundStepPropose, state.lockedValue, state.lockedRound, state.validValue, state.validRound, state.validatorID, state.validatorSetSize)

    val result = consensus.consensus(event, state) match {
      case (newState, Some(MessageProposal(_, _, _, _)), Some(TriggerTimeout(_, _, _, TimeoutPropose(_, _)))) if currentRound < newRound => true
      case (state, None, None) if currentRound >= newRound => true
      case _ => false
    }

    assertTrue(result)
  }

  @Test def eventProposalReceived {
    val height = 5
    val round = 4
    val polRound = 2
    val event = EventProposal(height, round, 5, Some(5), polRound, 2)
    val state = State(height, round, RoundStepPropose, None, -1, None, -1, 2, 0)

    def checkFunction(): Boolean = {
      polRound >= state.lockedRound || event.blockID.get == state.lockedValue.get || state.lockedRound == -1
    }

    val result = consensus.consensus(event, state) match {
      case (State(_, _, RoundStepPrevote, _, _, _, _, _, _), Some(MessageVote(_, _, Some(value), Prevote)), _) if checkFunction() => true
      case (State(_, _, RoundStepPrevote, _, _, _, _, _, _), Some(MessageVote(_, _, None, Prevote)), _) if !checkFunction() => true
      case (state, None, None) => true
      case _ => false
    }

    assertTrue(result)
  }

  @Test def eventTimeoutPropose {
    val height = 5
    val round = 4
    val event = TimeoutPropose(height, round)
    val state = State(height, round, RoundStepPropose, None, -1, None, -1, 2, 0)

    val result = consensus.consensus(event, state) match {
      case (State(_, _, RoundStepPrevote, _, _, _, _, _, _), Some(MessageVote(_, _, None, Prevote)), _) => true
      case (state, None, None) => true
      case _ => false
    }

    assertTrue(result)
  }

}