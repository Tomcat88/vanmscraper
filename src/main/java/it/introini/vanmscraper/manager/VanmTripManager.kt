package it.introini.vanmscraper.manager

import io.vertx.core.AsyncResultHandler
import it.introini.vanmscraper.model.DbVanmTrip
import it.introini.vanmscraper.model.VanmTrip
import java.time.Instant

interface VanmTripManager {
    fun maxCode(handler: AsyncResultHandler<Int?>)
    fun trips(handler: AsyncResultHandler<Collection<DbVanmTrip>>)
    fun scrapedOn(code: String, handler: AsyncResultHandler<Instant?>)

    fun insert(now : Instant, code: String, url: String, trip: VanmTrip, handler: AsyncResultHandler<Void>)
}