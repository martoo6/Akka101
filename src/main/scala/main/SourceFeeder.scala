package main

import akka.actor.{Actor, Props}
import main.Asker.Ask
import main.SourceFeeder.Feed
import scala.concurrent.duration._
import main.ScrapperWorker.{Scrap, Urls}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by martin on 17/09/15.
 */
object SourceFeeder{
	case class Feed(lstSrc :String*)
}

class SourceFeeder extends Actor{
	val r = "href=\"http://[A-Za-z0-9./]*\"".r

	val counter   = context.actorOf(Props[Counter], "Counter")
	val scrapper  = context.actorOf(Props(classOf[Scrapper],counter))
	val asker     = context.actorOf(Props(classOf[Asker], counter))

	context.system.scheduler.schedule(0 seconds, 1 seconds)(asker ! Ask)
	context.system.scheduler.schedule(0 seconds, 5 seconds)(context.system.eventStream.publish(Urls))

	override def receive: Receive = {
		case Feed(all @ _*) =>
			val p = 0
			context.system.scheduler.schedule(0 seconds, 5 seconds){
				if(p < all.length) {
					val url = all(p)
					val src = scala.io.Source.fromURL(url).mkString
					val lst = r.findAllIn(src).map(_.drop("href=\"".length).dropRight(1)).toList
					lst foreach (scrapper ! Scrap(_, "salad"))
				}
			}
	}
}
