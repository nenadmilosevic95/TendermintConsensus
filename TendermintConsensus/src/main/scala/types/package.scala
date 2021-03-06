package main.scala

package object types {

  val NumberOfValidators = 4
 
  type BlockID = Option[String]
  type Block = Option[String]

  type RoundStep = Int

  val RoundStepUnknown = -1
  val RoundStepPropose = 0
  val RoundStepPrevote = 1
  val RoundStepPrecommit = 2
  val RoundStepCommit = 3

  type VoteType = Int

  val VoteTypeUnknown = -1
  val Prevote = 0
  val Precommit = 1

  type Time = Int

  type Duration = Long

  val TimeoutProposeDuration: Duration = 2000
  val TimeoutPrevoteDuration: Duration = 2000
  val TimeoutPrecommitDuration: Duration = 2000

 

}