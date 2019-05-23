package main.scala.types

trait Message

case class MessageProposal(
  height:   Long,
  round:    Int,
  blockID:  Long,
  polRound: Int) extends Message

case class MessageVote(
  height:   Long,
  round:    Int,
  blockID:  BlockID,
  voteType: VoteType) extends Message

case class MessageDecision(
  height:  Long,
  round:   Int,
  blockID: BlockID) extends Message

case class TriggerTimeout(
  height:   Long,
  round:    Int,
  duration: Duration) extends Message

