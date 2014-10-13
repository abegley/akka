package com.devsummit

import akka.actor.{PoisonPill, Actor}

class TicketSeller extends Actor {
  import TicketProtocol._

  //The list of tickets
  var tickets = Vector[Ticket]()

  def receive = {

    //Return the size of the list when GetEvents is received
    case GetEvents => sender ! tickets.size

    //add the new tickets to the existing list of tickets when Tickets message is received
    case Tickets(newTickets) => tickets = tickets ++ newTickets

    //Report SoldOut and kill self if there are no more tickets.  Otherwise get the head ticket and leave the rest in the tickets list.
    case BuyTicket =>
      if (tickets.isEmpty) {
        sender ! SoldOut
        self ! PoisonPill  //PoisonPill - cleans up the actor (deletes it) when it has no more work to do.
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

  //Message to create an event
  case class Event(event:String, nrOfTickets:Int)

  //Message for requesting the state of all events
  case object GetEvents

  //Response message that contains current status of all events
  case class Events(events:List[Event])

  //Signal event to indicate an event was created
  case object EventCreated

  //Request for a ticket for a particular event
  case class TicketRequest(event:String)

  //Signal event that the event is sold out
  case object SoldOut

  //New tickets for an Event, created by BoxOffice
  case class Tickets(tickets:List[Ticket])

  //Message to buy a ticket from the ticketSeller
  case object BuyTicket

  //The numbered ticket to an event
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


