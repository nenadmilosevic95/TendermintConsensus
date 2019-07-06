package main.scala.types

trait Event
trait Timeout extends Event

abstract class HeightAndRound(
  height: Int,
  round:  Int)

case class EventNewHeight(
  height:      Int,
  validatorId: Int) extends Event

case class EventNewRound(height: Int, round: Int) extends HeightAndRound(height, round) with Event

case class EventProposal(
  height:    Int,
  round:     Int,
  timestamp: Time,
  proposal:  Block,
  polRound:  Int,
  sender:    Int) extends Event

case class Majority23PrevotesBlock(
  height:  Int,
  round:   Int,
  blockID: BlockID) extends Event

case class Majority23PrecommitBlock(
  height:  Int,
  round:   Int,
  blockID: BlockID) extends Event

case class Majority23PrevotesAny(height: Int, round: Int) extends HeightAndRound(height, round) with Event
case class Majority23PrecommitAny(height: Int, round: Int) extends HeightAndRound(height, round) with Event
case class TimeoutPropose(height: Int, round: Int) extends HeightAndRound(height, round) with Timeout
case class TimeoutPrevote(height: Int, round: Int) extends HeightAndRound(height, round) with Timeout
case class TimeoutPrecommit(height: Int, round: Int) extends HeightAndRound(height, round) with Timeout
 
