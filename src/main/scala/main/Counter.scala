package main

import akka.actor.Actor
import main.Asker.Tell
import main.Counter.{Increment, Total}

/**
 * Created by martin on 17/09/15.
 */

object Counter{
	case object Increment
	case class Total(total: Int)
}

class Counter extends Actor{
	var c = 0

	override def receive: Receive = {
		case Total => sender ! Tell(c)
		case Increment => c += 1
	}
}
