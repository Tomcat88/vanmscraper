package it.introini.vanmscraper.scraper

import com.google.inject.ImplementedBy
import it.introini.vanmscraper.model.VanmTrip
import it.introini.vanmscraper.scraper.impl.VanmScraperImpl

@ImplementedBy(VanmScraperImpl::class)
interface VanmScraper {

    fun scrape(trip: String): Pair<String, VanmTrip?>
}