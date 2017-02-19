package it.introini.vanmscraper.app

import com.google.inject.Inject
import com.google.inject.Singleton
import io.vertx.core.Future
import io.vertx.core.Vertx
import it.introini.vanmscraper.config.Config
import org.pmw.tinylog.Logger

@Singleton class App @Inject constructor(val scraper: Scraper,
                                         val webServer: WebServer,
                                         val vertx: Vertx,
                                         val config: Config) {


    fun start(startFuture: Future<Void>) {
        Logger.info(config.jsonConfig.encodePrettily())
        startScraper()
        startWebServer()
        startFuture.complete()
    }

    private fun startWebServer() = webServer.start()
    private fun startScraper() = scraper.start()

}