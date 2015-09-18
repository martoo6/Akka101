package main

import akka.actor.SupervisorStrategy.{Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props}
import akka.routing.RoundRobinRouter
import main.Counter.Increment
import main.ScrapperWorker.{Paused, Scrap, Urls}

import scala.concurrent.duration._

/**
 * Created by martin on 15/09/15.
 */

class Scrapper(counter: ActorRef) extends Actor {
	//val workers     = context.actorOf(RoundRobinPool(1).props(Props(classOf[ScrapperWorker],counter)))
	val workers     = context.actorOf(Props(classOf[ScrapperWorker],counter).withRouter(RoundRobinRouter(nrOfInstances = 10)))

	override val supervisorStrategy =
		OneForOneStrategy(maxNrOfRetries = 1000, withinTimeRange = 1 minute, false) {
			case _: ArithmeticException             => Resume
			case _: NullPointerException            => Restart
			case _: java.net.MalformedURLException  => Restart
			case _: IllegalArgumentException        => Stop
			case _: Exception                       => Restart
		}

	def receive = {
		case s: Scrap => workers ! s
		case Paused   => println(s"${sender.path} is paused right now")
	}
}

object ScrapperWorker{
	case class Scrap(url: String, word: String)
	case object Urls
	case object Paused
}

class ScrapperWorker(counter :ActorRef) extends Actor with HttpUtils{
	import context._

	val scrappedUrls = collection.mutable.ArrayBuffer[String]()
	context.system.eventStream.subscribe(self, Urls.getClass)

	def alive: Receive = {
		case Scrap(url, word) =>
			scrappedUrls += url
			if(get(url).contains(word)) counter ! Increment
//			if(Random.nextInt(10)>8) {
//				become(paused)
//				context.system.scheduler.scheduleOnce(3 seconds)(become(alive))
//			}
		case Urls => if(scrappedUrls.nonEmpty) println(s"Mi scrapped Urls are: $scrappedUrls - ${self.path}")//sender ! scrappedUrls
	}

	def paused:Receive = {
		case Scrap(url, word) => sender ! Paused
		case Urls             => if(scrappedUrls.nonEmpty) println(s"Mi scrapped Urls are: $scrappedUrls - ${self.path}")//sender ! scrappedUrls
	}

	override def receive: Receive = {
		case s: Scrap => become(alive); self ! s
	}
}