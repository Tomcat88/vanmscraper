package it.introini.vanmscraper.app

import com.google.inject.Guice
import com.google.inject.Key
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import it.introini.vanmscraper.guice.AppModule

class AppVerticle : AbstractVerticle() {
    override fun start(startFuture: Future<Void>) {
        val injector = Guice.createInjector(AppModule())
        val app = injector.getInstance(Key.get(App::class.java))
        app.start(startFuture)
    }
}