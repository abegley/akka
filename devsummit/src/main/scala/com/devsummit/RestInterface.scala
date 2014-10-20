package com.devsummit

import akka.actor._

import spray.routing._
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport._
import spray.routing.RequestContext
import akka.util.Timeout
import scala.concurrent.duration._

/*
The REST Interface Actor will handle the HTTP requests. It is basically an adapter for HTTP:
it takes care of converting from and to JSON and provides the required HTTP response.

The REST Interface creates a BoxOffice child actor when it is constructed. It also creates a
temporary Responder actor which stays around for the lifetime of the HTTP request.
This responder sends messages to the BoxOffice and handles the responses that are sent back
from the TicketSeller and BoxOffice actors.

1. First the actor system is created
2. The REST interface is the top level actor in our App.
3. The REST interface creates one BoxOffice actor.
4. The BoxOffice creates a TicketSeller per event.
 */


class RestInterface extends HttpServiceActor
                    with RestApi {
  def receive = runRoute(routes)
}

trait RestApi extends HttpService with ActorLogging { actor: Actor =>
  import context.dispatcher
  import com.devsummit.TicketProtocol._

  implicit val timeout = Timeout(10 seconds)
  import akka.pattern.ask
  import akka.pattern.pipe

  val boxOffice = context.actorOf(Props[BoxOffice])  //Created the BoxOffice Actor

  def routes: Route =

    path("events") {
      put {
        entity(as[Event]) { event => requestContext =>
          val responder = createResponder(requestContext)
          boxOffice.ask(event).pipeTo(responder)
        }
      } ~
      get { requestContext =>
        val responder = createResponder(requestContext)
        boxOffice.ask(GetEvents).pipeTo(responder)
      }
    } ~
    //Notes: no specific code for adapting messages into objects
    path("ticket") {
      get {
        entity(as[TicketRequest]) { ticketRequest => requestContext =>      //The request entity is unmarshalled into a TicketRequest object
          val responder = createResponder(requestContext)                   //We make the responder
          boxOffice.ask(ticketRequest).pipeTo(responder)                    //When we are done, the ticketRequest is sent to the responder. The responder completes the HTTP request when it receives the Ticket or SoldOut response from the the TicketSeller
        }
      }
    } ~
    path("ticket" / Segment) { eventName => requestContext =>
      val req = TicketRequest(eventName)
      val responder = createResponder(requestContext)
      boxOffice.ask(req).pipeTo(responder)
    }
  //Created the responder
  def createResponder(requestContext:RequestContext) = {
    context.actorOf(Props(new Responder(requestContext, boxOffice)))
  }

}

class Responder(requestContext:RequestContext, ticketMaster:ActorRef) extends Actor with ActorLogging {
  import TicketProtocol._
  import spray.httpx.SprayJsonSupport._

  def receive = {

    case ticket:Ticket =>
      requestContext.complete(StatusCodes.OK, ticket)
      self ! PoisonPill               //The responder kills itself after it has completed the request. It's only around for as long as the HTTP request is being processed.

    case EventCreated =>
      requestContext.complete(StatusCodes.OK)
      self ! PoisonPill

    case SoldOut =>
      requestContext.complete(StatusCodes.NotFound)     //standard HTTP not found when we run out of Tickets to a gvien event.
      self ! PoisonPill

    case Events(events) =>
      requestContext.complete(StatusCodes.OK, events)
      self ! PoisonPill

  }
}