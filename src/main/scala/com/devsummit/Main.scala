package com.devsummit

import akka.actor._
import akka.io.IO

import spray.can.Http
import spray.can.server._

import com.typesafe.config.ConfigFactory

object Main extends App {
  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  implicit val system = ActorSystem("devsummit")

  //Creates the top level REST Interface actor
  val api = system.actorOf(Props(new RestInterface()), "httpInterface")
  //Start a Spray HTTP server
  IO(Http) ! Http.Bind(listener = api, interface = host, port = port)
}
