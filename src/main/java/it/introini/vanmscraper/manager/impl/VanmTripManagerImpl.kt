package it.introini.vanmscraper.manager.impl

import com.google.inject.Inject
import io.vertx.core.AsyncResultHandler
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.FindOptions
import io.vertx.ext.mongo.MongoClient
import it.introini.vanmscraper.manager.VanmTripManager
import it.introini.vanmscraper.model.DbVanmTrip
import it.introini.vanmscraper.model.VanmTrip
import java.time.Instant

class VanmTripManagerImpl @Inject constructor(val mongoClient: MongoClient) : VanmTripManager{

    override fun maxCode(handler: AsyncResultHandler<Int?>) {
        mongoClient.findWithOptions(
                "trip",
                JsonObject(),
                FindOptions().setSort(JsonObject().put("code", 1))
                             .setLimit(1),
                {
                    if (it.succeeded()) {
                        handler.handle(Future.succeededFuture(it.result().getOrNull(0)?.getInteger("code")))
                    } else {
                        handler.handle(Future.failedFuture(it.cause()))
                    }
                })
    }

    override fun trips(handler: AsyncResultHandler<Collection<DbVanmTrip>>) {

    }

    override fun scrapedOn(code: String, handler: AsyncResultHandler<Instant?>) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insert(now: Instant, code: String, url: String, trip: VanmTrip, handler: AsyncResultHandler<Void>) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}