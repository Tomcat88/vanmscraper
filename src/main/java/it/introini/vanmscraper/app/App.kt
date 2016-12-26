package it.introini.vanmscraper.app

import com.google.inject.Inject
import com.google.inject.Singleton
import io.vertx.core.AsyncResultHandler
import io.vertx.core.Future
import io.vertx.core.Vertx
import it.introini.vanmscraper.manager.VanmTripManager
import it.introini.vanmscraper.scraper.VanmScraper
import java.time.Instant

@Singleton class App @Inject constructor(val scraper: VanmScraper,
                                         val vertx: Vertx,
                                         val vanmTripManager: VanmTripManager) {

    val MAX_CODE = 9999

    fun start(startFuture: Future<Void>) {
        vertx.setPeriodic(10000, { e ->
            vanmTripManager.maxCode(AsyncResultHandler { res ->
                if (res.succeeded()) {
                    var startingCode = res.result() ?: 0
                    if (startingCode == MAX_CODE) startingCode = 0
                    (startingCode..startingCode + 10).forEach { i ->
                        val code = String.format("%04d", i)
                        val vanmTrip = scraper.scrape(code)
                        if (vanmTrip.second != null) {
                            vanmTripManager.insert(Instant.now(), code, vanmTrip.first, vanmTrip.second!!, AsyncResultHandler {
                                if (it.succeeded()) {
                                    println("Succesfully scraped $code (${vanmTrip.first})")
                                } else {
                                    //error
                                }
                            })
                        } else {
                            vanmTripManager.insertMissing(Instant.now(), code, vanmTrip.first, AsyncResultHandler {
                                if (it.succeeded()) {
                                    println("Inserted missing $code")
                                } else {
                                    // error
                                }
                            })
                        }
                    }
                } else {
                    //log error
                }
            })
        })
        startFuture.complete()
    }
}