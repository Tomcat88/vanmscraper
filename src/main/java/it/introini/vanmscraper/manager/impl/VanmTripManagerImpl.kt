package it.introini.vanmscraper.manager.impl

import com.google.inject.Inject
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import io.vertx.core.json.JsonObject
import it.introini.vanmscraper.manager.VanmTripManager
import it.introini.vanmscraper.model.*
import org.bson.Document
import java.time.Instant

class VanmTripManagerImpl @Inject constructor(mongoClient: MongoDatabase) : VanmTripManager {

    val collection: MongoCollection<Document> = mongoClient.getCollection("trip")

    val tripMapper: (Document) -> DbVanmTrip = { json ->
        val jsonObject = JsonObject(json.toJson())
        val tripObj = jsonObject.getJsonObject("trip")
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
                tripInfoObj.getString("infos"),
                tripInfoObj.getJsonArray("classifications").map(Any?::toString),
                tripInfoObj.getJsonArray("countries").map(Any?::toString)
        )
        val tripRatesObj = tripInfoObj.getJsonArray("rates")
        val tripRates = (0..tripRatesObj.size()).map { i ->
            val e = tripRatesObj.getJsonObject(i)
            VanmTripRate(RateType.valueOf(e.getString("type")), e.getString("description"), e.getString("currency"), e.getDouble("price"))
        }
        DbVanmTrip(
                jsonObject.getInteger("code"),
                jsonObject.getString("str_code"),
                jsonObject.getString("url"),
                TripStatus.valueOf(jsonObject.getString("status")),
                jsonObject.getInstant("scraped_on"),
                VanmTrip(tripInfo, tripRates)
        )
    }

    override fun maxCode(): Int? {
        val document = collection.find().sort(Document("code", -1)).limit(1).projection(Document("code", 1)).firstOrNull()
        return document?.getInteger("code")
    }

    override fun trips(): Collection<DbVanmTrip> = collection.find().map(tripMapper).toList()

    override fun scrapedOn(code: String): Instant? = collection.find(Document("code", code)).projection(Document("scraped_on", 1)).limit(1).firstOrNull()?.getString("scraped_on").let(Instant::parse)

    override fun insert(now: Instant, code: String, url: String, trip: VanmTrip) {
        val jsonTrip = Document("str_code", code)
                              .append("code", code.toInt())
                              .append("scraped_on", now.toString())
                              .append("status", TripStatus.OK.name)
                              .append("url", url)
                              .append("rates", trip.rates.map { Document("type", it.rateType.name).append("description", it.description).append("currency", it.currency).append("price", it.price) })
                              .append("trip", Document("info", Document("name", trip.infos.name)
                                                                        .append("description", trip.infos.description)
                                                                        .append("duration", trip.infos.duration)
                                                                        .append("period", trip.infos.period)
                                                                        .append("overnights", trip.infos.overnights)
                                                                        .append("transports", trip.infos.transports)
                                                                        .append("meals", trip.infos.meals)
                                                                        .append("difficulty", trip.infos.difficulty)
                                                                        .append("visas", trip.infos.visas)
                                                                        .append("infos", trip.infos.infos)))
         collection.insertOne(jsonTrip)
    }

    override fun insertMissing(now: Instant, code: String, url: String) {
        val jsonTrip = JsonObject()
                              .put("str_code", code)
                              .put("code", code.toInt())
                              .put("scraped_on", now)
                              .put("status", TripStatus.MISSING.name)
                              .put("url", url)
                              .put("trip", JsonObject())
        collection.insertOne(Document(jsonTrip.map))
    }


}