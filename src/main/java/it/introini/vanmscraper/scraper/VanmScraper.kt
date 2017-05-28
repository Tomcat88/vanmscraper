package it.introini.vanmscraper.scraper

import com.google.inject.ImplementedBy
import it.introini.vanmscraper.model.VanmTrip
import it.introini.vanmscraper.scraper.impl.VanmScraperImpl
import org.jsoup.nodes.Document

@ImplementedBy(VanmScraperImpl::class)
interface VanmScraper {

    fun scrapeURL(trip: String): Pair<String, VanmTrip?>
    fun scrapeDocument(document: Document): VanmTrip?
    fun scrapeHTML(html: String): VanmTrip?
}