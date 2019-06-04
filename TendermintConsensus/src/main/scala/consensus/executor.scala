package consensus
import java.util.concurrent.LinkedBlockingQueue;
import main.scala.types._
import main.scala.consensus.consensus
import main.scala.net._

object executor extends App {

  val messageBuffer = new LinkedBlockingQueue[Message]()
  
  val netSimulatorThread = new Thread {
    override def run {
      netSimulator.startNet(messageBuffer)
    }
  }
  netSimulatorThread.start()

  while (true) {
    val message = messageBuffer.take();
    consensus.handle(message, null)
  }

}