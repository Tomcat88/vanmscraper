package it.introini.vanmscraper.guice

import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Provides
import com.google.inject.Singleton
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

class AppModule : AbstractModule() {
    override fun configure() { }

    @Singleton @Provides fun vertx(): Vertx = Vertx.currentContext().owner()
    @Singleton @Provides @Inject fun config(vertx: Vertx): JsonObject = Vertx.currentContext().config()
    @Singleton @Provides @Inject fun mongoClient(vertx: Vertx, config: JsonObject): MongoClient {
        val mongoConfig = JsonObject()
        mongoConfig.put("connection_string", config.getString("mongo.connection_string"))
        return MongoClient.createShared(vertx, config)
    }
}