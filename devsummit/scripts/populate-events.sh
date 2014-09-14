#!/bin/bash

echo "Populating dev summit events"

http PUT localhost:5000/events event="Single code-base cross-platform native mobile development, Qian Wang, Chuong Mai" nrOfTickets=:50
http PUT localhost:5000/events event="Akka, Tim Reid, Alan Begley" nrOfTickets=:50
http PUT localhost:5000/events event="Building Reactive Android Applications using BLE, Deepak Dhiman, Urvi Chetta" nrOfTickets:=50
http PUT localhost:5000/events event="Innovation, Sivanarayana Gaddam" nrOfTickets:=50


