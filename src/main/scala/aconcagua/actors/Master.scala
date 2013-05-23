package aconcagua.actors

/**
 * @author Emilio Daniel González | @emdagon
 *         Date: 4/30/13
 */

import akka.actor.{Terminated, ActorRef, Actor, ActorLogging}

class Master extends Actor with ActorLogging {
  import aconcagua.messages.MasterWorkerProtocol._
  import scala.collection.mutable

  case class WorkerRef(module: String, ref: ActorRef, var work: Option[(Any, ActorRef)])

  // Holds known workers and what they may be working on
  val workers = mutable.Map.empty[ActorRef, WorkerRef]

  // Holds the pending (if any) list of work to be done as well
  // as the memory of who asked for it
  // {module: (actor-ref, work)}
  val workQueues = mutable.Map.empty[String, mutable.Queue[(ActorRef, Any)]]

  def registerWorker(module: String) {
    context.watch(sender)
    val worker = WorkerRef(module, sender, None)
    getWork(module) match {
      case Some((senderRef: ActorRef, work)) =>
        // work = Command(module, function, arguments)
        worker.work = Option(work, senderRef)
        worker.ref ! WorkToBeDone(work)
      case _ =>
    }
    workers += (sender -> worker)
  }

  def getIdleWorker(module: String) = workers.find({ w => w._2.module == module && w._2.work == None})

  @deprecated
  def getIdleWorkers(module: String) = {
    workers.filter {
      case (_, workerRef) if (workerRef.module == module && workerRef.work.isEmpty) => true
      case _ => false
    }
  }

  def getWorkerByRef(ref: ActorRef) = workers(ref)

  @deprecated
  def notifyIdleWorkers(module: String) {
    if (workQueues.contains(module) && !workQueues(module).isEmpty) {
      // I notify to many workers as pending work I have.
      // This make sense since I decided to support single-master scenario only
      println(" -> " + getIdleWorkers(module).slice(0, workQueues(module).size))
      getIdleWorkers(module).slice(0, workQueues(module).size).foreach {
        case (_, workerRef) if (workerRef.module == module && workerRef.work.isEmpty) => workerRef.ref ! WorkIsReady
        case _ =>
      }
    }
  }

  def queueWork(module: String, sender: ActorRef, work: Any) {
    if (!workQueues.contains(module)) {
      workQueues += (module -> mutable.Queue.empty[(ActorRef, Any)])
    }
    workQueues(module).enqueue((sender, work))
  }

  def getWork(module: String): Option[(ActorRef, Any)] = {
    if (workQueues.contains(module) && !workQueues(module).isEmpty)
      Option(workQueues(module).dequeue())
    else
      None
  }

  def receive = {
    // Worker is alive. Add him to the list (segmented by modules name), watch him for death
    case WorkerCreated(module) =>
      log.info("{} Worker created: {}", module, sender)
      registerWorker(module)

    // Worker has completed its work and we can clear it out
    case WorkIsDone(result) =>
      log.info("Work is complete.  Sending Result to the requester: {}.", result)
      val worker = getWorkerByRef(sender)
      worker.work match {
        case Some((_, workSender: ActorRef)) => workSender ! result
        case x => println("AAAAA!" + x.toString)
      }
      // TODO: this should be handle by registerWorker or similar
      getWork(worker.module) match {
        case Some((senderRef: ActorRef, work)) =>
          // work = Command(module, function, arguments)
          worker.work = Option(work, senderRef)
          worker.ref ! WorkToBeDone(work)
        case _ =>
          worker.work = None
      }
      workers += (sender -> worker)

    // A worker died.  If he was doing anything then we need
    // to give it to someone else so we just add it back to the
    // master and let things progress as usual
    case Terminated(workerRef) =>
      getWorkerByRef(workerRef) match {
        case worker: WorkerRef => {
          workers -= workerRef
          // Send the work that it was doing back to ourselves for processing
          worker.work match {
            case Some((work, workSender: ActorRef)) =>
              log.error("Error! {} -> {} died while processing {}", worker.module, worker.ref, worker.work)
              self.tell(work, workSender)
            case _ =>
          }
        }
      }

    // We got work to be done!
    // It must have the ´module´ attribute
    case Command(module: String, function: String, arguments: List[String]) =>
      val work = Command(module, function, arguments) // FIXME: there should be a more elegant way to do this
      getIdleWorker(module) match {
        case Some((key: ActorRef, worker: WorkerRef)) =>
          // FIXME: ugliness here
          log.info("Got idle worker! {}, ", worker)
          worker.work = Option(work, sender)
          worker.ref ! WorkToBeDone(work)
          workers += (worker.ref -> worker)
        case x =>
          // We have no idle worker for this module, so let's queue this work
          log.info("Queueing {}, ", work)
          queueWork(module, sender, work)
      }

    case wtf =>
      log.info("WFT! {}", wtf)
  }
}
