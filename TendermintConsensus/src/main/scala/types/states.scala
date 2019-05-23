package main.scala.types

class State(
  height:           Long,
  round:            Int,
  step:             RoundStep,
  lockedValue:      BlockID,
  lockedRound:      Int,
  validValue:       BlockID,
  validRound:       Int,
  validatorID:      Int,
  validatorSetSize: Int)

