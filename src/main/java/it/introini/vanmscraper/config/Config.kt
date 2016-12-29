package it.introini.vanmscraper.config

import com.google.inject.Inject
import io.vertx.core.json.JsonObject


class Config @Inject constructor(val jsonConfig: JsonObject) {

    fun getString(param: String, def: String) = jsonConfig.getString(param, def)
    fun getInt(param: String, def: Int)       = jsonConfig.getInteger(param, def)
    fun getDouble(param: String, def: Double) = jsonConfig.getDouble(param, def)
    fun getLong(param: String, def: Long)     = jsonConfig.getLong(param, def)
}