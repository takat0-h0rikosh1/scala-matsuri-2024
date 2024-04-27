import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

@main def helloVirtualThreads(): Unit =

  val es: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
  given ec: ExecutionContext = ExecutionContext.fromExecutor(es)

  def getCurrentTimeMillis: Future[Long] =
    Future.successful(System.currentTimeMillis())

  def asyncTask(): Future[Int] = Future {
    Thread.sleep(1000)
    1
  }

  val result = for {
    start <- getCurrentTimeMillis
    intSeq <- Future.traverse(1 to 1_000_000)(_ => asyncTask())
    end <- getCurrentTimeMillis
  } yield {
    println(s"Result: ${end - start}ms")
    println(s"Count: ${intSeq.sum}")
  }

  Await.result(result, Duration.Inf)

@main def helloOutOfMemoryError(): Unit =

  val es: ExecutorService = Executors.newCachedThreadPool()
  given ec: ExecutionContext = ExecutionContext.fromExecutor(es)

  def getCurrentTimeMillis: Future[Long] =
    Future.successful(System.currentTimeMillis())

  def asyncTask(): Future[Int] = Future {
    Thread.sleep(1000)
    1
  }

  val result = for {
    start <- getCurrentTimeMillis
    intSeq <- Future.traverse(1 to 1000000)(_ => asyncTask())
    end <- getCurrentTimeMillis
  } yield {
    println(s"Result: ${end - start}ms")
    println(s"Count: ${intSeq.sum}")
  }

  Await.result(result, Duration.Inf)

