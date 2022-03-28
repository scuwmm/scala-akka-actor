package io.demo.actor.hello

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import HelloServiceActor._

object HelloServiceActor {
  trait Cmd
  case class SayHello(msg: String)                                  extends Cmd
  case class SayHelloAndReply(msg: String, reply: ActorRef[String]) extends Cmd

  def apply(): Behavior[Cmd] =
    Behaviors
      .supervise[Cmd] {
        Behaviors.setup[Cmd] { implicit context =>
          new HelloServiceActor()
        }
      }
      .onFailure(SupervisorStrategy.restart)
}

class HelloServiceActor(implicit context: ActorContext[Cmd]) extends AbstractBehavior[Cmd](context) {

  println("init HelloServiceActor")

  override def onMessage(msg: Cmd): Behavior[Cmd] = msg match {
    case SayHello(hello)                =>
      println(s"HelloServiceActor receive SayHello($hello)")

      val hello2 = context.spawn(HelloServiceActor2.apply(), "hello2-actor")
      println(s"hello2 path = ${hello2.path}")
      hello2 ! HelloServiceActor2.SayHello2("OOOOOOOO")

      Behaviors.same
    case SayHelloAndReply(hello, reply) =>
      println(s"HelloServiceActor receive SayHelloAndReply($hello)")
      reply ! "HaHa"
      this
    case _                              =>
      println("unknown msg!")
      this
  }

}
