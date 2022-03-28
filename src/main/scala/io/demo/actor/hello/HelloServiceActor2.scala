package io.demo.actor.hello

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import HelloServiceActor2._

object HelloServiceActor2 {
  trait Cmd2
  case class SayHello2(msg: String)                                  extends Cmd2
  case class SayHelloAndReply2(msg: String, reply: ActorRef[String]) extends Cmd2

  def apply(): Behavior[Cmd2] =
    Behaviors
      .supervise[Cmd2] {
        Behaviors.setup[Cmd2] { implicit context =>
          new HelloServiceActor2()
        }
      }
      .onFailure(SupervisorStrategy.restart)
}

class HelloServiceActor2(implicit context: ActorContext[Cmd2]) extends AbstractBehavior[Cmd2](context) {

  println("init HelloServiceActor2")

  override def onMessage(msg: Cmd2): Behavior[Cmd2] = msg match {
    case SayHello2(hello)                =>
      println(s"HelloServiceActor2 receive SayHello($hello)")
      Behaviors.same
    case SayHelloAndReply2(hello, reply) =>
      println(s"HelloServiceActor2 receive SayHelloAndReply($hello)")
      reply ! "HaHa"
      this
    case _                               =>
      println("unknown msg!")
      this
  }

}
