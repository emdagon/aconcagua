package aconcagua.actors.wrappers.ruby

/**
 * @author Emilio Daniel González | @emdagon
 *         Date: 5/6/13
 */

import aconcagua.actors.ShellProcessWorker
import akka.actor.ActorPath


class RubyWorker (masterLocation: ActorPath) extends ShellProcessWorker(masterLocation) {

  override def program = "ruby resources/wrapper.rb"

}
