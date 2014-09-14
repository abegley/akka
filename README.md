[![Build Status](https://travis-ci.org/begleyalan/akka.svg?branch=master)](https://travis-ci.org/begleyalan/akka)

akka
=====
Sample akka application based on a ticket selling REST service which will allow customers to buy tickets to all sorts of events, concerts, sports games and the like.


Requirements for the REST interface
=====
Create an event with a number of tickets.  

  http PUT localhost:5000/events event=event1 nrOfTickets:=10

Get an overview of all events and the number of tickets available.

  http PUT localhost:5000/events

Purchase a ticket for an event.

  http GET localhost:5000/ticket/event1
  
Notes
====
httpie (https://github.com/jakubroztocil/httpie) is a friendly replacement to cURL and is used to generate each of the http requests  shown above.
