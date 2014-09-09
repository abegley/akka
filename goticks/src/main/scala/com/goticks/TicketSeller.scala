package com.goticks

import akka.actor.{PoisonPill, Actor}

/**
 * Created by alan on 09/09/14.
 */
class TicketSeller extends Actor {
  import TicketProtocol._

  var tickets = Vector[Ticket]()

  override def receive: Receive = {

    case GetEvents => sender ! tickets.size

    case Tickets(newTickets) => tickets = tickets ++ newTickets

    case BuyTicket =>
      if(tickets.isEmpty){
        sender ! SoldOut
        self ! PoisonPill
      }

      tickets.headOption.foreach { ticket =>
        tickets = tickets.tail
        sender ! ticket
      }
  }
}

object TicketProtocol {
  import spray.json._
  import DefaultJsonProtocol._

  case class Event(event:String, nrOfTickets:Int)

  case object GetEvents

  case class Events(events:List[Event])

  case object EventCreated

  case class TicketRequest(event:String)

  case object SoldOut

  case class Tickets(tickets:List[Ticket])

  case object BuyTicket

  case class Ticket(event:String, nr:Int)

  //----------------------------------------------
  // JSON
  //----------------------------------------------
  object Event extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(Event.apply)
  }

  object TicketRequest extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(TicketRequest.apply)
  }

  object Ticket extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(Ticket.apply)
  }

}
