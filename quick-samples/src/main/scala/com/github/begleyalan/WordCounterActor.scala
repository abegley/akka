package com.github.begleyalan

import akka.actor.{ActorSystem, Props, ActorRef, Actor}

object WordCounterApp extends App {
  import akka.util.Timeout
  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.dispatch.ExecutionContexts._

  implicit val ec = global

  val system = ActorSystem("WordCountSystem")
  val actor = system.actorOf(Props(new WordCounterActor(args(0))))
  implicit val timeout = Timeout(25 seconds)
  val future = actor ? StartProcessFileMsg()
  future.map{
    result => println("Total number of words " + result)
    system.shutdown()
  }
}

case class StartProcessFileMsg()

class WordCounterActor(fileName: String) extends Actor{
  private var running = false
  private var totalLines = 0
  private var linesProcessed = 0
  private var result = 0
  private var fileSender: Option[ActorRef] = None

  def receive = {
    case StartProcessFileMsg() => {
      if(running){
        println("Warning: duplicate start message received")
      } else{
        running = true
        fileSender = Some(sender) //save reference to process invoker

        import scala.io.Source._

          fromFile(fileName).getLines.foreach { line =>
          context.actorOf(Props[StringCounterActor]) ! ProcessStringMsg(line)
          totalLines += 1
        }
      }
    }
    case StringProcessedMsg(words) => {
      result += words
      linesProcessed += 1
      if(linesProcessed == totalLines){
        fileSender.map(_ ! result) //provide result to process invoker
      }
    }
    case _ => println("message not recognized!")
  }

}
