package it.introini.vanmscraper.manager

import com.google.inject.ImplementedBy
import io.vertx.core.json.JsonObject
import it.introini.vanmscraper.manager.impl.VanmTripManagerImpl
import it.introini.vanmscraper.model.DbVanmTrip
import it.introini.vanmscraper.model.VanmTrip
import java.time.Instant

@ImplementedBy(VanmTripManagerImpl::class)
interface VanmTripManager {
    fun maxCode(): Int?
    fun trips(): Collection<DbVanmTrip>
    fun jsonTripByCode(code: String): JsonObject?
    fun search(name: String?): Collection<DbVanmTrip>
    fun scrapedOn(code: String): Instant?

    fun insert(now : Instant, code: String, url: String, trip: VanmTrip)
    fun insertMissing(now: Instant, code: String, url: String)
}