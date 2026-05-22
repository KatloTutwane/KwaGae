package com.example.kwagae.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.kwagae.data.database.AppDatabase

class ListingAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = AlertPreferences.load(applicationContext)
        if (!prefs.enabled) return Result.success()

        val db = AppDatabase.getDatabase(applicationContext)
        val listings = db.listingDao().getAllListings()

        val alreadyNotified = AlertPreferences.getNotifiedIds(applicationContext)
        val newMatches = listings.filter { listing ->
            listing.listingId !in alreadyNotified && AlertPreferences.matches(listing, prefs)
        }

        if (newMatches.isNotEmpty()) {
            val locationSummary = newMatches
                .map { it.location.substringBefore(",").trim() }
                .toSet()
                .take(2)
                .joinToString(" & ")

            val body = when {
                newMatches.size == 1 ->
                    "New accommodation available in ${newMatches.first().location} within your budget!"
                else ->
                    "${newMatches.size} new listings in $locationSummary match your preferences!"
            }

            NotificationHelper.sendAlert(
                context = applicationContext,
                title   = "New homes match your preferences!",
                body    = body
            )

            AlertPreferences.saveNotifiedIds(
                applicationContext,
                alreadyNotified + newMatches.map { it.listingId }.toSet()
            )
        }

        return Result.success()
    }
}
