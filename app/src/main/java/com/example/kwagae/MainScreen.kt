package com.example.kwagae

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(navController: NavController) {
    val bottomNavController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf("listings", "filters", "profile")
                val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.Person)
                val labels = listOf("Houses", "Filters", "Profile")
                
                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = bottomNavController.currentBackStackEntry?.destination?.route == screen,
                        onClick = { 
                            bottomNavController.navigate(screen) {
                                popUpTo(bottomNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(icons[index], contentDescription = labels[index]) },
                        label = { Text(labels[index]) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "listings",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("listings") { ListingsScreen(navController) }
            composable("filters") { FiltersScreen() }
            composable("profile") { ProfileScreen(navController) }
        }
    }
}

@Composable
fun FiltersScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Filters Screen - Coming Soon")
    }
}

@Composable
fun ProfileScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Profile Screen - Coming Soon")
    }
}
