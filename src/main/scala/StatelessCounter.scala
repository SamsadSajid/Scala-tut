import akka.actor.{Actor, ActorSystem, Props}
import StatelessCounter.Counter.{Increment, Decrement, Print}

object StatelessCounter extends App {
  // Companion object
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }
  class Counter extends Actor {
    def count(currentCounter: Int): Receive = {
      case Increment => context.become(count(currentCounter + 1))
      case Decrement => context.become(count(currentCounter - 1))
      case Print     => println(currentCounter)
    }

    override def receive: Receive = count(currentCounter = 0)
  }

  val actorSystem = ActorSystem("actorSystem")
  val counter = actorSystem.actorOf(Props[Counter], "counter")

  (1 to 500).foreach(_ => counter ! Increment)
  (1 to 300).foreach(_ => counter ! Decrement)
  counter ! Print
}
