package it.introini.vanmscraper.www

import com.google.inject.Inject
import com.google.inject.Injector
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import it.introini.vanmscraper.www.handlers.TripHandler

class RouterService @Inject constructor(val router: Router,
                                        val injector: Injector){

    val BASE_ROUTE = "/vanm"
    val TRIP_ROUTE = "/trip/:code"

    fun wire() {
        router.route("$BASE_ROUTE/*").handler(BodyHandler.create())
        router.route("$BASE_ROUTE$TRIP_ROUTE").blockingHandler(injector.getInstance(TripHandler::class.java))
    }
}