package consensus

import org.junit._
import org.junit.Assert._

import main.scala.types._
import main.scala.consensus._
import main.scala.consensus.Consensus

class TestConsensus {

  @Test def eventNewHeightReceived {
    val height = 5
    val currentHeight = 4
    val validatorId: Int = 3
    val event = EventNewHeight(height, validatorId)
    val state = State(currentHeight, -1, RoundStepPropose, None, -1, None, -1, 5, 0, None)

    val result = Consensus.consensus(event, state) match {
      case (newState, None, None,_) if height > currentHeight => (newState.height == height) && (newState.validatorID == validatorId)
      case (oldState, None, None,_) => (oldState.height == currentHeight)
      case _ => false
    }

    assertTrue(result)
  }

  @Test def eventNewRoundReceived {
    val height = 5
    val newRound = 4
    val currentRound = newRound - 1
    val event = EventNewRound(height, newRound)
    val state = State(height, currentRound, RoundStepPropose, None, -1, None, -1, 2, 0, None)
    val newState = State(height, newRound, RoundStepPropose, state.lockedValue, state.lockedRound, state.validValue, state.validRound, state.validatorID, state.validatorSetSize, None)

    val result = Consensus.consensus(event, state) match {
      case (newState, _, Some(TriggerTimeout(_, _, _, TimeoutPropose(_, _))),_) if currentRound < newRound => true
      case (state, None, None,_) if currentRound >= newRound => true
      case _ => false
    }

    assertTrue(result)
  }

  @Test def eventProposalReceived {
    val height = 5
    val round = 4
    val polRound = 2
    val event = EventProposal(height, round, 5, Some("3"), polRound, 2)
    val state = State(height, round, RoundStepPropose, None, -1, None, -1, 2, 0, None)

    def checkFunction(): Boolean = {
      polRound >= state.lockedRound || event.proposal.get == state.lockedValue.get || state.lockedRound == -1
    }

    val result = Consensus.consensus(event, state) match {
      case (State(_, _, RoundStepPrevote, _, _, _, _, _, _,_), Some(MessageVote(_, _, Some(value), Prevote,_)), _,_) if checkFunction() => true
      case (State(_, _, RoundStepPrevote, _, _, _, _, _, _,_), Some(MessageVote(_, _, None, Prevote,_)), _,_) if !checkFunction() => true
      case (state, None, None,_) => true
      case _ => false
    }

    assertTrue(result)
  }

  @Test def eventTimeoutPropose {
    val height = 5
    val round = 4
    val event = TimeoutPropose(height, round)
    val state = State(height, round, RoundStepPropose, None, -1, None, -1, 2, 0, None)

    val result = Consensus.consensus(event, state) match {
      case (State(_, _, RoundStepPrevote, _, _, _, _, _, _,_), Some(MessageVote(_, _, None, Prevote,_)), _,_) => true
      case (state, None, None,_) => true
      case _ => false
    }

    assertTrue(result)
  }

  @Test def eventMajority23PrevotesBlock {
    val height = 5
    val round = 4
    val blockID = Some("5")
    val event = Majority23PrevotesBlock(height, round, blockID)
    val state = State(height, round, RoundStepPrevote, Some("5"), -1, Some("5"), -1, 2, 0, None)

    val result = Consensus.consensus(event, state) match {
      case (State(_, _, RoundStepPrecommit, lockedValue, lockedRound, validValue, validRound, _, _,_), Some(MessageVote(_, _, _, Precommit,_)), _,_) if state.step == RoundStepPrevote => true
      case (State(_, _, RoundStepPrecommit, _, _, validValue, validRound, _, _,_), None, _,_) if state.step == RoundStepPrecommit => blockID.get == validValue.get && round == validRound
      case _ => false
    }

    assertTrue(result)
  }

  @Test def eventMajority23PrevotesAny {
    val height = 5
    val round = 4
    val event = Majority23PrevotesAny(height, round)
    val state = State(height, round, RoundStepPrevote, None, -1, None, -1, 2, 0, None)

    val result = Consensus.consensus(event, state) match {
      case (state, None, Some(TriggerTimeout(_, _, _, TimeoutPrevote(_, _))),_) if state.step == RoundStepPrevote => true
      case (state, None, None,_) => true
      case _ => false
    }

    assertTrue(result)
  }

  @Test def eventTimeoutPrevote {
    val height = 5
    val round = 4
    val event = TimeoutPrevote(height, round)
    val state = State(height, round, RoundStepPrevote, None, -1, None, -1, 2, 0, None)

    val result = Consensus.consensus(event, state) match {
      case (State(_, _, RoundStepPrecommit, _, _, _, _, _, _,_), Some(MessageVote(_, _, None, Precommit,_)), None,_) if state.step == RoundStepPrevote => true
      case (state, None, None,_) => true
      case _ => false
    }

    assertTrue(result)
  }

  @Test def eventMajority23PrecommitBlock {
    val height = 5
    val newHeight = height + 1
    val round = 4
    val event = Majority23PrecommitBlock(height, round, Some("5"))
    val state = State(height, round, RoundStepPrecommit, None, -1, None, -1, 2, 0, None)

    val result = Consensus.consensus(event, state) match {
      case (State(newHeight, _, _, _, _, _, _, _, _,_), None, None,_) => true
      case (state, None, None,_) => true
      case _ => false
    }

    assertTrue(result)
  }

  @Test def eventMajority23PrecommitAny {
    val height = 5
    val round = 4
    val event = Majority23PrecommitAny(height, round)
    val state = State(height, round, RoundStepPrevote, None, -1, None, -1, 2, 0, None)

    val result = Consensus.consensus(event, state) match {
      case (state, None, Some(TriggerTimeout(_, _, _, TimeoutPrecommit(_, _))),_) => true
      case _ => false
    }
    assertTrue(result)
  }
  
  @Test def eventMajority23PrecommitAnyAnd23PrecommitBlock {
    val height = 5
    val round = 4
    val event = Majority23PrecommitAny(height, round)
    val state = State(height, round, RoundStepPrevote, None, -1, None, -1, 2, 0, None)

    val result = Consensus.consensus(event, state) match {
      case (state, None, Some(TriggerTimeout(_, _, _, TimeoutPrecommit(_, _))),_) => true
      case _ => false
    }
    assertTrue(result)
  }

  @Test def eventTimeoutPrecommit {
    val height = 5
    val round = 4
    val newRound = round + 1
    val event = TimeoutPrecommit(height, round)
    val state = State(height, round, RoundStepPrecommit, None, -1, None, -1, 2, 0, None)

    val result = Consensus.consensus(event, state) match {
      case (State(_, newRound, _, _, _, _, _, _, _,_), None, None,_) => true
      case (state, None, None,_) => true
      case _ => false
    }

    assertTrue(result)
  }

}