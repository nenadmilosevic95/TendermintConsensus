package main.scala.net
import java.util.concurrent.LinkedBlockingQueue
import main.scala.types._

object netSimulator {

  def startNet(buffer: LinkedBlockingQueue[Message]) = {
    for (i <- 1 to 5) {
      val message = MessageProposal(i, 1, Some(1), 1)
      buffer.add(message)
    }
    Thread.sleep(3000)
    for (i <- 5 to 10) {
      val message = MessageProposal(i, 1, Some(1), 1)
      buffer.add(message)
      
    }
  }

}