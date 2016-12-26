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
import it.introini.vanmscraper.model.VanmTripInfo
import java.time.Instant

class VanmTripManagerImpl @Inject constructor(val mongoClient: MongoClient) : VanmTripManager {

    val tripMapper: (JsonObject) -> DbVanmTrip = { json ->
        val tripObj = json.getJsonObject("trip")
        val tripInfoObj = tripObj.getJsonObject("infos")
        val tripInfo = VanmTripInfo(
                tripInfoObj.getString("name"),
                tripInfoObj.getString("description"),
                tripInfoObj.getString("duration"),
                tripInfoObj.getString("period"),
                tripInfoObj.getString("overnights"),
                tripInfoObj.getString("transports"),
                tripInfoObj.getString("meals"),
                tripInfoObj.getString("difficulty"),
                tripInfoObj.getString("visas"),
                tripInfoObj.getString("infos")
        )
        DbVanmTrip(
                json.getInteger("code"),
                json.getString("str_code"),
                json.getString("url"),
                json.getInstant("scraped_on"),
                VanmTrip(tripInfo)
        )
    }

    override fun maxCode(handler: AsyncResultHandler<Int?>) {
        mongoClient.findWithOptions(
                "trip",
                JsonObject(),
                FindOptions().setSort(JsonObject().put("code", -1))
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
        mongoClient.find("trip", JsonObject(), {
            if (it.succeeded()) {
                handler.handle(Future.succeededFuture(it.result().map(tripMapper)))
            } else {
                handler.handle(Future.failedFuture(it.cause()))
            }
        })
    }

    override fun scrapedOn(code: String, handler: AsyncResultHandler<Instant?>) {
        mongoClient.findOne("trip", JsonObject().put("code", code), JsonObject().put("scraped_on", 1),  {
            if (it.succeeded()) {
                handler.handle(Future.succeededFuture(it.result().getInstant("scraped_on")))
            } else {
                handler.handle(Future.failedFuture(it.cause()))
            }
        })
    }

    override fun insert(now: Instant, code: String, url: String, trip: VanmTrip, handler: AsyncResultHandler<Void>) {
        val jsonTrip = JsonObject()
                              .put("str_code", code)
                              .put("code", code.toInt())
                              .put("url", url)
                              .put("trip", JsonObject().put("info",
                                                            JsonObject().put("name", trip.infos.name)
                                                                        .put("description", trip.infos.description)
                                                                        .put("duration", trip.infos.duration)
                                                                        .put("period", trip.infos.period)
                                                                        .put("overnights", trip.infos.overnights)
                                                                        .put("transports", trip.infos.transports)
                                                                        .put("meals", trip.infos.meals)
                                                                        .put("difficulty", trip.infos.difficulty)
                                                                        .put("visas", trip.infos.visas)
                                                                        .put("infos", trip.infos.infos)))

        mongoClient.insert("trip",jsonTrip, {
            if (it.succeeded()) {
                handler.handle(Future.succeededFuture())
            } else {
                handler.handle(Future.failedFuture(it.cause()))
            }
        })
    }

    override fun insertMissing(now: Instant, code: String, url: String, handler: AsyncResultHandler<Void>) {
        val jsonTrip = JsonObject()
                              .put("str_code", code)
                              .put("code", code.toInt())
                              .put("url", url)
                              .put("trip", JsonObject())
        mongoClient.insert("trip", jsonTrip, {
            if (it.succeeded()) {
                handler.handle(Future.succeededFuture())
            } else {
                handler.handle(Future.failedFuture(it.cause()))
            }

        })
    }


}