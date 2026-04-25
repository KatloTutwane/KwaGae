package com.example.kwagae

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kwagae.ui.theme.KwaGaeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KwaGaeTheme {
                KwaGaeApp()
            }
        }
    }
}

@Composable
fun KwaGaeApp() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("main") { MainScreen(navController) }
            // Stub these screens if they don't exist yet to fix build
            composable("listing_detail/{listingId}") { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId")?.toLongOrNull() ?: 0L
                ListingDetailScreen(navController, listingId)
            }
            composable("reservation/{listingId}") { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId")?.toLongOrNull() ?: 0L
                ReservationScreen(navController, listingId)
            }
        }
    }
}

@Composable
fun ListingDetailScreen(navController: androidx.navigation.NavController, listingId: Long) {
    // Placeholder
}

@Composable
fun ReservationScreen(navController: androidx.navigation.NavController, listingId: Long) {
    // Placeholder
}
