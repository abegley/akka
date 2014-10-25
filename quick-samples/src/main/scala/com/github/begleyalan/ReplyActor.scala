package com.github.begleyalan

import akka.actor.{Props, ActorSystem, Actor}


object ReplyActor extends App {
  val system = ActorSystem("ReplyApp")
  val replyActor = system.actorOf(Props[ReplyActor])
  replyActor ! 5

}

class ReplyActor extends Actor {
  def receive = {
    case number: Int =>
      sender ! println("Hi, I received the " + number)
  }

}
