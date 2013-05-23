package aconcagua.actors

/**
 * @author Emilio Daniel González | @emdagon
 *         Date: 4/30/13
 */

import akka.actor.{Actor, ActorLogging, ActorPath}


abstract class Worker(masterLocation: ActorPath)
  extends Actor with ActorLogging {
  import aconcagua.messages.MasterWorkerProtocol._

  // We need to know where the master is
  val master = context.actorFor(masterLocation)

  // Required to be implemented
  def doWork(work: Any): Unit

  def response(result: Any) = {
    log.info("Work is complete.  Result {}.", result)
    master ! WorkIsDone(result)
  }

  // Notify the Master that we're alive
  override def preStart() = {
    master ! WorkerCreated("samples")
  }

  def receive = {
    case WorkToBeDone(work) =>
      log.info("Got work {}", work)
      doWork(work)

  }

}
