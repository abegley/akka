package com.github.begleyalan

import akka.actor.{Props, ActorSystem, Actor}
import akka.actor.Actor.Receive

object HelloWorldAkka extends App {
   val system = ActorSystem("myfirstapp")
   val myFirstActor = system.actorOf(Props[MyFirstActor])

   myFirstActor ! "Hello World!"

   myFirstActor.!("Hello World")
}

class MyFirstActor extends Actor {
  override def receive: Receive = {
    case msg: String => println(msg)
    case _ => println("default")
  }
}
