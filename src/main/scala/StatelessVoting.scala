import StatelessVoting.Citizen.{Vote, VoteStatusRequest}
import StatelessVoting.VotingAggregator.{AggregateVotes, VoteStatusReply}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object StatelessVoting extends App {
  // companion object
  object Citizen {
    case class Vote(candidate: String)
    case object VoteStatusRequest
  }
  class Citizen extends Actor {
    def vote(candidate: Option[String] = None): Receive = {
      case Vote(votedCandidate) => context.become(vote(Some(votedCandidate)))
      case VoteStatusRequest    => sender() ! VoteStatusReply(candidate)
    }

    override def receive: Receive = vote()
  }

  object VotingAggregator {
    case class VoteStatusReply(candidate: Option[String])
    case class AggregateVotes(citizens: Set[ActorRef])
  }
  class VotingAggregator extends Actor {
    def aggregateVotes(
        citizensToBeVoted: Set[ActorRef],
        currentStats: Map[String, Int]
    ): Receive = {
      case AggregateVotes(citizens: Set[ActorRef]) =>
        citizens.foreach(citizen => citizen ! VoteStatusRequest)
        context.become(aggregateVotes(citizens, currentStats))

      case VoteStatusReply(None) =>
        sender() ! VoteStatusRequest
        context.become(aggregateVotes(citizensToBeVoted, currentStats))

      case VoteStatusReply(Some(candidate)) =>
        val newWaitingCitizenSet = citizensToBeVoted - sender()

        val voteCount = currentStats.getOrElse(candidate, 0)
        val newCurrentStats = currentStats + (candidate -> (voteCount + 1))

        if (newWaitingCitizenSet.isEmpty) {
          println(s"Vote aggregation result is $newCurrentStats")
        } else {
          context.become(aggregateVotes(newWaitingCitizenSet, newCurrentStats))
        }
    }

    override def receive: Receive = aggregateVotes(Set(), Map())
  }

  val actorSystem = ActorSystem("actorSystem")

  val alice = actorSystem.actorOf(Props[Citizen], "alice")
  val bob = actorSystem.actorOf(Props[Citizen], "bob")
  val john = actorSystem.actorOf(Props[Citizen], "john")
  val ron = actorSystem.actorOf(Props[Citizen], "ron")

  val votingAggregator =
    actorSystem.actorOf(Props[VotingAggregator], "votingAggregator")

  // let's vote
  alice ! Vote("Megan")
  bob ! Vote("Amy")
  john ! Vote("Anne")
  ron ! Vote("Anne")

  // aggregate votes
  votingAggregator ! AggregateVotes(Set(alice, bob, ron, john))

//  // let's vote
//  alice ! Vote("Anne")
//
//  // aggregate votes
//  votingAggregator ! AggregateVotes(Set(alice, bob, ron, john))
}
