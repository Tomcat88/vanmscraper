package it.introini.vanmscraper.scraper.impl

import com.google.inject.Inject
import io.vertx.core.json.JsonObject
import it.introini.vanmscraper.model.VanmTrip
import it.introini.vanmscraper.scraper.VanmScraper
import it.introini.vanmscraper.model.VanmTripInfo
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities
import java.util.*

class VanmScraperImpl @Inject constructor(val config: JsonObject): VanmScraper {

    override fun scrape(trip: String) : Pair<String, VanmTrip?> {
        val tripUrl = buildUrl(trip)
        try {
            val document = Jsoup.connect(tripUrl)
                    .userAgent("Mozilla")
                    .timeout(5000)
                    .get()
            document.outputSettings().charset(Charsets.ISO_8859_1)
            document.outputSettings().escapeMode(Entities.EscapeMode.xhtml)
            val tripInfo = tripInfo(document)
            println(tripInfo)
            return Pair(tripUrl, VanmTrip(tripInfo))
        } catch (e: HttpStatusException) {
            println("Could not scrape trip $trip, ${e.message}, ${e.statusCode}")
        }
        return Pair(tripUrl, null)
    }

    fun buildUrl(trip:String): String {
        val base = s("vanm.base_url")
        val tripBase = s("vanm.trip_url")
        val ext = s("vanm.file_ext")
        return "$base$tripBase$trip$ext"
    }

    fun tripInfo(doc: Document): VanmTripInfo {
        val tripInfo = doc.select(s("vanm.parse.info_table"))
        val name = tripInfo.select(s("vanm.parse.name"))
        val description = tripInfo.select(s("vanm.parse.desc"))
        val infoRows = tripInfo.select(s("vanm.parse.infos"))
        val durationKey = s("vanm.parse.infos.duration")
        val periodKey = s("vanm.parse.infos.period")
        val overnightsKey = s("vanm.parse.infos.overnights")
        val transportsKey = s("vanm.parse.infos.transports")
        val mealsKey = s("vanm.parse.infos.meals")
        val difficultyKey = s("vanm.parse.infos.meals")
        val visaKey = s("vanm.parse.infos.visa")
        val infosKey = s("vanm.parse.infos.infos")
        val map = HashMap<String,String>()
        listOf(durationKey, periodKey, overnightsKey, transportsKey, mealsKey, difficultyKey, visaKey, infosKey)
                .forEach { k ->
                    infoRows.filter { it.childNodeSize() > 1 && it.select(s("vanm.parse.infos.class")).text().contains(k)}
                            .map {  map.put(k, it.child(1).html()) }
                }
        return VanmTripInfo(
                name.text(),
                description.text(),
                map[durationKey],
                map[periodKey],
                map[overnightsKey],
                map[transportsKey],
                map[mealsKey],
                map[difficultyKey],
                map[visaKey],
                map[infosKey]
        )
    }

    private fun s(key:String): String = config.getString(key)
}