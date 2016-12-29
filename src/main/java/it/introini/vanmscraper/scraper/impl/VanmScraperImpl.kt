package it.introini.vanmscraper.scraper.impl

import com.google.inject.Inject
import it.introini.vanmscraper.config.Config
import it.introini.vanmscraper.model.VanmTrip
import it.introini.vanmscraper.model.VanmTripInfo
import it.introini.vanmscraper.model.VanmTripRate
import it.introini.vanmscraper.scraper.VanmScraper
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities
import org.pmw.tinylog.Logger
import java.util.*

class VanmScraperImpl @Inject constructor(val config: Config): VanmScraper {

    override fun scrapeHTML(html: String): VanmTrip? {
        val document = Jsoup.parse(html)
        document.outputSettings().charset(Charsets.ISO_8859_1)
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml)
        val tripRates = tripRates(document)
        Logger.info(tripRates)
        val tripInfo = tripInfo(document)
        return VanmTrip(tripInfo, tripRates)
    }

    override fun scrapeURL(trip: String) : Pair<String, VanmTrip?> {
        val tripUrl = buildUrl(trip)
        try {
            val document = Jsoup.connect(tripUrl)
                    .userAgent("Mozilla")
                    .timeout(5000)
                    .get()

            val tripInfo = tripInfo(document)
            val tripRates = tripRates(document)
            return Pair(tripUrl, VanmTrip(tripInfo, tripRates))
        } catch (e: HttpStatusException) {
            println("Could not scrapeURL trip $trip, ${e.message}, ${e.statusCode}")
        }
        return Pair(tripUrl, null)
    }



    private fun buildUrl(trip:String): String {
        val base = s("vanm.base_url")
        val tripBase = s("vanm.trip_url")
        val ext = s("vanm.file_ext")
        return "$base$tripBase$trip$ext"
    }

    private fun tripInfo(doc: Document): VanmTripInfo {
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
                            .forEach{  map.put(k, it.child(1).html()) }
                }
        val tripInfo2 = doc.select(s("vanm.parse.info_table2"))
        val classifications = tripInfo2.select(s("vanm.parse.classifications")).map { it.attr("href").split("#").last() }
        val countries = tripInfo2.select(s("vanm.parse.countries")).map { it.attr("alt") }
        //Logger.info(countries)

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
                map[infosKey],
                classifications,
                countries
        )
    }

    private fun tripRates(doc: Document): List<VanmTripRate> {
        val ratesTbody = doc.select("body>div>div>table>tbody")[1] // nth-child(1) doesn't work here
        val ratesTrs = ratesTbody.select("tr").drop(1)
        return ratesTrs.map { it.select("td") }.filter { it.size == 3 }.map { e ->
            val description = e[0].html()
            val currency = e[1].select("b").html()
            val price = e[2].select("div>b").html().replace(".", "").toDouble()
            if (description.toLowerCase().contains(s("vanm.parse.rates.extra_key"))) {
                VanmTripRate.VanmTripExtra(description, currency, price)
            } else if (description.toLowerCase().contains(s("vanm.parse.rates.city_prefix"))) {
                VanmTripRate.VanmTripCity(description.replace(s("vanm.parse.rates.city_prefix"), ""), currency, price)
            } else {
                null
            }
        }.filter { it != null }.map { it!! }
    }

    private fun s(key:String): String = config.getString(key, "")
}