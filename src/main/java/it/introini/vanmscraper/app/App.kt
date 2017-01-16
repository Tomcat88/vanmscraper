package it.introini.vanmscraper.app

import com.google.inject.Inject
import com.google.inject.Singleton
import io.vertx.core.Future
import io.vertx.core.Vertx

@Singleton class App @Inject constructor(val scraper: Scraper,
                                         val webServer: WebServer,
                                         val vertx: Vertx) {


    fun start(startFuture: Future<Void>) {
        startScraper()
        startWebServer()
        startFuture.complete()
    }

    private fun startWebServer() = webServer.start()
    private fun startScraper() = scraper.start()

}