package main.scala.types

trait Event
trait Timeout extends Event

abstract class HeightAndRound(
  height: Long,
  round:  Int)

case class EventNewHeight(
  height:      Long,
  validatorId: Int) extends Event

case class EventNewRound(height: Long, round: Int) extends HeightAndRound(height, round) with Event

case class EventProposal(
  height:    Long,
  round:     Int,
  timestamp: Time,
  blockID:   BlockID,
  polRound:  Int,
  sender:    Int) extends Event

case class Majority23PrevotesBlock(
  height:  Long,
  round:   Int,
  blockID: BlockID) extends Event

case class Majority23PrecommitBlock(
  height:  Long,
  round:   Int,
  blockID: BlockID) extends Event

case class Majority23PrevotesAny(height: Long, round: Int) extends HeightAndRound(height, round) with Event
case class Majority23PrecommitAny(height: Long, round: Int) extends HeightAndRound(height, round) with Event
case class TimeoutPropose(height: Long, round: Int) extends HeightAndRound(height, round) with Timeout
case class TimeoutPrevote(height: Long, round: Int) extends HeightAndRound(height, round) with Timeout
case class TimeoutPrecommit(height: Long, round: Int) extends HeightAndRound(height, round) with Timeout
 
