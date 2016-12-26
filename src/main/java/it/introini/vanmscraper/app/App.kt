package it.introini.vanmscraper.app

import com.google.inject.Inject
import com.google.inject.Singleton
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.mongo.MongoClient
import it.introini.vanmscraper.scraper.VanmScraper

@Singleton class App @Inject constructor(val scraper: VanmScraper,
                                         val vertx: Vertx,
                                         val mongoClient: MongoClient) {

    fun start(startFuture: Future<Void>) {
        vertx.setPeriodic(5000, {
            (1..20).forEach {
                val code = String.format("%04d", it)
                scraper.scrape(code)

            }
        })
        startFuture.complete()
    }
}