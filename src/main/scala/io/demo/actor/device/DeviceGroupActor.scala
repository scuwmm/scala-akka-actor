package io.demo.actor.device

import akka.actor.typed
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import io.demo.actor.device.DeviceGroupActor._

import scala.util.Random

object DeviceGroupActor {
  trait Cmd
  case class DeviceRegister(deviceId: String, ip: String)                         extends Cmd
  case class DeviceTracker(deviceId: String, ip: String, reply: ActorRef[String]) extends Cmd
  object DeviceLength                                                             extends Cmd
  case class DeviceTerminated(deviceId: String) extends Cmd

  case class FetchDevice(deviceId: String, reply: ActorRef[typed.ActorRef[DeviceActor.Cmd]]) extends Cmd

  def apply(): Behavior[Cmd] =
    Behaviors
      .supervise(Behaviors.setup[Cmd] { implicit ctx =>
        new DeviceGroupActor()
      })
      .onFailure(SupervisorStrategy.restart)

}

class DeviceGroupActor(implicit context: ActorContext[Cmd]) extends AbstractBehavior[Cmd](context) {

  println("init DeviceGroupActor")

  //维护一组Device
  private var deviceId2DeviceActor =
    Map.empty[String, ActorRef[DeviceActor.Cmd]]

  override def onMessage(msg: Cmd): Behavior[Cmd] = msg match {
    case req: DeviceRegister =>
//      println(s"DeviceGroupActor receive DeviceRegister($req)")
      deviceId2DeviceActor.get(req.deviceId) match {
        case Some(deviceActor) =>
          deviceActor ! DeviceActor.DeviceRecorder(req.deviceId, req.ip)
        case None              =>
          val deviceActor = context.spawn(
            DeviceActor.apply(),
            s"DeviceActor_${Random.between(1, 10000)}"
          )
          deviceActor ! DeviceActor.DeviceRecorder(req.deviceId, req.ip)
          deviceId2DeviceActor += req.deviceId -> deviceActor
      }

      Behaviors.same
    case req: DeviceTracker  =>
      deviceId2DeviceActor.get(req.deviceId).map { actor =>
        actor ! DeviceActor.DeviceRecorder(req.deviceId, req.ip)
      }
      this

    case DeviceTerminated(deviceId) =>
      deviceId2DeviceActor -= deviceId
      this
    case DeviceLength        =>
      println(s"DeviceLength=${deviceId2DeviceActor.size}")
      this

    case req: FetchDevice =>
      req.reply ! (deviceId2DeviceActor
        .get(req.deviceId) match {
        case Some(actor) => actor
        case None        =>
          context.spawn(
            DeviceActor.apply(),
            s"DeviceActor_${Random.between(1, 10000)}"
          )
      })
//        .fold[ActorRef[DeviceActor.Cmd]](
//          context.spawn(
//            DeviceActor.apply(),
//            s"DeviceActor_${Random.between(1, 10000)}"
//          )
//        )(_)
      this
    case _                =>
      println("unknown msg!")
      this
  }

}
