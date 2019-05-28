package types

import main.scala.types._
import org.junit._
import org.junit.Assert._

class TestMessages {
  
  @Test def isProposalMessage {
    val msg: Message = new MessageProposal(2,3,Some(3),3)
    assertTrue(msg match {
      case m:MessageProposal => true
      case _ => false
    })
  }
  
}