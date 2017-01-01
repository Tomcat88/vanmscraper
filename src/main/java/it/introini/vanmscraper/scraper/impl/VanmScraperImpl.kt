package it.introini.vanmscraper.scraper.impl

import com.google.inject.Inject
import it.introini.vanmscraper.config.Config
import it.introini.vanmscraper.model.VanmTrip
import it.introini.vanmscraper.model.VanmTripInfo
import it.introini.vanmscraper.model.VanmTripRate
import it.introini.vanmscraper.model.VanmTripSchedule
import it.introini.vanmscraper.scraper.VanmScraper
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities
import java.time.LocalDate
import java.util.*

class VanmScraperImpl @Inject constructor(val config: Config): VanmScraper {

    override fun scrapeHTML(html: String): VanmTrip? {
        val document = Jsoup.parse(html)
        document.outputSettings().charset(Charsets.ISO_8859_1)
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml)
        return scrape(document)
    }

    override fun scrapeURL(trip: String) : Pair<String, VanmTrip?> {
        val tripUrl = buildUrl(trip)
        try {
            val document = Jsoup.connect(tripUrl)
                    .userAgent("Mozilla")
                    .timeout(5000)
                    .get()

            return Pair(tripUrl, scrape(document))
        } catch (e: HttpStatusException) {
            println("Could not scrapeURL trip $trip, ${e.message}, ${e.statusCode}")
        }
        return Pair(tripUrl, null)
    }

    fun scrape(document: Document): VanmTrip {
        val tripRates = tripRates(document)
        val tripInfo = tripInfo(document)
        val tripSchedule = tripSchedule(document)
        return VanmTrip(tripInfo, tripRates, tripSchedule)
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

    private fun tripSchedule(doc: Document): List<VanmTripSchedule> {
        val schedule = doc.select("body>div>div>table")[0]
        return schedule.select("tbody>tr").drop(2).dropLast(3).map {
            val elements = it.select("td")
            val code = elements[0].select("div").html()
            val from = elements[1].select("div").html().split("-").let { LocalDate.of(it[2].toInt(), it[1].toInt(), it[0].toInt()) }
            val to = elements[2].select("div").html().split("-").let { LocalDate.of(it[2].toInt(), it[1].toInt(), it[0].toInt()) }
            val booked = elements[3].select("div").html()
            val info = elements[4].select("div").text()
            val open = elements[5].select("div>input").isNotEmpty()
            VanmTripSchedule(code.toInt(), from, to, booked.toInt(), info, open)
        }
    }

    private fun s(key:String): String = config.getString(key, "")
}