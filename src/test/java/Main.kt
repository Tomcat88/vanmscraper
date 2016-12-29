import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Provides
import com.google.inject.Singleton
import io.vertx.core.json.JsonObject
import it.introini.vanmscraper.scraper.VanmScraper
import sun.misc.IOUtils

fun main(args: Array<String>) {
    val injector = Guice.createInjector(TestModule())
    val scraper = injector.getInstance(VanmScraper::class.java)
    val resourceAsStream = TestModule::class.java.getResourceAsStream("./3810.html")
    val html = String(IOUtils.readFully(resourceAsStream, resourceAsStream.available(), true))
    scraper.scrapeHTML(html)
}

class TestModule : AbstractModule() {
    override fun configure() {
    }
    @Singleton @Provides fun config(): JsonObject {
        val resourceAsStream = javaClass.getResourceAsStream("./config.json")
        val readFully = IOUtils.readFully(resourceAsStream, resourceAsStream.available(), true)
        return JsonObject(String(readFully))
    }
}