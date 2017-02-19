package it.introini.vanmscraper.app

import com.google.inject.Inject
import io.vertx.core.Vertx
import it.introini.vanmscraper.config.Config
import it.introini.vanmscraper.www.RouterService
import org.pmw.tinylog.Logger

class WebServer @Inject constructor(val vertx: Vertx,
                                    val config: Config,
                                    val routerService: RouterService) {

    fun start() {
        val port = config.getInt("web.port", 8080)
        val httpServer = vertx.createHttpServer()
        httpServer.requestHandler{ routerService.router.accept(it) }
        httpServer.listen(port) {
            if (it.succeeded()) {
                routerService.wire()
                Logger.info("Http server up! listening on port $port")
            } else {
                Logger.error("Something went wrong while starting http server: ${it.cause().message}",it.cause())
            }
        }
    }
}