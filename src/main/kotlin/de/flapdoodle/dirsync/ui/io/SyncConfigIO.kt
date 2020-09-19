package de.flapdoodle.dirsync.ui.io

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.flapdoodle.dirsync.ui.config.SyncConfig
import de.flapdoodle.photosync.ui.io.UUIDAdapter

object SyncConfigIO {
    private val moshi = Moshi.Builder()
//        .add(JsonTabModel.Adapter)
            .add(UUIDAdapter)
            .add(KotlinJsonAdapterFactory())
            .build()

    private val modelAdapter = moshi.adapter(SyncConfig::class.java)
            .indent("  ")

    fun asJson(config: SyncConfig): String {
        return modelAdapter.toJson(config)
    }

    fun fromJson(json: String): SyncConfig {
        val config = modelAdapter.fromJson(json)
        require(config!=null) {"could not parse $json"}
        return config
    }

}