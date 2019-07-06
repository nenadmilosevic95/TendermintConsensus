package main.scala.consensus

import main.scala.types._

import scala.util.Random

object Consensus {

  def getValue(): Block = Some(Random.alphanumeric.take(100).mkString)

  var counter:Int = -1
  
  def proposer(height: Int, round: Int): Int = {
    counter = counter + 1
    (height + round + counter) % NumberOfValidators
  }

  def getQuorumNumber(message: Message): Int = 5

  def handle(messages: List[Message], state: State): Option[Event] = {
    None
  }

  def checkMessage(message: Message, state: State) = {

  }

  def consensus(event: Event, state: State): (State, Option[Message], Option[TriggerTimeout]) = {
    event match {
      case EventNewHeight(height, validatorId) => {
        def checkEventValidity(): Boolean = height > state.height

        if (checkEventValidity()) {
          val newState = State(height, -1, RoundStepPropose, state.lockedValue, state.lockedRound, state.validValue, state.validRound, validatorId, state.validatorSetSize, None)
          (newState, None, None)
        } else {
          (state, None, None)
        }
      }
      case EventNewRound(height, round) => {
        def checkEventValidity(): Boolean = height == state.height && round > state.round

        if (checkEventValidity()) {
          val (newMessage: Option[MessageProposal], proposal: Block) = if (proposer(height, round) == state.validatorID) {
            val proposalValue = if (state.validValue != None) state.validValue else getValue()
            (Some(MessageProposal(height, round, proposalValue, -1)), proposalValue)
          } else (None, None)
          val newState = State(height, round, RoundStepPropose, state.lockedValue, state.lockedRound, state.validValue, state.validRound, state.validatorID, state.validatorSetSize, proposal)
          val newTimeout = TriggerTimeout(height, round, TimeoutProposeDuration, TimeoutPropose(height, round))
          (newState, newMessage, Some(newTimeout))
        } else {
          (state, None, None)
        }
      }
      case EventProposal(height, round, _, proposal, polRound, sender) => {
        def checkEventValidity(): Boolean = height == state.height && round == state.round && sender == proposer(height, round) && state.step == RoundStepPropose

        if (checkEventValidity()) {
          val newState = State(height, round, RoundStepPrevote, state.lockedValue, state.lockedRound, state.validValue, state.validRound, state.validatorID, state.validatorSetSize,proposal)
          val newMessage = if (polRound >= state.lockedRound || proposal.get == state.lockedValue.get || state.lockedRound == -1) {
            MessageVote(height, round, Some(util.md5HashString(proposal.get)) , Prevote)
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
          val newState = State(height, round, RoundStepPrevote, state.lockedValue, state.lockedRound, state.validValue, state.validRound, state.validatorID, state.validatorSetSize,state.proposal)
          (newState, Some(newMessage), None)
        } else {
          (state, None, None)
        }
      }
      case Majority23PrevotesBlock(height, round, blockID) => {
        def checkEventValidity(): Boolean = height == state.height && round == state.round && state.step >= RoundStepPrevote && round > state.validRound

        if (checkEventValidity()) {
          val (lockedRound, lockedValue, message) = if (state.step == RoundStepPrevote) {
            (round, blockID, Some(MessageVote(height, round, blockID, Precommit)))
          } else {
            (state.lockedRound, state.lockedValue, None)
          }
          val newState = State(height, round, RoundStepPrecommit, lockedValue, lockedRound, state.proposal, round, state.validatorID, state.validatorSetSize,state.proposal)
          (newState, message, None)
        } else {
          (state, None, None)
        }
      }
      case Majority23PrevotesAny(height, round) => {
        def checkEventValidity(): Boolean = height == state.height && round == state.round && state.step == RoundStepPrevote

        if (checkEventValidity()) {
          val timeout = TriggerTimeout(height, round, TimeoutPrevoteDuration, TimeoutPrevote(height, round))
          (state, None, Some(timeout))
        } else {
          (state, None, None)
        }
      }
      case TimeoutPrevote(height, round) => {
        def checkEventValidity(): Boolean = height == state.height && round == state.round && state.step == RoundStepPrevote

        if (checkEventValidity()) {
          val message = MessageVote(height, round, None, Precommit)
          val newState = State(state.height, state.round, RoundStepPrecommit, state.lockedValue, state.lockedRound, state.validValue, state.validRound, state.validatorID, state.validatorSetSize,state.proposal)
          (newState, Some(message), None)
        } else {
          (state, None, None)
        }
      }
      case Majority23PrecommitBlock(height, round, blockID) => {
        def checkEventValidity(): Boolean = height == state.height

        if (checkEventValidity()) {
          val newState = State(state.height + 1, state.round, RoundStepCommit, state.lockedValue, state.lockedRound, state.validValue, state.validRound, state.validatorID, state.validatorSetSize,state.proposal)
          (newState, None, None)
        } else {
          (state, None, None)
        }
      }
      case Majority23PrecommitAny(height, round) => {
        def checkEventValidity(): Boolean = height == state.height && round == state.round

        if (checkEventValidity()) {
          val timeout = TriggerTimeout(height, round, TimeoutPrecommitDuration, TimeoutPrecommit(height, round))
          (state, None, Some(timeout))
        } else {
          (state, None, None)
        }
      }
      case TimeoutPrecommit(height, round) => {
        def checkEventValidity(): Boolean = height == state.height && round == state.round

        if (checkEventValidity()) {
          val newState = State(state.height, state.round + 1, RoundStepPrecommit, state.lockedValue, state.lockedRound, state.validValue, state.validRound, state.validatorID, state.validatorSetSize,state.proposal)
          (newState, None, None)
        } else {
          (state, None, None)
        }
      }
    }
  }
}

