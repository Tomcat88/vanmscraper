package it.introini.vanmscraper.www

import com.google.inject.Inject
import com.google.inject.Injector
import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.introini.vanmscraper.www.handlers.CORSHandler
import it.introini.vanmscraper.www.handlers.ScrapeRequestHandler
import it.introini.vanmscraper.www.handlers.TripHandler
import it.introini.vanmscraper.www.handlers.TripSearchHandler

class RouterService @Inject constructor(val router: Router,
                                        val injector: Injector){

    val BASE_ROUTE           = "/vanm"
    val TRIP_ROUTE           = "/trip/:code"
    val SEARCH_ROUTE         = "/trips/search"
    val SCRAPE_REQUEST_ROUTE = "/scrape/:code"

    fun wire() {
        router.route("$BASE_ROUTE/*").handler(BodyHandler.create())
        router.route("$BASE_ROUTE/*").handler(injector.getInstance(CORSHandler::class.java))
        router.options("$BASE_ROUTE/*").handler({ e -> e.response().end() })
        router.route("$BASE_ROUTE$TRIP_ROUTE").blockingHandler(injector.getInstance(TripHandler::class.java))
        router.get("$BASE_ROUTE$SEARCH_ROUTE").blockingHandler(injector.getInstance(TripSearchHandler::class.java))
        router.put("$BASE_ROUTE$SCRAPE_REQUEST_ROUTE").blockingHandler(injector.getInstance(ScrapeRequestHandler::class.java))
    }
}