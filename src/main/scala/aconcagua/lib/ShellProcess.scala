package aconcagua.lib

/**
 * @author Emilio Daniel González | @emdagon
 * Date: 4/15/13
 */

import scala.sys.process.{Process, ProcessIO}
import scala.io.Source
import java.io.{BufferedReader, DataInputStream, DataOutputStream}
import scala.concurrent._


class ShellProcess(command: String, err: String => Unit) {

  private var processIn: DataOutputStream = null
  private var processOut: BufferedReader = null

  //private var resultPromise: Promise[String] = null

  val process = Process(command).run(
    new ProcessIO(
      stdin => {
        processIn = new DataOutputStream(stdin)
      },
      stdout => {
        processOut = Source.fromInputStream(stdout).bufferedReader()
      },
      stderr => {
        Source.fromInputStream(stderr).getLines().foreach(err)
        stderr.close() // prevents file descriptors leaks
      }
    ))

  def write(message: String): Future[String] = synchronized {
    val messageBytes = message.getBytes("UTF-8")
    processIn.write(messageBytes, 0, messageBytes.length)
    processIn.writeBytes("\nend!\n")
    processIn.flush()
    promise[String]().success(read).future
  }

  def read: String = {

    val line = new StringBuilder

    var ends = false

    while (!ends) {
      val subline = processOut.readLine

      if (subline == null) {
        val errorMessage = new StringBuilder
        errorMessage.append("Pipe to subprocess seems to be broken!")
        if (line.length == 0) {
          errorMessage.append(" No output read.\n")
        } else {
          errorMessage.append(" Currently read output: " + line.toString() + "\n")
        }
        errorMessage.append("Shell Process Exception: ?\n")
        throw new RuntimeException(errorMessage.toString())
      }

      if (subline != "end!") {
        if (line.length != 0) {
          line.append("\n")
        }
        line.append(subline)
      } else {
        ends = true
      }
    }
    line.toString()
  }

  def close = {
    // prevents file descriptors leaks
    processIn.close()
    processOut.close()
  }

}
