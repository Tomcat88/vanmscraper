package it.introini.vanmscraper.model

import java.time.Instant

data class DbVanmTrip(val code:Int, val strCode:String, val url:String, val scrapedOn: Instant, val trip: VanmTrip)
data class VanmTrip(val infos: VanmTripInfo)
data class VanmTripInfo(val name        : String?,
                        val description : String?,
                        val duration    : String?,
                        val period      : String?,
                        val overnights  : String?,
                        val transports  : String?,
                        val meals       : String?,
                        val difficulty  : String?,
                        val visas       : String?,
                        val infos       : String?)