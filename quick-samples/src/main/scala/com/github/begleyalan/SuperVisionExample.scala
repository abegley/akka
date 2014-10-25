package com.github.begleyalan

import akka.actor.SupervisorStrategy.{Escalate, Stop, Restart, Resume}
import akka.actor._
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration._

class Supervisor extends Actor {
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute){
    case _: ArithmeticException => Resume
    case _: NullPointerException => Restart
    case _: IllegalArgumentException => Stop
    case _: Exception => Escalate
  }

  def receive = {
    case p: Props => sender ! context.actorOf(p)
  }
}

class Child extends Actor {
  var state = 0
  def receive = {
    case ex: Exception => throw ex
    case x: Int => state = x
    case "get" => sender ! state
  }
}

object SuperVisionExample extends App {
 implicit val timeout = Timeout(50000 milliseconds)
 val system = ActorSystem("SupervisionApp")

 val supervisor = system.actorOf(Props[Supervisor], "supervisor")
 val future = supervisor ? Props[Child]
 val child = Await.result(future, timeout.duration).asInstanceOf[ActorRef]

 child ! 42
 println("Await response " + Await.result(child ? "get", timeout.duration).asInstanceOf[Int])
 child ! new ArithmeticException
 println("Arithmetic Exception response " + Await.result(child ? "get", timeout.duration).asInstanceOf[Int])
 child ! new NullPointerException
 println("Null Pointer Exception response " + Await.result(child ? "get", timeout.duration).asInstanceOf[Int])
}
