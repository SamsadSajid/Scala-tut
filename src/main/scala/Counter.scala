import akka.actor.{Actor, ActorSystem, Props}

object Counter extends App {
  val actorSystem = ActorSystem("actorSystem")

  object Command extends Enumeration {
    type Command = Value
    val Increment, Decrement, Print = Value
  }

  class CounterActor extends Actor {
    var counter = 0

    override def receive: Receive = {
      case Command.Increment => counter += 1
      case Command.Decrement => counter -= 1
      case Command.Print     => println(s"[${self}] counter value is ${counter}")
    }
  }

  val counterActor = actorSystem.actorOf(Props[CounterActor], "counterActor")
  counterActor ! Command.Print
  counterActor ! Command.Increment
  counterActor ! Command.Increment
  counterActor ! Command.Increment
  counterActor ! Command.Print
  counterActor ! Command.Decrement
  counterActor ! Command.Print
}
