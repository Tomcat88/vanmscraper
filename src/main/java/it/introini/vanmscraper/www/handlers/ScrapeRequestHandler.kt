package it.introini.vanmscraper.www.handlers

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import it.introini.vanmscraper.manager.VanmScrapeHelper
import java.time.Instant
import javax.inject.Inject


class ScrapeRequestHandler @Inject constructor(val vanmScrapeHelper: VanmScrapeHelper): Handler<RoutingContext> {

    override fun handle(event: RoutingContext) {
        val now = Instant.now()
        event.response().putHeader(HttpHeaderNames.CONTENT_TYPE, "application/json")
        if (event.request().method() != HttpMethod.PUT) {
            event.response().statusCode = HttpResponseStatus.BAD_REQUEST.code()
            event.response().end(JsonObject().put("message", "method not allowed").encode())
            return
        }

        val code = event.request().getParam("code")
        if (code == null) {
            event.response().statusCode = HttpResponseStatus.BAD_REQUEST.code()
            event.response().end(JsonObject().put("message", "code is mandatory").encode())
            return
        }

        vanmScrapeHelper.insertScrapeRequest(now, code)
        event.response().end(JsonObject().put("code", code).encode())
    }
}