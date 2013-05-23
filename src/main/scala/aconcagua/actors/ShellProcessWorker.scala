package aconcagua.actors

/**
 * @author Emilio Daniel González | @emdagon
 *         Date: 4/30/13
 */

import akka.actor.ActorPath
// import scala.concurrent.Future

import aconcagua.lib.ShellProcess
import scala.util.{Success, Failure}


abstract class ShellProcessWorker(masterLocation: ActorPath) extends Worker(masterLocation) {
  import context.dispatcher
  import aconcagua.messages.MasterWorkerProtocol._

  def program: String

  val process = new ShellProcess(program, err => log.info("Error: {}.", err))

  // Notify the Master that we're alive
  override def preStart() = {
    master ! WorkerCreated("samples")
  }

  def doWork(msg: Any): Unit = {
    //Future {
      msg match {
        case Command(module: String, function: String, arguments: List[String]) => {
          process.write("""{"module": "%s", "function": "%s", "arguments": [%s]}""".format(module, function, arguments.mkString(", "))) onComplete {
            case Failure(t) => log.warning("FAILURE! " + t.toString)
            case Success(v) => response(Result(v))
          }
        }
      }
    //}
  }

  override def postStop = process.close

}
