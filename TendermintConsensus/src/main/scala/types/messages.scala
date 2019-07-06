package main.scala.types

trait Message

case class MessageProposal(
  height:   Long,
  round:    Int,
  block:  Block,
  polRound: Int) extends Message

case class MessageVote(
  height:   Long,
  round:    Int,
  blockID:  BlockID,
  voteType: VoteType) extends Message





