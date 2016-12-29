package it.introini.vanmscraper.guice

import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Provides
import com.google.inject.Singleton
import com.mongodb.MongoClient
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import it.introini.vanmscraper.config.Config

class AppModule : AbstractModule() {
    override fun configure() { }

    @Singleton @Provides fun vertx(): Vertx = Vertx.currentContext().owner()
    @Singleton @Provides fun config(): JsonObject = Vertx.currentContext().config()
    @Singleton @Provides @Inject fun mongoClient(config: Config): MongoClient {
        val host = config.getString("mongo.host", "localhost")
        val port = config.getInt("mongo.port", 27017)
        return MongoClient(host, port)
    }

    @Singleton @Provides @Inject fun mongoDb(config: Config, mongoClient: MongoClient) = mongoClient.getDatabase(config.getString("mongo.db_name", "vanm_trips"))
}