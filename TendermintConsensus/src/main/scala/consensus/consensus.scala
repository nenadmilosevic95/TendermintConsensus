package main.scala.consensus

import main.scala.types._

object consensus {

  def getValue(): BlockID = Some(5)

  def proposer(height: Long, round: Int): Int = 2

  def consensus(event: Event, state: State): (State, Option[Message], Option[TriggerTimeout]) = {
    event match {
      case EventNewHeight(height, validatorId) => {
        def checkEventValidity(): Boolean = height > state.height

        if (checkEventValidity()) {
          val newState = State(height, -1, RoundStepPropose, state.lockedValue, state.lockedRound, state.validValue, state.validRound, validatorId, state.validatorSetSize)
          (newState, None, None)
        } else {
          (state, None, None)
        }
      }
      case EventNewRound(height, round) => {
        def checkEventValidity(): Boolean = height == state.height && round > state.round

        if (checkEventValidity()) {
          val newState = State(height, round, RoundStepPropose, state.lockedValue, state.lockedRound, state.validValue, state.validRound, state.validatorID, state.validatorSetSize)
          val newMessage = if (proposer(height, round) == newState.validatorID) {
            val proposalValue = if (newState.validValue != None) newState.validValue else getValue()
            Some(MessageProposal(height, round, proposalValue, -1))
          } else None
          val newTimeout = TriggerTimeout(height, round, TimeoutProposeDuration, TimeoutPropose(height, round))
          (newState, newMessage, Some(newTimeout))
        } else {
          (state, None, None)
        }
      }
      case EventProposal(height, round, _, blockID, polRound, sender) => {
        def checkEventValidity(): Boolean = height == state.height && round == state.round && sender == proposer(height, round) && state.step == RoundStepPropose

        if (checkEventValidity()) {
          val newState = State(height, round, RoundStepPrevote, state.lockedValue, state.lockedRound, state.validValue, state.validRound, state.validatorID, state.validatorSetSize)
          val newMessage = if (polRound >= state.lockedRound || blockID.get == state.lockedValue.get || state.lockedRound == -1) {
            MessageVote(height, round, blockID, Prevote)
          } else {
            MessageVote(height, round, None, Prevote)
          }
          (newState, Some(newMessage), None)
        } else {
          (state, None, None)
        }

      }
      case TimeoutPropose(height, round) => {
        def checkEventValidity(): Boolean = height == state.height && round == state.round && state.step == RoundStepPropose

        if (checkEventValidity()) {
          val newMessage = MessageVote(height, round, None, Prevote)
          val newState = State(height, round, RoundStepPrevote, state.lockedValue, state.lockedRound, state.validValue, state.validRound, state.validatorID, state.validatorSetSize)
          (newState, Some(newMessage), None)
        } else {
          (state, None, None)
        }
      }
    }
  }
}

