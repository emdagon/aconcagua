package aconcagua

/**
 * @author Emilio Daniel González | @emdagon
 *         Date: 4/30/13
 */

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

import aconcagua.actors.Master
import aconcagua.messages.MasterWorkerProtocol._
import aconcagua.actors.wrappers.python.PythonWorker
import aconcagua.actors.wrappers.ruby.RubyWorker
import scala.util.{Success, Failure}


object AconcaguaMaster extends App {

  println("Aconcagua Master")

  println(ConfigFactory.load.getConfig("master"))

  val system = ActorSystem("AconcaguaMasterSystem", ConfigFactory.load.getConfig("master"))

  val m = system.actorOf(Props[Master], "master")

}

object AconcaguaWorkers extends App {

  println("Aconcagua Workers")

  val system = ActorSystem("AconcaguaWorkersSystem", ConfigFactory.load.getConfig("workers"))

  val mp = ActorPath.fromString("akka://AconcaguaMasterSystem@127.0.0.1:9000/user/master")
  val ma = system.actorFor(mp)

  for (n <- 1 to 3) {
    system.actorOf(Props(new PythonWorker(mp)))
    system.actorOf(Props(new RubyWorker(mp)))
  }
  //val w3 = system.actorOf(Props(new PythonWorker(mp)))

}

object AconcaguaTasks extends App {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.util.Random

  val system = ActorSystem("Aconcagua", ConfigFactory.load.getConfig("client"))

  implicit val timeout = Timeout(5 seconds)

  val mp = ActorPath.fromString("akka://AconcaguaMasterSystem@127.0.0.1:9000/user/master")

  val ma = system.actorFor(mp)

  println(ma)

  for (n <- List.fill(100)(Random.nextInt().toString)) {
    ma ? Command("samples", "avg", List("2", "4", n)) onComplete {
      case r => println(r)
    }
  }

}

object AconcaguaShellProcessTest extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  import aconcagua.lib.ShellProcess

  val process = new ShellProcess("python resources/wrapper.py", err => println("Error: {}.", err))

  def test(module: String, function: String, arguments: List[String]) {
    process.write("""{"module": "%s", "function": "%s", "arguments": [%s]}""".format(module, function, arguments.mkString(", "))) onComplete {
      case Failure(t) => println("FAILURE! " + t.toString)
      case Success(v) => println("Success! " + v)
    }
  }

  test("samples", "avg", List("2", "4", "6", "1"))
  test("samples", "avg", List("22", "44", "66", "2"))
  //test("samples", "avg", List("1", "3", "5", "7", "3"))

}
