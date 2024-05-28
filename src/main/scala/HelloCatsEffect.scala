import cats.effect.*
import cats.effect.std.Supervisor
import cats.implicits.*

import scala.concurrent.duration.*

object HelloCeFiber extends IOApp {

  def taskIO: IO[Int] = IO.sleep(1.second) *> IO.pure(1)

  def run(args: List[String]): IO[ExitCode] = {
    for {
      start <- IO.realTime
      fibers <- (1 to 1000000).toList.map(_ => taskIO.start).parSequence
      intSeq <- fibers.map(_.joinWithNever).sequence
      end <- IO.realTime
      _ <- IO.println(s"Time: ${(end - start).toMillis}ms")
      _ <- IO.println(s"Count: ${intSeq.sum}")
    } yield ExitCode.Success
  }
}

object HelloCeSupervisor extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    Supervisor[IO](await = false).use { outerSupervisor =>
      for {
        _ <- Supervisor[IO](await = false).use { _ =>
          for {
            _ <- IO
              .println("Still running ...")
              .andWait(1.second)
              .foreverM
              .supervise(outerSupervisor)
            _ <- IO.sleep(3.seconds)
            _ <- IO.println("The innermost scope is about to be closed.")
          } yield ()
        }
        _ <- IO.sleep(5.seconds)
        _ <- IO.println("The outer scope is about to be closed.")
      } yield ExitCode.Success
    }
}
