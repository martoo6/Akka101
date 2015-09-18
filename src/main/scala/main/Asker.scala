package main

import akka.actor.{Actor, ActorRef}
import main.Asker.{Tell, Ask}
import main.Counter.Total

/**
 * Created by martin on 17/09/15.
 */

object Asker{
	case object Ask
	case class Tell(number :Int)
}

class Asker(counter: ActorRef) extends Actor{
	override def receive: Receive = {
		case Ask => counter ! Total
		case Tell(number) => println(s"Asked and response was: $number")
	}
}
