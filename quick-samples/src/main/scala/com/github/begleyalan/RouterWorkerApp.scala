package com.github.begleyalan

import akka.actor.{Props, ActorSystem, Actor}
import akka.actor.Actor.Receive
import akka.routing.{Broadcast, RoundRobinPool}


object RouterWorkerApp extends App{
   val system = ActorSystem("RouterWorker")
   val routerActor = system.actorOf(RoundRobinPool(5).props(Props[RouterWorkerApp]), "workers")
   routerActor ! Broadcast("Hello")
}

class RouterWorkerApp extends Actor {
  override def receive: Receive = {
    case msg => println(s"Message $msg received in ${self.path}")
  }
}