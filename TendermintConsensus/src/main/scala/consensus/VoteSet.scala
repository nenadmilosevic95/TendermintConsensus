package main.scala.consensus

import scala.collection.mutable.ArrayBuffer
import main.scala.types._
import scala.collection.mutable.ListBuffer

class VoteSet(val numerOfValidators: Int) {
  val messages: ListBuffer[MessageVote] = ListBuffer()

  def addVote(message: MessageVote) {
    if (!messages.contains(message)) {
      messages += message
    }
  }

  def generateEvent(height: Int, round: Int, block: Block): Option[Event] = {
    val prevoteWithBlock = ((messages.toList filter (_.blockID != None)) filter (_.voteType == Prevote)) filter (_.blockID.get == util.md5HashString(block.get))
    val precommitWithBlock = ((messages.toList filter (_.blockID != None)) filter (_.voteType == Precommit)) filter (_.blockID.get == util.md5HashString(block.get))
    val prevoteWithNone = (messages.toList filter (_.blockID == None)) filter (_.voteType == Prevote)
    val precommitWithNone = (messages.toList filter (_.blockID == None)) filter (_.voteType == Precommit)

    if (precommitWithBlock.length >= (numerOfValidators * 2 / 3)) {
      return Some(Majority23PrecommitBlock(height, round, Some(util.md5HashString(block.get))))
    }
    if (precommitWithNone.length + precommitWithBlock.length >= (numerOfValidators * 2 / 3)) {
      return Some(Majority23PrecommitAny(height, round))
    }
    if (prevoteWithBlock.length >= (numerOfValidators * 2 / 3)) {
      return Some(Majority23PrevotesBlock(height, round, Some(util.md5HashString(block.get))))
    }
    if (prevoteWithNone.length + prevoteWithBlock.length >= (numerOfValidators * 2 / 3)) {
      return Some(Majority23PrevotesAny(height, round))
    }

    None

  }

  override def toString(): String = {
    "Messages: " + messages.length
  }
}