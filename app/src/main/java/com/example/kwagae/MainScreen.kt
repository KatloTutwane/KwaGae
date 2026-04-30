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
import com.example.kwagae.ui.theme.GroundedColors
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.vector.ImageVector

// Data class for categories
data class Category(val name: String, val icon: ImageVector)

// ── Brush helpers (matching login screen) ────────────────────────────────────
private val backgroundGradient = Brush.verticalGradient(
    colors = listOf(
        GroundedColors.BgTop,
        GroundedColors.BgMid1,
        GroundedColors.BgMid2,
        GroundedColors.BgBottom
    )
)

private val topStripeGradient = Brush.horizontalGradient(
    colors = listOf(
        GroundedColors.AccentMoss,
        GroundedColors.AccentClay,
        GroundedColors.AccentBark
    )
)

private val cardGradient = Brush.verticalGradient(
    colors = listOf(
        GroundedColors.CreamCard,
        GroundedColors.CreamCard.copy(alpha = 0.95f)
    )
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(navController: NavController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = GroundedColors.CreamCard,
                tonalElevation = 0.dp,
                modifier = Modifier.shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    ambientColor = Color(0xFF1E1208).copy(alpha = 0.15f),
                    spotColor = Color(0xFF1E1208).copy(alpha = 0.1f)
                )
            ) {
                val items = listOf("listings", "filters", "profile")
                val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.Person)
                val labels = listOf("HOMES", "FILTERS", "PROFILE")

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
                                modifier = Modifier.size(24.dp),
                                tint = if (isSelected) GroundedColors.ClayWarm else GroundedColors.TextMuted
                            )
                        },
                        label = {
                            Text(
                                labels[index],
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                letterSpacing = 1.sp,
                                color = if (isSelected) GroundedColors.ClayWarm else GroundedColors.TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GroundedColors.ClayWarm,
                            selectedTextColor = GroundedColors.ClayWarm,
                            unselectedIconColor = GroundedColors.TextMuted,
                            unselectedTextColor = GroundedColors.TextMuted,
                            indicatorColor = GroundedColors.AccentClay.copy(alpha = 0.1f)
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
            composable("listings") { GroundedListingsScreen(navController) }
            composable("filters") { GroundedFiltersScreen(bottomNavController) }
            composable("profile") { GroundedProfileScreen(navController) }
        }
    }
}

