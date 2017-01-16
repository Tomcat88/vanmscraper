package it.introini.vanmscraper.app

import com.google.inject.Inject
import io.vertx.core.Vertx
import it.introini.vanmscraper.config.Config

class WebServer @Inject constructor(val vertx: Vertx,
                                    val config: Config) {

    fun start() {
        val httpServer = vertx.createHttpServer()

        httpServer.requestHandler { r ->
            r.response().end("Hello!")
        }

        httpServer.listen(config.getInt("web.port", 8080))
    }
}