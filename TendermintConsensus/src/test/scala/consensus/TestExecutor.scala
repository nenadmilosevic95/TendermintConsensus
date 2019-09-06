package consensus

import org.junit._
import org.junit.Assert._

import main.scala.types._
import main.scala.consensus._
import main.scala.consensus.Consensus

class TestExecutor {

  @Test def perfectScenario {
    val height = 0;
    val round = 0;

    println("***Perfect scenarion!***")

    Actors.messageReceiver ! MessageProposal(height, round, Some("Poruka 1"), -1, 0)

    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)
    

    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)

    Thread.sleep(2000)
    println("--------------------------------------------------------------------------------------------")
  }

  @Test def notEnoughPrevotesReceived {
    val height = 0;
    val round = 0;

    println("***Not enough prevotes received!***")

    Actors.messageReceiver ! MessageProposal(height, round, Some("Poruka 1"), -1, 0)

    Actors.messageReceiver ! MessageVote(height, round, None, Prevote, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)
    Thread.sleep(2000)
    Actors.messageReceiver ! MessageVote(height, round, None, Precommit, 0)
    Actors.messageReceiver ! MessageVote(height, round, None, Precommit, 2)
    Actors.messageReceiver ! MessageVote(height, round, None, Precommit, 3)

    Thread.sleep(2000)
    println("--------------------------------------------------------------------------------------------")
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

  @Test def makingDecisionInRoundTwo {
    val height = 0;
    val round = 0;

    println("***Making decision in second round***")

    Actors.messageReceiver ! MessageProposal(height, round, Some("Poruka 1"), -1, 0)

    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)

    Actors.messageReceiver ! MessageVote(height, round, None, Precommit, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)

    Thread.sleep(4000)

    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 0)

    Thread.sleep(2000)
    println("--------------------------------------------------------------------------------------------")
  }

  @Test def noProposalMessageReceived {
    val height = 0;
    val round = 0;

    println("***No Proposal Message Received***")

    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)

    Actors.messageReceiver ! MessageVote(height, round, None, Precommit, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)

    Thread.sleep(2000)
    println("--------------------------------------------------------------------------------------------")
  }

  @Test def faultyProcess {
    val height = 0;
    val round = 0;

    println("***Faulty process***")

    Actors.messageReceiver ! MessageProposal(height, round, Some("Poruka 1"), -1, 0)

    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 2")), Prevote, 0)
    Actors.messageReceiver ! MessageVote(height, round, None, Prevote, 1)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)

    Actors.messageReceiver ! MessageVote(height, round, None, Precommit, 1)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 2")), Precommit, 0)
    Actors.messageReceiver ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)

    Thread.sleep(8000)
    println("--------------------------------------------------------------------------------------------")
  }

}