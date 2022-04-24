import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

object WordCounterWithActor extends App {
  trait ActorWithLogging extends Actor with ActorLogging

  // companion object
  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(text: String, taskId: Int)
    case class WordCountReply(count: Int, taskId: Int)
  }
  class WordCounterMaster extends ActorWithLogging {
    import WordCounterMaster._

    def logTaskResult(
        count: Int,
        taskId: Int,
        workerRefList: Seq[ActorRef],
        requestMap: Map[Int, ActorRef]
    ): Unit = {
      log.info(
        "[Result] Number of word for task {} is {}}",
        taskId,
        count
      )

      /**
        * sender() ! count
        *
        * this is wrong because here sender() is actually the Worker not the tester
        * */

      val originalSender = requestMap(taskId)

      originalSender ! count

      context.become(handleWork(workerRefList, taskId, requestMap))
    }

    def createTask(
        text: String,
        currTaskId: Int,
        workerRefList: Seq[ActorRef],
        requestMap: Map[Int, ActorRef]
    ): Unit = {
      val workerNoToBeAssigned: Int = currTaskId % workerRefList.length

      val newRequestMap: Map[Int, ActorRef] =
        requestMap + (currTaskId -> sender())

      val worker: ActorRef = workerRefList(workerNoToBeAssigned)

      val task = WordCountTask(text, currTaskId)

      log.info(
        "Received a new task. Sending it to {}",
        workerNoToBeAssigned
      )

      worker ! task

      val newTaskId = currTaskId + 1

      context.become(handleWork(workerRefList, newTaskId, newRequestMap))
    }

    def handleWork(
        workerRefList: Seq[ActorRef],
        currTaskId: Int,
        requestMap: Map[Int, ActorRef]
    ): Receive = {
      case text: String =>
        createTask(text, currTaskId, workerRefList, requestMap)
      case WordCountReply(count, taskId) =>
        logTaskResult(count, taskId, workerRefList, requestMap)
    }

    def initializeWorker(nChildren: Int): Unit = {
      log.info("Initializing {} worker...", nChildren)

      val workerRefList: Seq[ActorRef] =
        for (i <- 1 to nChildren)
          yield context.actorOf(Props[WordCountWorker], s"child_$i")

      log.info("{} worker initialized...", nChildren)

      context.become(
        handleWork(workerRefList, currTaskId = 0, requestMap = Map())
      )
    }

    override def receive: Receive = {
      case Initialize(nChildren) => initializeWorker(nChildren)
    }
  }

  class WordCountWorker extends ActorWithLogging {
    import WordCounterMaster.{WordCountTask, WordCountReply}

    def countWord(text: String): Int = text.split(" ").length

    override def receive: Receive = {
      case WordCountTask(text, taskId) =>
        log.info(
          "Got a new task with taskId {}. Processing...",
          taskId
        )
        sender() ! WordCountReply(countWord(text), taskId)
    }
  }

  class Tester extends ActorWithLogging {
    import WordCounterMaster._
    override def receive: Receive = {
      case "go" =>
        val masterWorker =
          context.actorOf(Props[WordCounterMaster], "masterWorker")

        masterWorker ! Initialize(3)

        val taskList = List(
          "You can't do this to me",
          "I am something of a scientist of myself",
          "I missed th part where that's my problem",
          "I am your father, Luke",
          "Fire the proton torpedo into the thermal exhaust port"
        )

        taskList.foreach(task => masterWorker ! task)

      case count: Int => log.info("Got my result {}", count)
    }
  }

  val actorSystem = ActorSystem("actorSystem")

  val testerWorker =
    actorSystem.actorOf(Props[Tester], "testerWorker")

  testerWorker ! "go"
}
