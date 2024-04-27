import scala.concurrent.Future
import scala.util.{Failure, Success, Using}

@main def helloStructuredConcurrency(): Unit =

  def sayHello(): String = throw new RuntimeException("Hello")

  def sayGoodBye(): String =
    Thread.sleep(1000)
    println("Goodbye")
    "done"

  case class Response(task1: String, task2: String)

  import java.util.concurrent.StructuredTaskScope

  val result = Using(new StructuredTaskScope.ShutdownOnFailure()) { scope =>
    val t1 = scope.fork(() => sayHello())
    val t2 = scope.fork(() => sayGoodBye())
    scope.join().throwIfFailed()
    Response(t1.get(), t2.get())
  }

  result match {
    case Success(_: Response) => println("Success")
    case Failure(e) => e.printStackTrace()
  }

  Thread.sleep(3000)


@main def helloUnstructuredConcurrency(): Unit =

  def sayHello(): String = throw new RuntimeException("Hello")

  def sayGoodBye(): String =
    Thread.sleep(1000)
    println("Goodbye")
    "done"

  case class Response(task1: String, task2: String)
  import scala.concurrent.ExecutionContext.Implicits.global

  val futureTask1 = Future(sayHello())
  val futureTask2 = Future(sayGoodBye())

  val result = for {
    t1 <- futureTask1
    t2 <- futureTask2
  } yield Response(t1, t2)

  result onComplete  {
    case Success(_: Response) => println("Success")
    case Failure(e) => e.printStackTrace()
  }

  Thread.sleep(3000)