package it.introini.vanmscraper.scraper.impl

import com.google.inject.Inject
import it.introini.vanmscraper.config.Config
import it.introini.vanmscraper.model.*
import it.introini.vanmscraper.scraper.VanmScraper
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities
import org.pmw.tinylog.Logger
import java.time.LocalDate
import java.util.*
import it.introini.vanmscraper.model.Currency

class VanmScraperImpl @Inject constructor(val config: Config): VanmScraper {

    val durationRegex = Regex("(?i)(\\d+) GIORNI")


    override fun scrapeHTML(html: String): VanmTrip? {
        val document = Jsoup.parse(html)
        document.outputSettings().charset(Charsets.ISO_8859_1)
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml)
        return scrape(document)
    }

    override fun scrapeDocument(document: Document): VanmTrip? = scrape(document)

    override fun scrapeURL(trip: String) : Pair<String, VanmTrip?> {
        val tripUrl = buildUrl(trip)
        try {
            val document = Jsoup.connect(tripUrl)
                    .userAgent("Mozilla")
                    .timeout(5000)
                    .get()

            document.html()
            return Pair(tripUrl, scrape(document))
        } catch (e: HttpStatusException) {
            Logger.error("Could not scrapeURL trip $trip, ${e.message}, ${e.statusCode}")
        } catch (t: Throwable) {
            Logger.error(t, "Generic exception")
        }
        return Pair(tripUrl, null)
    }

    private fun scrape(document: Document): VanmTrip {
        val tripRates = tripRates(document)
        val cashPool = cashPool(document)
        val tripInfo = tripInfo(document)
        val tripSchedule = tripSchedule(document)
        val mapUrl = mapUrl(document)
        val routeHtml = routeHtml(document)
        return VanmTrip(tripInfo, tripRates, cashPool, tripSchedule, mapUrl, routeHtml)
    }

    private fun routeHtml(document: Document): String? {
        val table = document.select("body>div>div>table").getOrNull(4)
        return table?.select("tr>td")?.getOrNull(1)?.html()
    }

    private fun mapUrl(document: Document): String? {
        val table = document.select("body>div>div>table").getOrNull(3)
        return table?.select("iframe")?.attr("src")
    }

    private fun cashPool(document: Document): List<VanmCashPool> {
        val table = document.select("body>div>div>table").getOrNull(2)
        return table?.select("tr")?.drop(1)?.filter { it.select("td").size == 3 }
                ?.map { tr ->
                    val tds = tr.select("td")
                    val desc = tds[0].text()
                    val cur = tds[1].select("b").text()
                    val price = tds[2].select("div>b").text().replace(".", "")
                    VanmCashPool(desc, Currency.valueOf(cur), price.toDouble())
                } ?: emptyList()
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
        val difficultyKey = s("vanm.parse.infos.difficulty")
        val visaKey = s("vanm.parse.infos.visa")
        val infosKey = s("vanm.parse.infos.infos")
        val map = HashMap<String,String>()
        listOf(durationKey, periodKey, overnightsKey, transportsKey, mealsKey, difficultyKey, visaKey, infosKey)
                .forEach { k ->
                    infoRows.filter { it.childNodeSize() > 1 && it.select(s("vanm.parse.infos.class")).text().contains(k)}
                            .forEach{  map.put(k, it.child(1).text()) }
                }
        val tripInfo2 = doc.select(s("vanm.parse.info_table2"))
        val classifications = tripInfo2.select(s("vanm.parse.classifications")).map { it.attr("href").split("#").last() }
        val countries = tripInfo2.select(s("vanm.parse.countries")).map { it.attr("alt") }
        //Logger.info(countries)
        val days = map[durationKey]?.let { durationRegex.find(it) }.let { it?.groups?.get(1)?.value?.toInt() }

        return VanmTripInfo(
                name.text(),
                description.text(),
                map[durationKey],
                days,
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
        val schedule = doc.select("body>div>div>table").getOrNull(0)
        return schedule?.select("tbody>tr")?.drop(2)?.dropLast(3)?.map {
            val elements = it.select("td")
            val code = elements.getOrNull(0)?.select("div")?.html()
            if (code.isNullOrBlank()) {
                null
            } else {
                val from = elements.getOrNull(1)?.select("div")?.html()?.split("-")?.let { LocalDate.of(it[2].toInt(), it[1].toInt(), it[0].toInt()) }
                val to = elements.getOrNull(2)?.select("div")?.html()?.split("-")?.let { LocalDate.of(it[2].toInt(), it[1].toInt(), it[0].toInt()) }
                val booked = elements.getOrNull(3)?.select("div")?.html()
                val info = elements.getOrNull(4)?.select("div")?.text()
                val open = elements.getOrNull(5)?.select("div>input")?.isNotEmpty() ?: false
                VanmTripSchedule(code!!.toInt(), from, to, if(booked.isNullOrBlank()) 0 else booked?.toInt(), info, open)
            }
        }?.filterNotNull() ?: emptyList()
    }

    private fun s(key:String): String = config.getString(key, "")
}