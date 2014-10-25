package com.github.begleyalan

import akka.actor._
import akka.pattern.ask
import akka.actor.Actor.Receive
import akka.util.Timeout
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object AskPatternApp extends App {
  implicit val timeout = Timeout(500 millis)
  val system = ActorSystem("AskPattern")
  val echoActor = system.actorOf(Props[EchoActor])
  val future: Future[Any] = echoActor ? "Hello"
  val message = Await.result(future, timeout.duration).asInstanceOf[String]
  println(message)
}

class EchoActor extends Actor {
  override def receive: Receive = {
    case msg => sender ! msg
  }
}
