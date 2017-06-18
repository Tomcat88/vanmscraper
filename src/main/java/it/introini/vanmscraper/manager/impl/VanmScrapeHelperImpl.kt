package it.introini.vanmscraper.manager.impl

import com.google.inject.Inject
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import it.introini.vanmscraper.config.Config
import it.introini.vanmscraper.manager.VanmScrapeHelper
import org.bson.Document
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.pmw.tinylog.Logger
import java.security.MessageDigest
import java.time.Instant

class VanmScrapeHelperImpl @Inject constructor(val config:        Config,
                                                   mongoDatabase: MongoDatabase) : VanmScrapeHelper {
    val collection: MongoCollection<Document> = mongoDatabase.getCollection("scrape_helper")
    val requestCollection: MongoCollection<Document> = mongoDatabase.getCollection("requests")
    val MAX_CODE: Int = config.getInt("app.max_code", 9999)

    override fun insertScrapeRequest(now: Instant, code: String) {
        requestCollection.insertOne(Document("status", "INSERTED").append("code", code).append("requested_on", now.toString()))
    }

    override fun getScrapeRequests(buffer: Int): Collection<String> {
        return requestCollection.find(Document("status", "INSERTED")).limit(buffer).map { it.getString("code") }.toList()
    }

    override fun completeScrapeRequests(now: Instant, codes: Collection<String>) {
        requestCollection.updateMany(`in`("code", codes), combine(set("status", "COMPLETED"), set("completed_on", now.toString())))
    }

    override fun getAndSetMaxCode(buffer: Int): Pair<Int, Int> {
        val maxcode = collection.find(Document("param", "max_code")).limit(1).firstOrNull()
        val current = maxcode?.getInteger("value") ?: 0
        val from = if (current > MAX_CODE) 0 else current
        val to = from + buffer
        collection.updateOne(eq("param", "max_code"), Document("\$set", Document("value", to)), UpdateOptions().upsert(true))
        return Pair(from, to)
    }


    override fun getDocument(url: String): org.jsoup.nodes.Document? {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla")
                    .timeout(5000)
                    .get()
        }  catch (e: HttpStatusException) {
            Logger.error("Could not scrapeURL trip $url, ${e.message}, ${e.statusCode}")
        } catch (t: Throwable) {
            Logger.error(t, "Generic exception")
        }
        return null
    }

    override fun calculateHash(document: org.jsoup.nodes.Document): String {
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(document.html().toByteArray())
        return messageDigest.digest().toString(Charsets.UTF_8)
    }

    override fun buildUrl(trip:String): String {
        val base = config.getString("vanm.base_url", "")
        val tripBase = config.getString("vanm.trip_url", "")
        val ext = config.getString("vanm.file_ext", "")
        return "$base$tripBase$trip$ext"
    }

}