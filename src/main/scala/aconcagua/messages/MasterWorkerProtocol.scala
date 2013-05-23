package aconcagua.messages

/**
 * @author Emilio Daniel González | @emdagon
 *         Date: 4/29/13
 */

object MasterWorkerProtocol {
  // Messages from Workers
  case class WorkerCreated(worker: String)
  case object WorkerRequestsWork
  case class WorkIsDone(result: Any)

  // Messages to Workers
  case class WorkToBeDone(work: Any)
  case object WorkIsReady
  case object NoWorkToBeDone

  // ShellProcessWorker Protocol
  case class Command(module: String, function: String, arguments: List[String])
  case class Result(output: String)
}
