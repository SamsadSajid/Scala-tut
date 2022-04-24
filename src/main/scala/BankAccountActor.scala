import BankAccountActor.Command.Command
import BankAccountActor.ExecutionStatus.ExecutionStatus
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object BankAccountActor extends App {
  val actorSystem = ActorSystem("bankAccount")

  object Command extends Enumeration {
    type Command = Value
    val Deposit, Withdraw, Statement = Value
  }

  object ExecutionStatus extends Enumeration {
    type ExecutionStatus = Value
    val Success, Error, InvalidCommand, InvalidMessageFormat = Value
  }

  case class EventBody(command: Command, givenAmount: Double = 0)
  case class Event(ref: ActorRef, eventBody: EventBody)
  case class ActorResponse(code: ExecutionStatus, msg: String)

  class BankAccount extends Actor {
    var amount: Double = 0.0

    override def receive: Receive = {
      case Event(ref, eventBody) =>
        eventBody.command match {
          case Command.Deposit =>
            if (eventBody.givenAmount < 0)
              ref ! ActorResponse(
                ExecutionStatus.Error,
                "Cannot deposit negative amount"
              )
            else {
              amount += eventBody.givenAmount
              ref ! ActorResponse(
                ExecutionStatus.Success,
                "Deposit was successful"
              )
            }
          case Command.Withdraw =>
            if (eventBody.givenAmount < 0)
              ref ! ActorResponse(
                ExecutionStatus.Error,
                "Cannot withdraw negative amount"
              )
            else if (amount - eventBody.givenAmount < 0)
              ref ! ActorResponse(
                ExecutionStatus.Error,
                "Current deposited amount is less than the withdrawal amount"
              )
            else {
              amount -= eventBody.givenAmount
              ref ! ActorResponse(
                ExecutionStatus.Success,
                "Withdraw was successful"
              )
            }
          case Command.Statement => ref ! amount
          case _                 => ref ! ExecutionStatus.InvalidCommand
        }
      case amount: Double =>
        println(s"[${self}] Current statement is ${amount}")
      case ActorResponse(code, msg) =>
        println(s"[$self] $code - $msg")
      case ExecutionStatus.InvalidCommand =>
        println(s"[${self}] Invalid command was given")
      case _ => println(s"[${self}] Invalid message format")
    }
  }

  val bankAccountActor =
    actorSystem.actorOf(Props[BankAccount], "bankAccountActor")

  val bankAccountActorMW =
    actorSystem.actorOf(Props[BankAccount], "bankAccountActorMW")

  bankAccountActor ! Event(
    bankAccountActorMW,
    eventBody = EventBody(command = Command.Deposit, givenAmount = 10.00)
  )

  bankAccountActor ! Event(
    bankAccountActorMW,
    eventBody = EventBody(command = Command.Statement)
  )

  bankAccountActor ! Event(
    bankAccountActorMW,
    eventBody = EventBody(command = Command.Withdraw, givenAmount = 20)
  )

  bankAccountActor ! Event(
    bankAccountActorMW,
    eventBody = EventBody(command = Command.Withdraw, givenAmount = -10)
  )
}
