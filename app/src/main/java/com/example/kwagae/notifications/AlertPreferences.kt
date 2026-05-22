package com.example.kwagae.notifications

import android.content.Context
import com.example.kwagae.data.models.Listing

data class AlertPrefs(
    val enabled: Boolean = false,
    val maxPrice: Float = 15000f,
    val types: Set<String> = emptySet(),
    val wifiRequired: Boolean = false,
    val furnishedRequired: Boolean = false
)

object AlertPreferences {

    private const val PREFS_NAME = "kwagae_alert_prefs"

    fun save(context: Context, prefs: AlertPrefs) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean("enabled", prefs.enabled)
            .putFloat("maxPrice", prefs.maxPrice)
            .putStringSet("types", prefs.types)
            .putBoolean("wifi", prefs.wifiRequired)
            .putBoolean("furnished", prefs.furnishedRequired)
            .apply()
    }

    fun load(context: Context): AlertPrefs {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AlertPrefs(
            enabled         = sp.getBoolean("enabled", false),
            maxPrice        = sp.getFloat("maxPrice", 15000f),
            types           = sp.getStringSet("types", emptySet()) ?: emptySet(),
            wifiRequired    = sp.getBoolean("wifi", false),
            furnishedRequired = sp.getBoolean("furnished", false)
        )
    }

    /** Returns true if [listing] satisfies every saved alert criterion. */
    fun matches(listing: Listing, prefs: AlertPrefs): Boolean {
        if (!prefs.enabled) return false
        if (listing.price > prefs.maxPrice) return false
        if (prefs.types.isNotEmpty() && listing.type !in prefs.types) return false
        if (prefs.wifiRequired) {
            val a = listing.amenities.lowercase()
            if (!a.contains("wi-fi") && !a.contains("wifi") && !a.contains("fibre")) return false
        }
        if (prefs.furnishedRequired && !listing.amenities.contains("furnished", ignoreCase = true)) return false
        return true
    }

    /** Tracking key so we don't re-notify about the same listings. */
    fun getNotifiedIds(context: Context): Set<Long> {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sp.getStringSet("notified_ids", emptySet())?.map { it.toLong() }?.toSet() ?: emptySet()
    }

    fun saveNotifiedIds(context: Context, ids: Set<Long>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putStringSet("notified_ids", ids.map { it.toString() }.toSet())
            .apply()
    }
}
