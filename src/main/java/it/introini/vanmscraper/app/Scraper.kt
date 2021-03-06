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
    val CHECK_REQUESTS_DELAY: Long    = config.getLong("app.check_requests_delay", 10000)
    val RESCRAPE_DAYS_THRESHOLD :Long = config.getLong("app.rescrape_days_threshold", 1)

    fun start() {
        startScraper()
        startCheckScrapeRequests()
    }

    fun startScraper() {
        if (config.getBoolean("app.scraper.enable", false)) {
            vertx.setPeriodic(APP_DELAY, this::scrapeEvent)
        } else {
            Logger.info("Scraper not enabled!")
        }
    }

    fun startCheckScrapeRequests() {
        if (config.getBoolean("app.check_scrape_requests.enable", false)) {
            vertx.setPeriodic(CHECK_REQUESTS_DELAY, this::checkScrapeRequest)
        } else {
            Logger.info("Scrape requests checker not enabled!")
        }
    }

    private fun scrapeEvent(e: Long) {
        val now = LocalDate.now()
        val (from, to) = vanmScrapeHelper.getAndSetMaxCode(SCRAPE_BUFFER)
        (from + 1..to).forEach { scrapeByCode(String.format("%04d", it), now) }
    }

    private fun checkScrapeRequest(e: Long) {
        val today = LocalDate.now()
        val now = Instant.now()
        Logger.info("Checking scrape requests...")
        val scrapeRequests = vanmScrapeHelper.getScrapeRequests(SCRAPE_BUFFER)
        if (scrapeRequests.isNotEmpty()) {
            Logger.info("...found ${scrapeRequests.size} requests")
            scrapeRequests.map {
                Logger.info("... scraping $it")
                Pair(it, scrapeByCode(it, today))
            }.filter {
                it.second
            }.map {
                it.first
            }.let {
                vanmScrapeHelper.completeScrapeRequests(now, it)
            }
        } else {
            Logger.info("... no request found")
        }
    }

    private fun scrapeByCode(code: String, now: LocalDate): Boolean {
        val trip = vanmTripManager.jsonTripByCode(code)
        var scraped = false
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
                        scraped = true
                    }
                }
            }
        }
        return scraped
    }


}