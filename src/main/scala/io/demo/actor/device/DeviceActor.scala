package io.demo.actor.device

import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import io.demo.actor.device.DeviceActor._

object DeviceActor {
  trait Cmd
  case class DeviceRecorder(deviceId: String, ip: String) extends Cmd
  case class FetchLastRecordIp(reply: ActorRef[String])   extends Cmd

  def apply(): Behavior[Cmd] = Behaviors
    .supervise[Cmd] {
      Behaviors.setup[Cmd] { implicit ctx =>
        new DeviceActor()
      }
    }
    .onFailure(SupervisorStrategy.restart)

}

class DeviceActor(implicit context: ActorContext[Cmd]) extends AbstractBehavior[Cmd](context) {

  println("init DeviceActor")

  private var lastRecordIp: Option[String] = None

  override def onMessage(msg: Cmd): Behavior[Cmd] = msg match {
    case req: DeviceRecorder    =>
//      println(s"DeviceActor receive DeviceRecorder($req)")
      lastRecordIp = Some(req.ip)
      Behaviors.same
    case req: FetchLastRecordIp =>
      println(s"DeviceActor receive FetchLastRecordIp, ip=${lastRecordIp.getOrElse("0.0.0.0")}")
//      req.reply ! lastRecordIp.getOrElse("0.0.0.0")

      this
    case _                      =>
      println("unknown msg!")
      this
  }

}
