package it.introini.vanmscraper.www.handlers

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext


class CORSHandler : Handler<RoutingContext> {
    override fun handle(event: RoutingContext) {
        event.response().putHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8080")
        event.response().putHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE")
        event.next()
    }
}