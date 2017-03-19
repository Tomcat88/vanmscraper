package it.introini.vanmscraper.www

import com.google.inject.Inject
import com.google.inject.Injector
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.introini.vanmscraper.www.handlers.TripHandler
import it.introini.vanmscraper.www.handlers.TripSearchHandler

class RouterService @Inject constructor(val router: Router,
                                        val injector: Injector){

    val BASE_ROUTE = "/vanm"
    val TRIP_ROUTE = "/trip/:code"
    val SEARCH_ROUTE = "/trips/search"

    fun wire() {
        router.route("$BASE_ROUTE/*").handler(BodyHandler.create())
        router.route("$BASE_ROUTE$TRIP_ROUTE").blockingHandler(injector.getInstance(TripHandler::class.java))
        router.route("$BASE_ROUTE$SEARCH_ROUTE").blockingHandler(injector.getInstance(TripSearchHandler::class.java))
    }
}