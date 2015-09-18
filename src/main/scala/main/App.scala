package main

import akka.actor.{ActorSystem, Props}
import main.SourceFeeder.Feed

/**
 * Created by martin on 15/09/15.
 */
object App {
	def main(args :Array[String]): Unit ={
		val system      = ActorSystem("ScrapperSystem")
		val sourceFeeder = system.actorOf(Props[SourceFeeder], "SourceFeeder")
		sourceFeeder ! Feed("http://ohsheglows.com", "http://minimalistbaker.com", "http://www.veganricha.com")

		//system.shutdown()
	}
}
