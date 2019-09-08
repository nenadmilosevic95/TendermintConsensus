package consensus

import org.junit._
import org.junit.Assert._

import main.scala.types._
import main.scala.consensus._
import main.scala.consensus.Consensus

class TestConsensus {

  // тестирање обраде догађаја преласка у нову висину
  @Test def eventNewHeightReceived {
    val newHeight = 5
    val currentHeight = 4
    val validatorId: Int = 3
    // креирање догађаја
    val event = EventNewHeight(newHeight, validatorId)
    // креирање стања процеса
    val state = State(currentHeight, 1, RoundStepPrecommit, None, -1, None, -1, 5, 0, None)
    // позивање консензуса
    val result = Consensus.consensus(event, state) match {
      case (State(5, _, RoundStepPropose, _, _, _, _, _, _, _), None, None, Some(EventNewRound(5, 0))) => true
      case _ => false
    }
    //провера резултата
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
      case (newState, _, Some(TriggerTimeout(_, _, _, TimeoutPropose(_, _))), _) if currentRound < newRound => true
      case (state, None, None, _) if currentRound >= newRound => true
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
      case (State(_, _, RoundStepPrevote, _, _, _, _, _, _, _), Some(MessageVote(_, _, Some(value), Prevote, _)), _, _) if checkFunction() => true
      case (State(_, _, RoundStepPrevote, _, _, _, _, _, _, _), Some(MessageVote(_, _, None, Prevote, _)), _, _) if !checkFunction() => true
      case (state, None, None, _) => true
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
      case (State(_, _, RoundStepPrevote, _, _, _, _, _, _, _), Some(MessageVote(_, _, None, Prevote, _)), _, _) => true
      case (state, None, None, _) => true
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
      case (State(_, _, RoundStepPrecommit, lockedValue, lockedRound, validValue, validRound, _, _, _), Some(MessageVote(_, _, _, Precommit, _)), _, _) if state.step == RoundStepPrevote => true
      case (State(_, _, RoundStepPrecommit, _, _, validValue, validRound, _, _, _), None, _, _) if state.step == RoundStepPrecommit => blockID.get == validValue.get && round == validRound
      case _ => false
    }

