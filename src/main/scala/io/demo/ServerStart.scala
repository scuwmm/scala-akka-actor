package io.demo

import akka.Done
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.{ActorSystem => classicActorSystem}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import com.typesafe.config.ConfigFactory
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import io.demo.actor.LoopActor
import io.demo.actor.device.{DeviceActor, DeviceGroupActor}
import io.demo.actor.hello.HelloServiceActor
import io.demo.actor.hello.HelloServiceActor._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util._

object ServerStart extends App {

  val config = ConfigFactory.load

  val myActor = ActorSystem[Done](
    Behaviors.setup[Done] { implicit context =>
      implicit val system        = context.system
      implicit val ec            = context.executionContext
      implicit val classicSystem = context.system.classicSystem

      val server = new ServerStart()
//      server.sayHello()
//      server.sayHelloAndReply()
//
//      server.deviceRegister()

      server.loopActor()

      Behaviors.receiveMessage { case Done => Behaviors.stopped }
    },
    "my-actor"
  )

//  myActor.tell(Done)

  println("end")

}

class ServerStart(implicit system: ActorSystem[Nothing], context: ActorContext[_], ec: ExecutionContext) {

  private[this] implicit val timeout: Timeout = Timeout(10.seconds)

  def loopActor(): Unit = {
    system.systemActorOf(LoopActor.apply(), "loop-actor")
  }

  def sayHello(): Unit = {
    val helloActor = system.systemActorOf(HelloServiceActor.apply(), "hello-actor-1")
//    val helloActor = context.spawn(HelloServiceActor.apply(), "helloActor")
    println(helloActor.path)
    helloActor.tell(SayHello("Hi, WM"))

    println("")
  }

  def sayHelloAndReply(): Unit = {
    val helloActor = system.systemActorOf(HelloServiceActor(), "hello-actor-2")
    println(helloActor.path)
    helloActor.ask[String](reply => SayHelloAndReply(msg = "Hi, WM", reply = reply)) onComplete {
      case Success(reply) =>
        println(s"Receive reply=$reply")
      case Failure(e)     =>
        println(s"exception : $e")
    }
  }

  def deviceRegister(): Unit = {
    val deviceGroupActor = system.systemActorOf(DeviceGroupActor.apply(), "deviceGroupActor-1")
    deviceGroupActor ! DeviceGroupActor.DeviceRegister("device-001", "111.111.111.111")
    deviceGroupActor ! DeviceGroupActor.DeviceRegister("device-002", "111.111.111.111")
    deviceGroupActor ! DeviceGroupActor.DeviceRegister("device-003", "111.111.111.111")

    deviceGroupActor ! DeviceGroupActor.DeviceLength //DeviceLength=3
    deviceGroupActor ! DeviceGroupActor.DeviceTerminated("device-002")
    deviceGroupActor ! DeviceGroupActor.DeviceLength //DeviceLength=2

    deviceGroupActor.ask[ActorRef[DeviceActor.Cmd]](reply =>
      DeviceGroupActor.FetchDevice("device-001", reply)
    ) onComplete {
      case Success(device) =>
        device ! DeviceActor.FetchLastRecordIp(null) //这里打印 "111.111.111.111"
        deviceGroupActor ! DeviceGroupActor.DeviceRegister("device-001", "222.222.222.222")
        device ! DeviceActor.FetchLastRecordIp(null) //这里打印 "111.111.111.111"
      case Failure(e)      =>
    }

    deviceGroupActor.ask[ActorRef[DeviceActor.Cmd]](reply =>
      DeviceGroupActor.FetchDevice("device-001", reply)
    ) onComplete {
      case Success(device) =>
        device ! DeviceActor.FetchLastRecordIp(null) //这里打印 "222.222.222.222"
      case Failure(e)      =>
    }

    println(s"deviceRegister end")
  }

}
