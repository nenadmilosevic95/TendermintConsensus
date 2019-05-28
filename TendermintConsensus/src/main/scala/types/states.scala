package main.scala.types

case class State(
  height:           Long,
  round:            Int,
  step:             RoundStep,
  lockedValue:      Option[BlockID],
  lockedRound:      Int,
  validValue:       Option[BlockID],
  validRound:       Int,
  validatorID:      Int,
  validatorSetSize: Int)