    assertTrue(result)
  }

  // тестира се ситуација када је процес
  // прво примио Majority23PrevoteAny па
  // Majority23PrevoteBlock
  @Test def eventMajority23PrevoteAnyPlusEventMajority23PrevoteBlock {
    val height = 5
    val round = 4
    // креира се иницијално стање
    val state = State(height, round, RoundStepPrevote, None, -1, None, -1, 2, 0, Some("5"))
    // креира се Majority23PrevotesAny догађај
    val event = Majority23PrevotesAny(height, round)
    var result = Consensus.consensus(event, state) match {
      case (
        State(5, 4, RoundStepPrevote, None, -1, None, -1, 2, 0, Some("5")),
        None,
        Some(TriggerTimeout(_, _, _, TimeoutPrevote(_, _))),
        _) => true
      case _ => false
    }
    //проверава се стање после првог догађаја
    assertTrue(result)
    // креира се други догађај
    val secondEvent = Majority23PrevotesBlock(height, round, Some("5"))
    result = Consensus.consensus(secondEvent, state) match {
      case (
        State(5, 4, RoundStepPrecommit, _, _, _, _, _, _, _),
        Some(MessageVote(_, _, _, Precommit, _)),
        _,
        _) => true
      case _ => false
    }
    // провера стања после другог догађаја
    assertTrue(result)
    val thirdEvent = TimeoutPrevote(height, round)
    val stateAfterSecondEvent = State(height, round, RoundStepPrecommit, None, -1, None, -1, 2, 0, Some("5"))
    result = Consensus.consensus(thirdEvent, stateAfterSecondEvent) match {
      case (
        State(height, round, RoundStepPrecommit, None, -1, None, -1, 2, 0, Some("5")),
        None,
        None,
        None) => true
      case _ => false
    }
    // провера после трећег догађаја
    assertTrue(result)
  }

  // тест обраде догађаја из рунде мање од тренутне
  @Test def eventMajority23PrevotesAnyFromPreviousRound {
    val height = 5
    val round = 4
    val event = Majority23PrevotesAny(height, round - 1)
    val state = State(height, round, RoundStepPrevote, None, -1, None, -1, 2, 0, None)

    val result = Consensus.consensus(event, state) match {
      case (state, None, Some(TriggerTimeout(_, _, _, TimeoutPrevote(_, _))), _) if state.step == RoundStepPrevote => true
      case (state, None, None, _) => true
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
      case (state, None, Some(TriggerTimeout(_, _, _, TimeoutPrevote(_, _))), _) if state.step == RoundStepPrevote => true
      case (state, None, None, _) => true
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
      case (State(_, _, RoundStepPrecommit, _, _, _, _, _, _, _), Some(MessageVote(_, _, None, Precommit, _)), None, _) if state.step == RoundStepPrevote => true
      case (state, None, None, _) => true
      case _ => false
    }

    assertTrue(result)
  }

  // тест обраде догађаја из претходне висине
  @Test def eventMajority23PrecommitBlockFromPreviousHeight {
    val height = 5
    val round = 4
    // креирање догађаја
    val event = Majority23PrecommitBlock(height - 1, round, Some("5"))
    // креирање тренутног стања
    val state = State(height, round, RoundStepPrecommit, None, -1, None, -1, 2, 0, Some("5"))
    // позивање консензус функције
    val result = Consensus.consensus(event, state) match {
      case (state, None, None, None) => true
      case _                         => false
    }
    // провера резултата
    assertTrue(result)
  }

  @Test def eventMajority23PrecommitAny {
    val height = 5
    val round = 4
    val event = Majority23PrecommitAny(height, round)
    val state = State(height, round, RoundStepPrevote, None, -1, None, -1, 2, 0, None)

    val result = Consensus.consensus(event, state) match {
      case (state, None, Some(TriggerTimeout(_, _, _, TimeoutPrecommit(_, _))), _) => true
      case _ => false
    }
    assertTrue(result)
  }
  
  // Тестирање да ли догађај eventMajority23PrecommitAny
  // доводи до креирања тајмаута TimeoutPrecommit и
  // и да ли истек тајмаута доводи до 
  // преласка у нову рунду
  @Test def eventMajority23PrecommitAnyAndTimeoutPrecommit {
    val height = 5
    val round = 4
    // креира се стање
    val state = State(height, round, RoundStepPrecommit, None, -1, None, -1, 2, 0, None)
    // креира се догађај
    val event = Majority23PrecommitAny(height, round)
    // покреће се консензус
    var result = Consensus.consensus(event, state) match {
      case (
          state,
          None,
          Some(TriggerTimeout(_, _, _, TimeoutPrecommit(5,4))),
          _) => true
      case _ => false
    }
    // проверава се да ли је креиран тајмаут
    assertTrue(result)
    // креира се идентичан тајмаут
    val timeoutEvent = TimeoutPrecommit(5,4)
    // покреће се косензус
    result = Consensus.consensus(timeoutEvent, state) match {
      case (
          _,
          None,
          None,
          Some(EventNewRound(5, 5))) => true
      case _ => false
    }
    // проверава се резултат
    assertTrue(result)
  }
  
  
  // тестирање пријема Majority23PrecommitAny и након тога 
  // пријема Majority23PrecommitBlock догађаја
  @Test def eventMajority23PrecommitAnyAnd23PrecommitBlock {
    val height = 5
    val round = 4
    // креира се иницијално стање
    val state = State(height, round, RoundStepPrecommit, None, -1, None, -1, 2, 0, Some("5"))
    // креира се први догађај
    val event = Majority23PrecommitAny(height, round)
    // покреће се консензус
    var result = Consensus.consensus(event, state) match {
      case (state, None, Some(TriggerTimeout(_, _, _, TimeoutPrecommit(_, _))), _) => true
      case _ => false
    }
    //проверава се први део теста
    assertTrue(result)
    // креира се други догађај
    val secondEvent = Majority23PrecommitBlock(height, round,Some("5"))
    // покреће се консензус
    result = Consensus.consensus(secondEvent, state) match {
      case (newState, None, None, Some(EventNewHeight(6, _))) => true
      case _ => false
    }
    // проверава се други део теста
    assertTrue(result)
  }

  @Test def eventTimeoutPrecommit {
    val height = 5
    val round = 4
    val newRound = round + 1
    val event = TimeoutPrecommit(height, round)
    val state = State(height, round, RoundStepPrecommit, None, -1, None, -1, 2, 0, None)

    val result = Consensus.consensus(event, state) match {
      case (State(_, newRound, _, _, _, _, _, _, _, _), None, None, _) => true
      case (state, None, None, _) => true
      case _ => false
    }

    assertTrue(result)
  }

}