// 🌱 GROUNDED LISTINGS SCREEN
@Composable
fun GroundedListingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userFullName by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("student") }
    var recentListings by remember { mutableStateOf<List<Listing>>(emptyList()) }
    var recommendedListings by remember { mutableStateOf<List<Listing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        Category("All", Icons.Default.Home),
        Category("Apartments", Icons.Default.Business),
        Category("Houses", Icons.Default.House),
        Category("Rooms", Icons.Default.Bed),
        Category("Studios", Icons.Default.Apartment)
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
            .background(backgroundGradient)
    ) {
        LeafShape(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-80).dp, y = (-40).dp)
        )
        LeafShape(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 240.dp, y = 600.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(topStripeGradient)
                )
            }

            item {
                GroundedHeader(
                    userName = userFullName.ifEmpty { "Explorer" },
                    userRole = userRole,
                    onNotificationClick = { /* TODO */ },
                    onProfileClick = { /* TODO */ }
                )
            }

            item {
                GroundedStatsPanel(
                    listingsCount = recentListings.size,
                    favoritesCount = 12,
                    visitsCount = 8,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    GroundedSectionHeader(
                        title = "EXPLORE CATEGORIES",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    LazyRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        items(categories) { category ->
                            GroundedCategoryChip(
                                category = category.name,
                                icon = category.icon,
                                isSelected = selectedCategory == category.name,
                                onClick = { selectedCategory = category.name }
                            )
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    GroundedSectionHeader(
                        title = "RECENT LISTINGS",
                        actionText = "See all",
                        onActionClick = { /* TODO */ },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    LazyRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        items(recentListings) { listing ->
                            GroundedListingCardHorizontal(
                                listing = listing,
                                onClick = {
                                    navController.navigate("listing_detail/${listing.listingId}")
                                }
                            )
                        }
                    }

                    if (recentListings.isEmpty() && !isLoading) {
                        GroundedEmptyCard(text = "No listings available", modifier = Modifier.padding(16.dp))
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    GroundedSectionHeader(
                        title = "RECOMMENDED FOR YOU",
                        actionText = "More",
                        onActionClick = { /* TODO */ },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    if (recommendedListings.isNotEmpty()) {
                        recommendedListings.chunked(2).forEach { rowListings ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowListings.forEach { listing ->
                                    GroundedListingCardVertical(
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
                            Text(
                                "No recommendations yet",
                                fontSize = 13.sp,
                                color = GroundedColors.TextMuted
                            )
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = GroundedColors.ClayWarm,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Finding your grounded home...",
                                fontSize = 13.sp,
                                color = GroundedColors.TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// 🌱 GROUNDED FILTERS SCREEN
@Composable
fun GroundedFiltersScreen(bottomNavController: NavController) {
    var selectedBedrooms by remember { mutableStateOf(1) }
    var selectedPropertyType by remember { mutableStateOf("All") }
    var priceRange by remember { mutableStateOf(5000f) }

    val propertyTypes = listOf("All", "Apartment", "House", "Room", "Studio")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        LeafShape(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 220.dp, y = (-40).dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(topStripeGradient)
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                Text(
                    text = "FILTER YOUR SEARCH",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GroundedColors.ClayWarm,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Find Your Grounded Home",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GroundedColors.TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                GroundedFilterCard(
                    title = "BUDGET RANGE",
                    icon = Icons.Default.AttachMoney
                ) {
                    Column {
                        Text(
                            text = "₱0 - ₱${priceRange.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GroundedColors.ClayWarm
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(GroundedColors.BorderDefault)
                                .clip(RoundedCornerShape(2.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(priceRange / 10000f)
                                    .fillMaxHeight()
                                    .background(GroundedColors.ClayWarm)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }

            item {
                GroundedFilterCard(
                    title = "BEDROOMS",
                    icon = Icons.Default.Bed
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(1, 2, 3, 4).forEach { bedrooms ->
                            GroundedFilterChip(
                                label = "$bedrooms",
                                isSelected = selectedBedrooms == bedrooms,
                                onClick = { selectedBedrooms = bedrooms }
                            )
                        }
                    }
                }
            }

            item {
                GroundedFilterCard(
                    title = "PROPERTY TYPE",
                    icon = Icons.Default.House
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(propertyTypes) { type ->
                            GroundedFilterChip(
                                label = type,
                                isSelected = selectedPropertyType == type,
                                onClick = { selectedPropertyType = type }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { bottomNavController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GroundedColors.ClayWarm
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFFF5E8CC)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "APPLY FILTERS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        color = Color(0xFFF5E8CC)
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// 🌱 GROUNDED PROFILE SCREEN
@Composable
fun GroundedProfileScreen(navController: NavController) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<com.example.kwagae.data.models.User?>(null) }

    LaunchedEffect(Unit) {
        val db = AppDatabase.getDatabase(context)
        val users = db.userDao().getAllUsers()
        user = users.firstOrNull()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        LeafShape(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-60).dp, y = 500.dp)
        )
        LeafShape(
            modifier = Modifier
                .size(150.dp)
                .offset(x = 250.dp, y = 100.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(topStripeGradient)
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    GroundedColors.ClayWarm,
                                    GroundedColors.BarkMid
                                )
                            )
                        )
                        .border(3.dp, GroundedColors.AccentMoss, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color(0xFFF5E8CC)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = user?.fullName?.split(" ")?.first() ?: "Explorer",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GroundedColors.TextPrimary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GroundedColors.AccentMoss.copy(alpha = 0.15f),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = GroundedColors.AccentMoss
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (user?.role == "student") "STUDENT HOMESEEKER" else "PROPERTY HOST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            color = GroundedColors.AccentMoss
                        )
                    }
                }

                Text(
                    text = user?.email ?: "email@example.com",
                    fontSize = 13.sp,
                    color = GroundedColors.TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(32.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GroundedProfileStatCard(
                        icon = Icons.Default.CalendarToday,
                        label = "Member Since",
                        value = "2024",
                        modifier = Modifier.weight(1f)
                    )
                    GroundedProfileStatCard(
                        icon = Icons.Default.Home,
                        label = "Listings",
                        value = "0",
                        modifier = Modifier.weight(1f)
                    )
                    GroundedProfileStatCard(
                        icon = Icons.Default.Favorite,
                        label = "Saved",
                        value = "0",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GroundedColors.BorderDefault, RoundedCornerShape(16.dp))
                    ) {
                        GroundedProfileMenuItem(Icons.Default.Settings, "Settings", "Adjust your preferences")
                        HorizontalDivider(color = GroundedColors.BorderDefault)
                        GroundedProfileMenuItem(Icons.Default.Palette, "Appearance", "Theme and display")
                        HorizontalDivider(color = GroundedColors.BorderDefault)
                        GroundedProfileMenuItem(Icons.Default.Lock, "Privacy", "Security settings")
                        HorizontalDivider(color = GroundedColors.BorderDefault)
                        GroundedProfileMenuItem(Icons.Default.Help, "Help Center", "FAQs and support")
                        HorizontalDivider(color = GroundedColors.BorderDefault)
                        GroundedProfileMenuItem(Icons.Default.Description, "Terms & Conditions", "Legal stuff")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            item {
                Button(
                    onClick = {
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GroundedColors.BarkMid
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = Color(0xFFF5E8CC),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LOGOUT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        color = Color(0xFFF5E8CC)
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Helper Components (Grounded Theme) ───────────────────────────────────────

@Composable
private fun LeafShape(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStartPercent = 50, bottomEndPercent = 50))
            .background(GroundedColors.LeafOverlay)
    )
}

@Composable
private fun HorizontalDivider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

@Composable
fun GroundedHeader(
    userName: String,
    userRole: String,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    fontSize = 12.sp,
                    color = GroundedColors.TextMuted,
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = userName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GroundedColors.TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = GroundedColors.ClayWarm
                    )
                }
            }

            Row {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = GroundedColors.BarkMid
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = GroundedColors.BarkMid
                    )
                }
            }
        }
    }
}

@Composable
fun GroundedStatsPanel(
    listingsCount: Int,
    favoritesCount: Int,
    visitsCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GroundedStatItem(Icons.Default.Home, count = listingsCount.toString(), label = "Listings")
            GroundedStatItem(Icons.Default.Favorite, count = favoritesCount.toString(), label = "Favorites")
            GroundedStatItem(Icons.Default.Visibility, count = visitsCount.toString(), label = "Visits")
        }
    }
}

@Composable
fun GroundedStatItem(icon: ImageVector, count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = GroundedColors.BarkMid
        )
        Spacer(Modifier.height(4.dp))
        Text(
            count,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = GroundedColors.ClayWarm
        )
        Text(
            label,
            fontSize = 11.sp,
            color = GroundedColors.TextMuted,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun GroundedSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = GroundedColors.ClayWarm,
            letterSpacing = 2.sp
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    actionText,
                    fontSize = 11.sp,
                    color = GroundedColors.BarkMid,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun GroundedCategoryChip(
    category: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) GroundedColors.ClayWarm else GroundedColors.CreamField,
        border = BorderStroke(
            1.dp,
            if (isSelected) GroundedColors.ClayWarm else GroundedColors.BorderDefault
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = category,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) Color(0xFFF5E8CC) else GroundedColors.BarkMid
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.uppercase(),
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = if (isSelected) Color(0xFFF5E8CC) else GroundedColors.TextSecondary,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
fun GroundedListingCardHorizontal(
    listing: Listing,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable(onClick = onClick)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF1E1208).copy(alpha = 0.15f),
                spotColor = Color(0xFF1E1208).copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(topStripeGradient)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(GroundedColors.CreamField),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = GroundedColors.TextMuted
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = listing.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    color = GroundedColors.TextPrimary
                )
                Text(
                    text = "₱${listing.price}/month",
                    color = GroundedColors.ClayWarm,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = listing.location,
                    fontSize = 11.sp,
                    color = GroundedColors.TextMuted
                )
            }
        }
    }
}

@Composable
fun GroundedListingCardVertical(
    listing: Listing,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF1E1208).copy(alpha = 0.1f),
                spotColor = Color(0xFF1E1208).copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(topStripeGradient)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(GroundedColors.CreamField),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Apartment,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = GroundedColors.TextMuted
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = listing.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    fontSize = 13.sp,
                    color = GroundedColors.TextPrimary
                )
                Text(
                    text = "₱${listing.price}",
                    color = GroundedColors.ClayWarm,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun GroundedEmptyCard(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        border = BorderStroke(1.dp, GroundedColors.BorderDefault)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = GroundedColors.TextMuted
        )
    }
}

@Composable
fun GroundedFilterCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = GroundedColors.ClayWarm
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = GroundedColors.ClayWarm,
                    letterSpacing = 1.5.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun GroundedFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                label,
                fontSize = 12.sp,
                color = if (isSelected) Color(0xFFF5E8CC) else GroundedColors.TextSecondary
            )
        },
        modifier = Modifier.height(36.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GroundedColors.ClayWarm,
            selectedLabelColor = Color(0xFFF5E8CC),
            containerColor = GroundedColors.CreamField,
            labelColor = GroundedColors.TextSecondary
        )
    )
}

@Composable
fun GroundedProfileStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = GroundedColors.BarkMid
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GroundedColors.ClayWarm
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = GroundedColors.TextMuted,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun GroundedProfileMenuItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = GroundedColors.BarkMid
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = GroundedColors.TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = GroundedColors.TextMuted
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = GroundedColors.TextMuted
        )
    }
}