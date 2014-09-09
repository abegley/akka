package com.goticks

import akka.actor.{Props, ActorSystem}
import akka.io.IO
import com.typesafe.config.ConfigFactory
import spray.can.Http

/**
 * Created by alan on 09/09/14.
 */
object Main extends App{
  val config = ConfigFactory.load();
  val host = config.getString("http.port");
  val port = config.getInt("http.port");

  implicit val system = ActorSystem("goticks");

  val api = system.actorOf(Props(new RestInterface()), "httpInterface")
  IO(Http) ! Http.Bind(listener = api, interface = host, port = port)

}
