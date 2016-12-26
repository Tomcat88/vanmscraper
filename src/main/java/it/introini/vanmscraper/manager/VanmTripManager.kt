package it.introini.vanmscraper.manager

import com.google.inject.ImplementedBy
import io.vertx.core.AsyncResultHandler
import it.introini.vanmscraper.manager.impl.VanmTripManagerImpl
import it.introini.vanmscraper.model.DbVanmTrip
import it.introini.vanmscraper.model.VanmTrip
import java.time.Instant

@ImplementedBy(VanmTripManagerImpl::class)
interface VanmTripManager {
    fun maxCode(handler: AsyncResultHandler<Int?>)
    fun trips(handler: AsyncResultHandler<Collection<DbVanmTrip>>)
    fun scrapedOn(code: String, handler: AsyncResultHandler<Instant?>)

    fun insert(now : Instant, code: String, url: String, trip: VanmTrip, handler: AsyncResultHandler<Void>)
    fun insertMissing(now: Instant, code: String, url: String, handler: AsyncResultHandler<Void>)
}