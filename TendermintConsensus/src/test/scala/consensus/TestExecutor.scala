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
    
    Actors.executorCore ! MessageProposal(height, round, Some("Poruka 1"), -1, 0)
    
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 0)
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)

    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 0)
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)
    
    Thread.sleep(2000)
    println("--------------------------------------------------------------------------------------------")
  }
  
  @Test def notEnoughPrevotesReceived {
    val height = 0;
    val round = 0;
    
    println("***Not enough prevotes received!***")
    
    Actors.executorCore ! MessageProposal(height, round, Some("Poruka 1"), -1, 0)
    
    Actors.executorCore ! MessageVote(height, round, None, Prevote, 0)
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)

    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 0)
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)
    
    Thread.sleep(2000)
    println("--------------------------------------------------------------------------------------------")
  }
  
  @Test def notEnoughPrecommitsReceived {
    val height = 0;
    val round = 0;
    
    println("***Not enough precommits received!***")
    
    Actors.executorCore ! MessageProposal(height, round, Some("Poruka 1"), -1, 0)
    
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 0)
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)

    Actors.executorCore ! MessageVote(height, round, None, Precommit, 0)
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)
    
    Thread.sleep(4000)
    println("--------------------------------------------------------------------------------------------")
  }

  @Test def makingDecisionInRoundTwo {
    val height = 0;
    val round = 0;
    
    println("***Making decision in second round***")
    
    Actors.executorCore ! MessageProposal(height, round, Some("Poruka 1"), -1, 0)
    
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 0)
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)

    Actors.executorCore ! MessageVote(height, round, None, Precommit, 0)
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)
    
    Thread.sleep(4000)
    
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 0)
    
    Thread.sleep(2000)
    println("--------------------------------------------------------------------------------------------")
  }
  
  @Test def noProposalMessageReceived {
    val height = 0;
    val round = 0;
    
    println("***No Proposal Message Received***")
    
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 0)
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Prevote, 2)

    Actors.executorCore ! MessageVote(height, round, None, Precommit, 0)
    Actors.executorCore ! MessageVote(height, round, Some(util.md5HashString("Poruka 1")), Precommit, 2)
    
    Thread.sleep(2000)
    println("--------------------------------------------------------------------------------------------")
  }
  
}