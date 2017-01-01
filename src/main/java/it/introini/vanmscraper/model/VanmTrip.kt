package it.introini.vanmscraper.model

import java.time.Instant
import java.time.LocalDate

data class DbVanmTrip(val code:Int, val strCode:String, val url:String, val status: TripStatus, val scrapedOn: Instant, val trip: VanmTrip)
enum class TripStatus {
    OK,
    MISSING,
    ERROR
}
data class VanmTrip(val infos: VanmTripInfo, val rates: List<VanmTripRate>, val schedules: List<VanmTripSchedule>)
data class VanmTripInfo(val name            : String?,
                        val description     : String?,
                        val duration        : String?,
                        val period          : String?,
                        val overnights      : String?,
                        val transports      : String?,
                        val meals           : String?,
                        val difficulty      : String?,
                        val visas           : String?,
                        val infos           : String?,
                        val classifications : Collection<String>,
                        val countries       : Collection<String>)

data class VanmTripRate(val rateType: RateType, val description: String, val currency: String, val price: Double) {
    companion object {
        fun VanmTripExtra(description: String, currency: String, price: Double) = VanmTripRate(RateType.EXTRA, description, currency, price)
        fun VanmTripCity(description: String, currency: String, price: Double) = VanmTripRate(RateType.CITY, description, currency, price)
    }
}
data class VanmTripSchedule(val code: Int, val from: LocalDate, val to: LocalDate, val booked: Int, val info: String?, val open: Boolean)

enum class RateType {
    CITY,
    EXTRA
}