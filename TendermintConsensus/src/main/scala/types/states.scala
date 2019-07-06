package main.scala.types

case class State(
  height:           Int,
  round:            Int,
  step:             RoundStep,
  lockedValue:      Block,
  lockedRound:      Int,
  validValue:       Block,
  validRound:       Int,
  validatorID:      Int,
  validatorSetSize: Int,
  proposal:         Block)
