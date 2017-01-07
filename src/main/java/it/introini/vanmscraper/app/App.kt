package it.introini.vanmscraper.app

import com.google.inject.Inject
import com.google.inject.Singleton
import io.vertx.core.Future
import io.vertx.core.Vertx
import it.introini.vanmscraper.config.Config
import it.introini.vanmscraper.manager.VanmScrapeHelper
import it.introini.vanmscraper.manager.VanmTripManager
import it.introini.vanmscraper.scraper.VanmScraper
import org.pmw.tinylog.Logger
import java.time.Instant

@Singleton class App @Inject constructor(val scraper: VanmScraper,
                                         val vertx: Vertx,
                                         val vanmTripManager: VanmTripManager,
                                         val vanmScrapeHelper: VanmScrapeHelper,
                                         config: Config) {

    val MAX_CODE: Int = config.getInt("app.max_code", 9999)
    val SCRAPE_BUFFER: Int = config.getInt("app.scrape_buffer", 20)
    val APP_DELAY: Long = config.getLong("app.delay", 10000)

    fun start(startFuture: Future<Void>) {
        vertx.setPeriodic(APP_DELAY, { e ->
            val (from, to) = vanmScrapeHelper.getAndSetMaxCode(SCRAPE_BUFFER)
            if (from == MAX_CODE) {
                //get old scraped
            } else {
                (from + 1..to).forEach { i ->
                    val code = String.format("%04d", i)
                    val vanmTrip = scraper.scrapeURL(code)
                    if (vanmTrip.second != null) {
                        vanmTripManager.insert(Instant.now(), code, vanmTrip.first, vanmTrip.second!!)
                        Logger.info("Succesfully inserted scraped trip ($code) url: (${vanmTrip.first})")
                    }
                }
            }
        })
        startFuture.complete()
    }
}