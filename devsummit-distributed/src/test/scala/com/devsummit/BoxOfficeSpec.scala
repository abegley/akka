package com.devsummit

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.{Props, ActorSystem}
import org.scalatest.WordSpecLike
import org.scalatest.matchers.MustMatchers

class BoxOfficeSpec extends TestKit(ActorSystem("testTickets"))
                       with WordSpecLike
                       with MustMatchers
                       with ImplicitSender
                       with StopSystemAfterAll {
  "The BoxOffice" must {

    "Create an event and get tickets from the correct Ticket Seller" in {
      import TicketProtocol._

      val ticketMaster = system.actorOf(Props[BoxOffice])
      ticketMaster ! Event("akka", 10)
      expectMsg(EventCreated)

      ticketMaster ! TicketRequest("akka")
      expectMsg(Ticket("akka", 1))

      ticketMaster ! TicketRequest("scala")
      expectMsg(SoldOut)

    }
  }
}
