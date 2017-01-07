package it.introini.vanmscraper.manager.impl

import com.google.inject.Inject
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOptions
import it.introini.vanmscraper.manager.VanmScrapeHelper
import org.bson.Document

class VanmScrapeHelperImpl @Inject constructor(mongoDatabase: MongoDatabase) : VanmScrapeHelper {

    val collection: MongoCollection<Document> = mongoDatabase.getCollection("scrape_helper")

    override fun getAndSetMaxCode(buffer: Int): Pair<Int, Int> {
        val maxcode = collection.find(Document("param", "max_code")).limit(1).firstOrNull()
        val current = maxcode?.getInteger("value") ?: 0
        collection.updateOne(eq("param", "max_code"), Document("\$set", Document("value", current + buffer)), UpdateOptions().upsert(true))
        return Pair(current, current + buffer)
    }
}