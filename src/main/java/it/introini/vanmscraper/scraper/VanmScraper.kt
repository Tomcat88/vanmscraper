package it.introini.vanmscraper.scraper

import com.google.inject.ImplementedBy
import it.introini.vanmscraper.model.VanmTrip
import it.introini.vanmscraper.scraper.impl.VanmScraperImpl

@ImplementedBy(VanmScraperImpl::class)
interface VanmScraper {

    fun scrapeURL(trip: String): Pair<String, VanmTrip?>
    fun scrapeHTML(html: String): VanmTrip?
}