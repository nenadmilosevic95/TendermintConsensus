package main.scala.types

trait Message

case class MessageProposal(
  height:   Int,
  round:    Int,
  block:  Block,
  polRound: Int,
  validatorID: Int) extends Message

case class MessageVote(
  height:   Int,
  round:    Int,
  blockID:  BlockID,
  voteType: VoteType,
  validatorID: Int) extends Message





