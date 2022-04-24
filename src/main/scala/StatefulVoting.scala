import StatefulVoting.Citizen.{Vote, VoteStatusRequest}
import StatefulVoting.VotingAggregator.{AggregateVotes, VoteStatusReply}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object StatefulVoting extends App {
  // companion object
  object Citizen {
    case class Vote(candidate: String)
    case object VoteStatusRequest
  }
  class Citizen extends Actor {
    var candidate: Option[String] = None

    def vote: Receive = {
      case Vote(votedCandidate) => candidate = Some(votedCandidate)
      case VoteStatusRequest    => sender() ! VoteStatusReply(candidate)
    }

    override def receive: Receive = vote
  }

  object VotingAggregator {
    case class VoteStatusReply(candidate: Option[String])
    case class AggregateVotes(citizens: Set[ActorRef])
  }
  class VotingAggregator extends Actor {
    var waitingCitizenSet: Set[ActorRef] = Set()
    var currentStat: Map[String, Int] = Map()

    def aggregateVotes: Receive = {
      case AggregateVotes(citizens: Set[ActorRef]) =>
        waitingCitizenSet = citizens
        citizens.foreach(citizen => citizen ! VoteStatusRequest)

      case VoteStatusReply(None) => sender() ! VoteStatusRequest

      case VoteStatusReply(Some(candidate)) =>
        val newWaitingCitizenSet = waitingCitizenSet - sender()

        val voteCount = currentStat.getOrElse(candidate, 0)
        currentStat = currentStat + (candidate -> (voteCount + 1))

        if (newWaitingCitizenSet.isEmpty) {
          println(s"Vote aggregation result is $currentStat")
        } else {
          waitingCitizenSet = newWaitingCitizenSet
        }
    }

    override def receive: Receive = aggregateVotes
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
}
