package it.introini.vanmscraper.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.time.Instant
import java.time.LocalDate

data class DbVanmTrip(@JsonProperty("code")       val code:Int,
                      @JsonProperty("str_code")   val strCode:String,
                      @JsonProperty("url")        val url:String,
                      @JsonProperty("status")     val status: TripStatus,
                      @JsonProperty("scraped_on") val scrapedOn: Instant,
                      @JsonProperty("trip")       val trip: VanmTrip)
enum class TripStatus {
    OK,
    MISSING,
    ERROR
}
data class VanmTrip(@JsonProperty("info")       val infos: VanmTripInfo,
                    @JsonProperty("rates")      val rates: List<VanmTripRate>,
                    @JsonProperty("cash_pools") val cashPools: List<VanmCashPool>,
                    @JsonProperty("schedules")  val schedules: List<VanmTripSchedule>,
                    @JsonProperty("map_url")    val mapUrl: String?,
                    @JsonProperty("route_html") val routeHtml: String?)

data class VanmTripInfo(@JsonProperty val name            : String?,
                        @JsonProperty val description     : String?,
                        @JsonProperty val duration        : String?,
                        @JsonProperty val days            : Int?,
                        @JsonProperty val period          : String?,
                        @JsonProperty val overnights      : String?,
                        @JsonProperty val transports      : String?,
                        @JsonProperty val meals           : String?,
                        @JsonProperty val difficulty      : String?,
                        @JsonProperty val visas           : String?,
                        @JsonProperty val infos           : String?,
                        @JsonProperty val classifications : Collection<String>,
                        @JsonProperty val countries       : Collection<String>)

data class VanmTripRate(@JsonProperty("rate_type")   val rateType: RateType,
                        @JsonProperty("description") val description: String,
                        @JsonProperty("currency")    val currency: String,
                        @JsonProperty("price")       val price: Double) {
    companion object {
        fun VanmTripExtra(description: String, currency: String, price: Double) = VanmTripRate(RateType.EXTRA, description, currency, price)
        fun VanmTripCity(description: String, currency: String, price: Double) = VanmTripRate(RateType.CITY, description, currency, price)
    }
}
data class VanmTripSchedule(@JsonProperty val code: Int,
                            @JsonProperty val from: LocalDate?,
                            @JsonProperty val to: LocalDate?,
                            @JsonProperty val booked: Int?,
                            @JsonProperty val info: String?,
                            @JsonProperty val open: Boolean)

data class VanmCashPool(@JsonProperty val description: String,
                        @JsonProperty val currency: Currency,
                        @JsonProperty val price: Double)

enum class RateType {
    CITY,
    EXTRA
}

enum class Currency {
    EUR,
    USD
}