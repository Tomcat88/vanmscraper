package it.introini.vanmscraper.app

import com.google.inject.Inject
import io.vertx.core.Vertx
import it.introini.vanmscraper.config.Config
import it.introini.vanmscraper.manager.VanmScrapeHelper
import it.introini.vanmscraper.manager.VanmTripManager
import it.introini.vanmscraper.scraper.VanmScraper
import org.pmw.tinylog.Logger
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Scraper @Inject constructor(val vertx: Vertx,
                                  val vanmTripManager: VanmTripManager,
                                  val vanmScrapeHelper: VanmScrapeHelper,
                                  val vanmScraper: VanmScraper,
                                  val config: Config) {

    val SCRAPE_BUFFER: Int            = config.getInt("app.scrape_buffer", 20)
    val APP_DELAY: Long               = config.getLong("app.delay", 60000)
    val RESCRAPE_DAYS_THRESHOLD :Long = config.getLong("app.rescrape_days_threshold", 1)

    fun start() {
        if (config.getBoolean("app.scraper.enable", false)) {
            vertx.setPeriodic(APP_DELAY, this::scrapeEvent)
        } else {
            Logger.info("Scraper not enabled!")
        }
    }

    private fun scrapeEvent(e: Long) {
        val now = LocalDate.now()
        val (from, to) = vanmScrapeHelper.getAndSetMaxCode(SCRAPE_BUFFER)
        (from + 1..to).forEach { scrapeByCode(String.format("%04d", it), now) }
    }

    private fun scrapeByCode(code: String, now: LocalDate) {
        val trip = vanmTripManager.jsonTripByCode(code)
        val scrapedOn = trip?.getInstant("scraped_on")?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.MIN
        if (now.isAfter(scrapedOn.plusDays(RESCRAPE_DAYS_THRESHOLD))) {
            val hash = trip?.getString("hash")
            val tripUrl = vanmScrapeHelper.buildUrl(code)
            val doc = vanmScrapeHelper.getDocument(tripUrl)
            if (doc != null) {
                val calculatedHash = vanmScrapeHelper.calculateHash(doc)
                if (hash == calculatedHash) {
                    Logger.info("Trip {} has not changed since last scrape ({}), skipping...", code, scrapedOn)
                } else {
                    val vanmTrip = vanmScraper.scrapeDocument(doc)
                    if (vanmTrip != null) {
                        vanmTripManager.insert(Instant.now(), code, tripUrl, calculatedHash, vanmTrip)
                        Logger.info("Succesfully inserted scraped trip ($code) url: ($tripUrl)")
                    }
                }
            }
        }
    }


}