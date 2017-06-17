package it.introini.vanmscraper.manager

import com.google.inject.ImplementedBy
import it.introini.vanmscraper.manager.impl.VanmScrapeHelperImpl
import org.jsoup.nodes.Document

@ImplementedBy(VanmScrapeHelperImpl::class)
interface VanmScrapeHelper {

    fun getScrapeRequests(buffer: Int): Collection<String>
    fun getAndSetMaxCode(buffer: Int): Pair<Int, Int>
    fun getDocument(url: String): Document?
    fun buildUrl(trip: String): String
    fun calculateHash(document: Document): String
}