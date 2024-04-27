import zio.*
import zio.Console.printLine
import java.util.concurrent.TimeUnit
import zio.Clock._

object HelloZioCalcFibNumWithFiber extends ZIOAppDefault {

  def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      start <- Clock.currentTime(TimeUnit.MILLISECONDS)
      _ <- printLine( "----- Start -----")
      n <- fibNFiber(50)
      _ <- n.join.flatMap(printLine(_))
      _ <- printLine("----- End -----")
      end <- Clock.currentTime(TimeUnit.MILLISECONDS)
      result = end - start
      _ <- printLine(s"result: $result")
    } yield ()
  }

  def fib(n: Long): UIO[Long] =
    ZIO.suspendSucceed {
      if (n <= 1) ZIO.succeed(n)
      else fib(n - 1).zipWith(fib(n - 2))(_ + _)
    }

  def fibNFiber(n: Int): UIO[Fiber[Nothing, Long]] =
    for {
      fiber <- fib(n).fork
    } yield fiber
}

object HelloZioComposing extends ZIOAppDefault {

  def run = for {
    fiber1 <- ZIO.succeed("Hello!").fork
    fiber2 <- sayGoodBye()
    fiber = fiber1 zip fiber2
    tuple <- fiber.join
    _ <- ZIO.sleep(3.seconds)
    _ <- printLine(tuple)
  } yield ()

  def sayHello(): UIO[Fiber[Nothing, String]] =
    ZIO.suspendSucceed {
//      throw new Exception("Hello!")
      ZIO.succeed("Hello!")
    }.fork

  def sayGoodBye(): UIO[Fiber[Nothing, String]] =
    ZIO.suspendSucceed {
      Thread.sleep(3000)
      ZIO.succeed("Goodbye!")
    }.fork

}

object HelloZioFiber extends ZIOAppDefault {

  def task = ZIO.sleep(1.second) *> ZIO.succeed(1)

  def run = for {
    start <- currentTime(TimeUnit.MILLISECONDS)
    fibers <- ZIO.collectAllPar((1 to 1000000).map(_ => task.fork))
    intSeq <- ZIO.collectAll(fibers.map(_.join))
    end <- currentTime(TimeUnit.MILLISECONDS)
    _ <- ZIO.debug(s"Time: ${end - start}")
    _ <- ZIO.debug(s"Count: ${intSeq.sum}")
  } yield ()

}

object HelloZioSpecificScope extends ZIOAppDefault {

  def childTask(n: Long): ZIO[Any, Nothing, Long] =
    ZIO
      .debug(s"Child-$n: still running!")
      .repeat(Schedule.fixed(1.seconds))

  val parentTask: ZIO[Any, Nothing, Unit] =
    for {
      _ <- ZIO.debug("Parent: started!")
      _ <- childTask(1).fork
      _ <- childTask(2).forkDaemon
      _ <- ZIO.sleep(2.seconds)
      _ <- ZIO.debug("Parent: finished!")
    } yield ()

  def run =
    for {
      f <- parentTask.fork
      _ <- f.join
      _ <- ZIO.sleep(5.seconds)
    } yield ()
}

object HelloZioForkInSpecificScope extends ZIOAppDefault {

  def run =
    ZIO.scoped {
      for {
        scope <- ZIO.scope
        _ <-
          ZIO.scoped {
            for {
              _ <- ZIO
                .debug("Still running ...")
                .repeat(Schedule.fixed(1.second))
                .forkIn(scope)
              _ <- ZIO.sleep(3.seconds)
              _ <- ZIO.debug("The innermost scope is about to be closed.")
            } yield ()
          }
        _ <- ZIO.sleep(5.seconds)
        _ <- ZIO.debug("The outer scope is about to be closed.")
      } yield ()
    }
}