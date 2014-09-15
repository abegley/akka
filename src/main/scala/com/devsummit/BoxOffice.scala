package com.devsummit


import akka.actor._
import concurrent.Future
import scala.concurrent.duration._
import akka.util.Timeout
/*
The BoxOffice needs to create TicketSeller children for every event and delegates the selling to them.
 */
class BoxOffice extends Actor with CreateTicketSellers with ActorLogging {
  import TicketProtocol._
  import context._
  implicit val timeout = Timeout(5 seconds)

  def receive = {

    case Event(name, nrOfTickets) =>
      log.info(s"Creating new event ${name} with ${nrOfTickets} tickets.")
      //if ticket sellers have not been created already -- Notice that it uses it's context instead of the actor system
      //to create the actor; Actors created with the context of another Actor are its children and subject to the parent
      //Actor's supervision
      if(context.child(name).isEmpty) {
        val ticketSeller = createTicketSeller(name)

        val tickets = Tickets((1 to nrOfTickets).map(nr=> Ticket(name, nr)).toList)
        ticketSeller ! tickets
      }

      sender ! EventCreated

    case TicketRequest(name) =>
      log.info(s"Getting a ticket for the ${name} event.")
      //BoxOffice tries to find a child TicketSeller for the event
      context.child(name) match {
        case Some(ticketSeller) => ticketSeller.forward(BuyTicket) //if it finds it, it forwards a BuyTicket message
        case None               => sender ! SoldOut                 //if there is no child for the event it sends a SoldOut message back to the sender
      }

    case GetEvents =>
      import akka.pattern.ask

      val capturedSender = sender

      def askAndMapToEvent(ticketSeller:ActorRef) =  {   //A local method definition for asking GetEvents to a TicketSeller.

        val futureInt = ticketSeller.ask(GetEvents).mapTo[Int] //Ask for the number of tickets that are left without waiting. The futureInt will at some point get the value.

        futureInt.map(nrOfTickets => Event(ticketSeller.actorRef.path.name, nrOfTickets))  //transform the future value from an Int to an Event.
      }
      val futures = context.children.map(ticketSeller => askAndMapToEvent(ticketSeller)) //Ask all children how many tickets they have left for the event.

      Future.sequence(futures).map { events =>
        capturedSender ! Events(events.toList)
      }

  }

}

trait CreateTicketSellers { self:Actor =>
  def createTicketSeller(name:String) =  context.actorOf(Props[TicketSeller], name)
}

/*
What is happening here is that an ask method returns immediately with a Future to the response.
A Future is a value that is going to be available at some point in the future (hence the name).
Instead of waiting for the response value (the number of tickets left) we get a future reference (which you could read
as 'use for future reference'). We never read the value directly but instead we define what should
happen once the value becomes available. We can combine a list of Future values into one list
of values and describe what should happen with said list once all of the asynchronous operations complete.
The code finally sends an Events message back to the sender once all responses have been handled.
 */