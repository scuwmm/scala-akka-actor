package io.demo.actor

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.duration._

object LoopActor {

  trait Msg
  case class LoopMsg(id: String) extends Msg

  def apply(): Behavior[Msg] = Behaviors
    .supervise(Behaviors.withTimers[Msg] { timer =>
      timer.startTimerAtFixedRate(LoopMsg("id_1"), 5.second)
      Behaviors.setup[Msg] { implicit context =>
        def loop(map: Map[String, Int]): Behavior[Msg] = Behaviors.receiveMessage { case LoopMsg(id) =>
          val newMap = map.get(id) match {
            case None    => map.updated(id, 1)
            case Some(v) => map.updated(id, v + 1)
          }
          println(s"LoopActor map=$map, newMap=$newMap")
          loop(newMap)
        }
        loop(Map.empty)
      }
    })
    .onFailure(SupervisorStrategy.restart)

}
