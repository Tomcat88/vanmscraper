package it.introini.vanmscraper.manager

import com.google.inject.ImplementedBy
import it.introini.vanmscraper.manager.impl.VanmScrapeHelperImpl

@ImplementedBy(VanmScrapeHelperImpl::class)
interface VanmScrapeHelper {
    fun getAndSetMaxCode(buffer: Int): Pair<Int, Int>
}