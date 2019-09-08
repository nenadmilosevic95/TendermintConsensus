package consensus

import org.junit._
import org.junit.Assert._

import main.scala.types._
import main.scala.consensus._
import main.scala.consensus.Consensus

class TestExecutor {

  // Потпуно коректан сценарио
  @Test def perfectScenario {
    val height = 0;
    val round = 0;
    // дефинисање почетног стања
    executor.state = State(height, round, RoundStepPropose, None, -1, None, -1, 5, 0, None)
    // слање Proposal поруке
    Actors.messageReceiver ! MessageProposal(height, round, Some("Poruka 1"), -1, 0)
    // слање одговарајућих Prevote порука
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)
    // слање одговарајућих Precommit порука
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)

    Thread.sleep(100)
    // проверамо да ли је консензус одлучио, тј прешао у нову висину
    assertTrue(executor.state.height == height + 1)
  }

  // процес није примио 2f+1 Prevote порука, али доноси одлуку
  @Test def notEnoughPrevotesReceived {
    val height = 0;
    val round = 0;
    // дефинисање почетног стања
    executor.state = State(height, round, RoundStepPropose, None, -1, None, -1, 5, 0, None)
    // слање Proposal поруке
    Actors.messageReceiver ! MessageProposal(height, round, Some("Poruka 1"), -1, 0)
    // слање једне Prevote и једне Prevote nil поруке
    Actors.messageReceiver ! MessageVote(height, round, None, Prevote, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)
    // слање одговарајућих Prevote порука
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)

    Thread.sleep(100)
    // проверамо да ли је консензус одлучио, тј прешао у нову висину
    assertTrue(executor.state.height == height + 1)
  }

  @Test def notEnoughPrecommitsReceived {
    val height = 0;
    val round = 0;

    println("***Not enough precommits received!***")

    Actors.messageReceiver ! MessageProposal(height, round, Some("Poruka 1"), -1, 0)

    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)

    Actors.messageReceiver ! MessageVote(height, round, None, Precommit, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)

    Thread.sleep(4000)
    println("--------------------------------------------------------------------------------------------")
  }

  //доношење одлуке у другој рунди
  @Test def makingDecisionInRoundTwo {
    val height = 0;
    val round = 0;
    // дефинисање почетног стања
    executor.state = State(height, round, RoundStepPropose, None, -1, None, -1, 5, 0, None)
    // слање Proposal поруке
    Actors.messageReceiver ! MessageProposal(height, round, Some("Poruka 1"), -1, 0)
    // слање одговарајућих Prevote порука
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)
    // слање једне одговарајуће Precommit поруке и једне Precommit nil поруке
    Actors.messageReceiver ! MessageVote(height, round, None, Precommit, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)
    // чекамо да истекне тајмаут и да процес пређе у другу рунду
    Thread.sleep(4000)
    // проверамо да ли је консензус прешао у другу рунду
    assertTrue(executor.state.round == round + 1)

    // слање Precommit поруке која недостаје
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 0)

    Thread.sleep(100)
    // проверамо да ли је консензус одлучио, тј прешао у нову висину
    assertTrue(executor.state.height == height + 1)
  }
  
  // недоношење одлуке због непримања Proposal поруке
  @Test def noProposalMessageReceived {
    val height = 0;
    val round = 0;
    // дефинисање почетног стања
    executor.state = State(height, round, RoundStepPropose, None, -1, None, -1, 5, 0, None)
    // слање одговарајућих Prevote порука
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)
    // слање одговарајућих Precommi порука
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)
    // Чекамо да процес пређе у наредну рунду
    Thread.sleep(4000)
    // проверавамо да ли је процес остао у истој висини али је прешао у наредну рунду
    assertTrue(executor.state.round == round + 1)

  }

  // малициозан процес
  @Test def faultyProcess {
    val height = 0;
    val round = 0;
    // дефинисање почетног стања
    executor.state = State(height, round, RoundStepPropose, None, -1, None, -1, 5, 0, None)
    // слање Proposal поруке
    Actors.messageReceiver ! MessageProposal(height, round, Some("Poruka 1"), -1, 0)
    // слање једне невалидне Prevote и једне валидне Prevote поруке и једне Prevote nil
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 2")), Prevote, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)
    Actors.messageReceiver ! MessageVote(height, round, None, Prevote, 3)
    // слање једне невалидне Precommit и једне валидне Precommit поруке и једне Precommit nil
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 2")), Precommit, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)
    Actors.messageReceiver ! MessageVote(height, round, None, Precommit, 3)

    // чекамо да истекне тајмаут и да процес пређе у другу рунду
    Thread.sleep(4000)
    // проверамо да ли је консензус прешао у другу рунду
    assertTrue(executor.state.round == round + 1)
  }

  
  
}