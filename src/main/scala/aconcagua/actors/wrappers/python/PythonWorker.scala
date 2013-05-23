package aconcagua.actors.wrappers.python

/**
 * @author Emilio Daniel González | @emdagon
 *         Date: 4/30/13
 */

import aconcagua.actors.ShellProcessWorker
import akka.actor.ActorPath


class PythonWorker(masterLocation: ActorPath) extends ShellProcessWorker(masterLocation) {

  override def program = "python resources/wrapper.py"

}
