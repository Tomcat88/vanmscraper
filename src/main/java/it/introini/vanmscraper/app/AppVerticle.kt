package it.introini.vanmscraper.app

import com.google.inject.Guice
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import it.introini.vanmscraper.config.Config
import it.introini.vanmscraper.guice.AppModule
import org.bson.Document
import org.pmw.tinylog.Configurator
import org.pmw.tinylog.Level
import org.pmw.tinylog.Logger
import org.pmw.tinylog.writers.ConsoleWriter
import org.pmw.tinylog.writers.FileWriter

class AppVerticle : AbstractVerticle() {
    override fun start(startFuture: Future<Void>) {
        val injector = Guice.createInjector(AppModule())
        logInit(injector.getInstance(Config::class.java))
        createIndex(injector.getInstance(MongoDatabase::class.java))
        injector.getInstance(App::class.java).start(startFuture)
    }

    fun logInit(config: Config) {
        Configurator.currentConfig()
                    .writer(FileWriter(config.getString("log.file","vanmscraper.log")))
                    .writer(ConsoleWriter())
                    .formatPattern("[{date}] {level}: {class}.{method}()\t {message}")
                    .level(Level.DEBUG)
                    .activate()
    }

    fun createIndex(mongoDatabase: MongoDatabase) {
        val indexName = mongoDatabase.getCollection("trip").createIndex(Document("code", 1), IndexOptions().unique(true))
        Logger.debug("Created index on trip collection $indexName")
    }
}