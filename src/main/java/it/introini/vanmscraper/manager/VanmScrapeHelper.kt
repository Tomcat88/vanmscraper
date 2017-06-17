package it.introini.vanmscraper.manager

import com.google.inject.ImplementedBy
import it.introini.vanmscraper.manager.impl.VanmScrapeHelperImpl
import org.jsoup.nodes.Document
import java.time.Instant

@ImplementedBy(VanmScrapeHelperImpl::class)
interface VanmScrapeHelper {

    fun insertScrapeRequest(now: Instant, code: String)
    fun getScrapeRequests(buffer: Int): Collection<String>
    fun completeScrapeRequests(now: Instant, codes: Collection<String>)
    fun getAndSetMaxCode(buffer: Int): Pair<Int, Int>
    fun getDocument(url: String): Document?
    fun buildUrl(trip: String): String
    fun calculateHash(document: Document): String
}