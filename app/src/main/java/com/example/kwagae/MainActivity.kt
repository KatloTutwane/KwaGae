package com.example.kwagae

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.kwagae.data.DatabaseSeeder
import com.example.kwagae.notifications.ListingAlertWorker
import com.example.kwagae.notifications.NotificationHelper
import com.example.kwagae.ui.theme.KwaGaeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — worker will still run; notify only if granted */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel (safe to call every launch)
        NotificationHelper.createChannel(this)

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Seed DB on first launch
        CoroutineScope(Dispatchers.IO).launch {
            DatabaseSeeder.seedIfNeeded(applicationContext)
        }

        // Schedule periodic listing alert checks (minimum interval is 15 min with WorkManager)
        scheduleListingAlerts()

        setContent {
            KwaGaeTheme {
                KwaGaeApp()
            }
        }
    }

    private fun scheduleListingAlerts() {
        val request = PeriodicWorkRequestBuilder<ListingAlertWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "listing_alerts",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

@Composable
fun KwaGaeApp() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = "login",
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable("login")    { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("main")     { MainScreen(navController) }

            composable("listing_detail/{listingId}") { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId")?.toLongOrNull() ?: 0L
                ListingDetailScreen(navController, listingId)
            }

            composable("chat")          { ChatScreen(navController) }
            composable("conversations") { ConversationsScreen(navController) }

            // Provider listing management
            composable("add_listing") {
                ProviderListingFormScreen(navController, listingId = 0L)
            }
            composable("edit_listing/{listingId}") { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId")?.toLongOrNull() ?: 0L
                ProviderListingFormScreen(navController, listingId = listingId)
            }

            // Deposit & reservation
            composable("reserve/{listingId}") { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId")?.toLongOrNull() ?: 0L
                ReservationScreen(navController, listingId)
            }
            composable("receipt/{listingId}/{refNumber}") { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId")?.toLongOrNull() ?: 0L
                val refNumber = backStackEntry.arguments?.getString("refNumber") ?: ""
                ReceiptScreen(navController, listingId, refNumber)
            }
        }
    }
}
