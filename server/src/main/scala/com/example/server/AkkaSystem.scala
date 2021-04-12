package com.example.server

import akka.actor.typed.ActorSystem

trait AkkaSystem {
  implicit val actorSystem: ActorSystem[_]
}