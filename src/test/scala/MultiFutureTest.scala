import akka.actor.ActorSystem

import java.util.concurrent.TimeoutException
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.pattern.after

//object x extends Enumeration
//object y extends Enumeratum

object MultiFutureTest {

  implicit val system = ActorSystem("tt")

  val timeoutFut = after(1.second)(Future(-100000))

  def main(args: Array[String]): Unit = {
    val fs =
      (0 until 100).map(i =>
        Future firstCompletedOf Seq(
          Future {
            if (i == 50) {
              Thread.sleep(2000L)
              i
            } else i
          },
          timeoutFut
        )
      )

    val fseq = Future.sequence(fs)
    val r    = Await.result(fseq, 5.second)
    println(s"###$r")

//    val fseq2 = Future.sequence(Seq(f1, f2)).flatMap{ r => if(r.isInstanceOf[Seq[Any]]) Future.successful(r) else Future.successful(1)}.map(r => println(r ))
//    Future.firstCompletedOf(Seq(f1, f2))
//    val r2 = Await.result(fseq2, 1.second)
//    println(s"###$r2")
//
//    val fc1 = Future.firstCompletedOf(Seq(f1, timeoutFut)).map(r => println(r))
//    val fc2 = Future.firstCompletedOf(Seq(f2, timeoutFut)).map(r => println(r))
//    val fc3 = Future.firstCompletedOf(Seq(f3, timeoutFut)).map(r => println(r))
//
//    Thread.sleep(5000L)
//    println("End")
  }

}
