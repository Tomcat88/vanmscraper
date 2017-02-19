package it.introini.vanmscraper.www.handlers

import com.google.inject.Inject
import io.vertx.core.Handler
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import it.introini.vanmscraper.manager.VanmTripManager


class TripHandler @Inject constructor(val tripManager: VanmTripManager): Handler<RoutingContext> {

    override fun handle(event: RoutingContext) {
        val code = event.request().getParam("code")
        if (code == null) {
            event.response().setStatusCode(400).end("code is mandatory")
        } else {
            val trip = tripManager.jsonTripByCode(code) ?: JsonObject()
            trip.remove("_id")
            event.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(trip.encode())
        }
    }
}