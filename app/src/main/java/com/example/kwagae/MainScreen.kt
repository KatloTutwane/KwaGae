package com.example.kwagae

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.Listing
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(navController: NavController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            // Comic-style navigation bar
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.graphicsLayer(
                    shadowElevation = 8f,
                    spotShadowColor = MaterialTheme.colorScheme.primary
                )
            ) {
                val items = listOf("listings", "filters", "profile")
                val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.Person)
                val labels = listOf("🏠 Houses", "🔍 Filters", "👤 Profile")

                items.forEachIndexed { index, screen ->
                    val isSelected = bottomNavController.currentBackStackEntry?.destination?.route == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            bottomNavController.navigate(screen) {
                                popUpTo(bottomNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                icons[index],
                                contentDescription = labels[index],
                                modifier = Modifier.graphicsLayer(
                                    scaleX = if (isSelected) 1.1f else 1f,
                                    scaleY = if (isSelected) 1.1f else 1f
                                )
                            )
                        },
                        label = {
                            Text(
                                labels[index],
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        )
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
            composable("listings") { ComicListingsScreen(navController) }
            composable("filters") { ComicFiltersScreen(bottomNavController) }
            composable("profile") { ComicProfileScreen(navController) }
        }
    }
}

// 🎨 COMIC-STYLE LISTINGS SCREEN
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ComicListingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userFullName by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("student") }
    var recentListings by remember { mutableStateOf<List<Listing>>(emptyList()) }
    var recommendedListings by remember { mutableStateOf<List<Listing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("All") }

    // Comic-style animation
    val infiniteTransition = rememberInfiniteTransition(label = "comic_shake")
    val comicShake by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    val categories = listOf(
        "All" to "🏠",
        "Apartments" to "🏢",
        "Houses" to "🏡",
        "Rooms" to "🛏️",
        "Studios" to "🎨"
    )

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val users = db.userDao().getAllUsers()
                if (users.isNotEmpty()) {
                    userFullName = users.first().fullName.split(" ").first()
                    userRole = users.first().role
                }

                val allListings = db.listingDao().getAllListings()
                recentListings = allListings.takeLast(6).reversed()
                recommendedListings = allListings.shuffled().take(4)
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        // Manga screentone dots
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dotSpacing = 30f
            val dotRadius = 1.5f
            for (x in 0..(size.width / dotSpacing).toInt()) {
                for (y in 0..(size.height / dotSpacing).toInt()) {
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.05f),
                        radius = dotRadius,
                        center = Offset(x * dotSpacing, y * dotSpacing)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Comic Header
            item {
                ComicHeader(
                    userName = userFullName.ifEmpty { "Explorer" },
                    userRole = userRole,
                    comicShake = comicShake,
                    onNotificationClick = { /* TODO */ },
                    onProfileClick = { /* TODO */ }
                )
            }

            // Stats Panel
            item {
                ComicStatsPanel(
                    listingsCount = recentListings.size,
                    favoritesCount = 12,
                    visitsCount = 8,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Categories
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SectionHeader(
                        title = "📚 Categories",
                        emoji = "🎯",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    LazyRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(categories) { (category, emoji) ->
                            ComicCategoryChip(
                                category = category,
                                emoji = emoji,
                                isSelected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                }
            }

            // Recent Listings
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SectionHeader(
                        title = "🔥 New Adventures",
                        emoji = "⚡",
                        actionText = "See all",
                        onActionClick = { /* TODO */ },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    LazyRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(recentListings) { listing ->
                            ComicListingCard(
                                listing = listing,
                                onClick = {
                                    navController.navigate("listing_detail/${listing.listingId}")
                                }
                            )
                        }
                    }
                    
                    if (recentListings.isEmpty() && !isLoading) {
                        EmptyComicCard(text = "No listings yet", modifier = Modifier.padding(16.dp))
                    }
                }
            }

            // Recommended
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SectionHeader(
                        title = "✨ Magical Picks",
                        emoji = "🎭",
                        actionText = "More",
                        onActionClick = { /* TODO */ },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    if (recommendedListings.isNotEmpty()) {
                        recommendedListings.chunked(2).forEach { rowListings ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowListings.forEach { listing ->
                                    ComicVerticalCard(
                                        listing = listing,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            navController.navigate("listing_detail/${listing.listingId}")
                                        }
                                    )
                                }
                                if (rowListings.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else if (!isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No recommendations yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

// 🎨 COMIC FILTERS SCREEN
@Composable
fun ComicFiltersScreen(bottomNavController: NavController) {
    var selectedBedrooms by remember { mutableStateOf(1) }
    var selectedPropertyType by remember { mutableStateOf("All") }

    val propertyTypes = listOf("All", "Apartment", "House", "Room", "Studio")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "🔍 Filter Your Quest",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💰 Price Range", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Simplified slider - in real app, use RangeSlider
                    Text("₱0 - ₱1000+", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🛏️ Bedrooms", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(1, 2, 3, 4).forEach { bedrooms ->
                            FilterChip(
                                selected = selectedBedrooms == bedrooms,
                                onClick = { selectedBedrooms = bedrooms },
                                label = { Text("$bedrooms") }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🏠 Property Type", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(propertyTypes) { type ->
                            FilterChip(
                                selected = selectedPropertyType == type,
                                onClick = { selectedPropertyType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { bottomNavController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🔮 Apply Magic Filter")
            }
        }
    }
}

// 🎨 COMIC PROFILE SCREEN
@Composable
fun ComicProfileScreen(navController: NavController) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<com.example.kwagae.data.models.User?>(null) }

    LaunchedEffect(Unit) {
        val db = AppDatabase.getDatabase(context)
        val users = db.userDao().getAllUsers()
        user = users.firstOrNull()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Comic avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (user?.role == "student") "🎓" else "🏠",
                    fontSize = 56.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user?.fullName?.split(" ")?.first() ?: "Explorer",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (user?.role == "student") "✨ Home Seeker ✨" else "🌟 Property Host 🌟",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = user?.email ?: "email@example.com",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Stats cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatCard("🎯", "Member Since", "2024", Modifier.weight(1f))
                ProfileStatCard("📊", "Listings", "0", Modifier.weight(1f))
                ProfileStatCard("❤️", "Saved", "0", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Menu items
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    ProfileMenuItem("⚙️", "Settings", "Adjust your preferences")
                    HorizontalDivider()
                    ProfileMenuItem("🎨", "Appearance", "Theme and display")
                    HorizontalDivider()
                    ProfileMenuItem("🔒", "Privacy", "Security settings")
                    HorizontalDivider()
                    ProfileMenuItem("❓", "Help Center", "FAQs and support")
                    HorizontalDivider()
                    ProfileMenuItem("📝", "Terms & Conditions", "Legal stuff")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Logout button
        item {
            Button(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("🚪 Logout")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Helper Components
@Composable
fun ProfileStatCard(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileMenuItem(emoji: String, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

@Composable
fun ComicHeader(
    userName: String,
    userRole: String,
    comicShake: Float,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .graphicsLayer(translationY = comicShake),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Welcome back,",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Row {
            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
            }
            IconButton(onClick = onProfileClick) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
            }
        }
    }
}

@Composable
fun ComicStatsPanel(
    listingsCount: Int,
    favoritesCount: Int,
    visitsCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("🏠", listingsCount.toString(), "Listings")
            StatItem("❤️", favoritesCount.toString(), "Favorites")
            StatItem("👀", visitsCount.toString(), "Visits")
        }
    }
}

@Composable
fun StatItem(emoji: String, count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 24.sp)
        Text(count, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun SectionHeader(
    title: String,
    emoji: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionText)
            }
        }
    }
}

@Composable
fun ComicCategoryChip(
    category: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun ComicListingCard(
    listing: Listing,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(64.dp))
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = listing.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1
                )
                Text(
                    text = "₱${listing.price}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "📍 ${listing.location}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ComicVerticalCard(
    listing: Listing,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(48.dp))
            }
            
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = listing.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "₱${listing.price}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyComicCard(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
