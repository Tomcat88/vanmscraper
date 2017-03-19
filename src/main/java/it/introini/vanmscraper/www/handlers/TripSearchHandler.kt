package it.introini.vanmscraper.www.handlers

import com.google.inject.Inject
import io.vertx.core.Handler
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import it.introini.vanmscraper.manager.VanmTripManager


class TripSearchHandler @Inject constructor(val vanmTripManager: VanmTripManager) : Handler<RoutingContext> {

    override fun handle(event: RoutingContext) {
        val nameQuery = event.request().getParam("name")
        val search = vanmTripManager.search(nameQuery)
        event.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
        event.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8080")
        event.response().end(search.map(JsonObject::mapFrom).let(::JsonArray).encode())
    }
}