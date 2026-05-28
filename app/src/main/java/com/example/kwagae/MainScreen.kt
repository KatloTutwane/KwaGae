@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.kwagae

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.Listing
import com.example.kwagae.notifications.AlertPreferences
import com.example.kwagae.notifications.AlertPrefs
import com.example.kwagae.ui.components.*
import com.example.kwagae.ui.theme.GroundedColors
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest

data class Category(val name: String, val icon: ImageVector)

// ── MainScreen ────────────────────────────────────────────────────────────────

@Composable
fun MainScreen(navController: NavController) {
    val bottomNavController = rememberNavController()
    val currentBackStack by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    // Determine role so providers get a tailored tab layout
    val context = LocalContext.current
    val currentRole = remember {
        context.getSharedPreferences("kwagae_prefs", android.content.Context.MODE_PRIVATE)
            .getString("role", "student") ?: "student"
    }
    val isProvider = currentRole == "provider"

    // Shared ViewModel for both student browse tabs
    val listingsViewModel: ListingsViewModel = viewModel()

    val items  = listOf("listings", "filters", "messages", "profile")
    val icons  = if (isProvider) {
        listOf(Icons.Default.Business, Icons.Default.Search, Icons.Default.Chat, Icons.Default.Person)
    } else {
        listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.Chat, Icons.Default.Person)
    }
    val labels = if (isProvider) {
        listOf("MY LISTINGS", "SEARCH", "CHATS", "PROFILE")
    } else {
        listOf("HOMES", "SEARCH", "CHATS", "PROFILE")
    }

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
                items.forEachIndexed { index, screen ->
                    val isSelected = currentRoute == screen
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
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                letterSpacing = 0.8.sp,
                                color = if (isSelected) GroundedColors.ClayWarm else GroundedColors.TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = GroundedColors.ClayWarm,
                            selectedTextColor   = GroundedColors.ClayWarm,
                            unselectedIconColor = GroundedColors.TextMuted,
                            unselectedTextColor = GroundedColors.TextMuted,
                            indicatorColor      = GroundedColors.AccentClay.copy(alpha = 0.1f)
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
            // HOMES tab: provider dashboard for providers, listings browse for students
            composable("listings") {
                if (isProvider) ProviderDashboardScreen(navController)
                else            GroundedListingsScreen(navController, listingsViewModel)
            }
            composable("filters")  { ListingsScreen(navController, listingsViewModel) }
            composable("messages") { ConversationsScreen(navController) }
            composable("profile")  { GroundedProfileScreen(navController) }
        }
    }
}

// ── GroundedListingsScreen (Home tab) ─────────────────────────────────────────

@Composable
fun GroundedListingsScreen(navController: NavController, viewModel: ListingsViewModel) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var userFullName by remember { mutableStateOf("") }
    var userRole     by remember { mutableStateOf("student") }
    var isLoading    by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("All") }

    val allFilteredListings by viewModel.filteredListings.collectAsState()

    val categories = listOf(
        Category("All",             Icons.Default.Home),
        Category("Houses",          Icons.Default.House),
        Category("Apartments",      Icons.Default.Business),
        Category("Rooms",           Icons.Default.Bed),
        Category("Studios",         Icons.Default.Apartment),
        Category("Townhouses",      Icons.Default.Villa),
        Category("Bachelor Flats",  Icons.Default.MeetingRoom),
        Category("Duplexes",        Icons.Default.Layers),
        Category("Bedsitters",      Icons.Default.SingleBed)
    )

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val db     = AppDatabase.getDatabase(context)
                val prefs  = context.getSharedPreferences("kwagae_prefs", android.content.Context.MODE_PRIVATE)
                val userId = prefs.getLong("user_id", -1L)
                if (userId != -1L) {
                    val currentUser = db.userDao().getById(userId)
                    if (currentUser != null) {
                        userFullName = currentUser.fullName.split(" ").first()
                        userRole     = currentUser.role
                    }
                }
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    // Recent listings — pick one from each type first so the row always shows variety
    // (seeded data all has the same syncedAt, so a plain take(6) would show only Houses)
    val recentListings = remember(allFilteredListings) {
        val onePerType = allFilteredListings
            .groupBy { it.type }
            .values
            .mapNotNull { it.firstOrNull() }
        val rest = allFilteredListings.filterNot { l ->
            onePerType.any { it.listingId == l.listingId }
        }
        (onePerType + rest).take(6)
    }

    // Recommended — cheapest representative from each type, sorted by price
    val recommendedListings = remember(allFilteredListings) {
        allFilteredListings
            .filter { !it.isReserved }
            .groupBy { it.type }
            .values
            .mapNotNull { group -> group.minByOrNull { it.price } }
            .sortedBy { it.price }
            .take(4)
    }

    // Real stats derived from live data
    val availableCount  = allFilteredListings.count { it.isAvailable && !it.isReserved }
    val areasCount      = allFilteredListings
        .map { it.location.substringBefore(",").trim() }
        .filter { it.isNotEmpty() }
        .toSet().size
    val providersCount  = allFilteredListings
        .map { it.ownerUid.ifEmpty { it.providerName } }
        .filter { it.isNotEmpty() }
        .toSet().size

    // Map category chip label → the exact type string used in the database
    val categoryTypeMap = mapOf(
        "Apartments"    to "Apartment",
        "Houses"        to "House",
        "Rooms"         to "Room",
        "Studios"       to "Studio",
        "Townhouses"    to "Townhouse",
        "Bachelor Flats" to "Bachelor Flat",
        "Duplexes"      to "Duplex",
        "Bedsitters"    to "Bedsitter"
    )

    val displayedRecent = remember(selectedCategory, recentListings, allFilteredListings) {
        if (selectedCategory == "All") {
            recentListings
        } else {
            val targetType = categoryTypeMap[selectedCategory] ?: selectedCategory
            // For Studios, search the full list since they are hidden from allFilteredListings
            val sourceList = if (selectedCategory == "Studios") allFilteredListings else recentListings
            sourceList.filter {
                it.type.equals(targetType, ignoreCase = true)
            }.ifEmpty {
                // Fallback: search full list by type if recentListings has none of this type
                allFilteredListings.filter { it.type.equals(targetType, ignoreCase = true) }.take(6)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GroundedColors.backgroundGradient)
    ) {
        LeafShape(Modifier.size(250.dp).offset(x = (-80).dp, y = (-40).dp))
        LeafShape(Modifier.size(180.dp).offset(x = 240.dp,   y = 600.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── Top stripe ────────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(GroundedColors.topStripeGradient)
                )
            }

            // ── Header card ───────────────────────────────────────────────────
            item {
                GroundedHeader(
                    userName      = userFullName.ifEmpty { "Explorer" },
                    userRole      = userRole,
                    onNotificationClick = { },
                    onProfileClick      = { }
                )
            }

            // ── Stats panel ───────────────────────────────────────────────────
            item {
                GroundedStatsPanel(
                    availableCount  = availableCount,
                    areasCount      = areasCount,
                    providersCount  = providersCount,
                    modifier        = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // ── Category chips ────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    GroundedSectionHeader(
                        title    = "EXPLORE CATEGORIES",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    LazyRow(
                        modifier              = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding        = PaddingValues(horizontal = 20.dp)
                    ) {
                        items(categories) { category ->
                            GroundedCategoryChip(
                                category   = category.name,
                                icon       = category.icon,
                                isSelected = selectedCategory == category.name,
                                onClick    = { selectedCategory = category.name }
                            )
                        }
                    }
                }
            }

            // ── Recent listings (horizontal scroll row) ───────────────────────
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    GroundedSectionHeader(
                        title        = "RECENT LISTINGS",
                        actionText   = "See all",
                        onActionClick = { },
                        modifier     = Modifier.padding(horizontal = 20.dp)
                    )
                    LazyRow(
                        modifier              = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding        = PaddingValues(horizontal = 20.dp)
                    ) {
                        items(
                            items = displayedRecent,
                            key   = { it.listingId }
                        ) { listing ->
                            GroundedListingCardHorizontal(
                                listing = listing,
                                onClick = {
                                    navController.navigate("listing_detail/${listing.listingId}")
                                }
                            )
                        }
                    }
                    if (displayedRecent.isEmpty() && !isLoading) {
                        GroundedEmptyCard(
                            text     = "No listings available",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // ── Recommended (2-column grid) ───────────────────────────────────
            item {
                GroundedSectionHeader(
                    title        = "RECOMMENDED FOR YOU",
                    actionText   = "More",
                    onActionClick = { },
                    modifier     = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            if (recommendedListings.isNotEmpty()) {
                // Emit each pair as its own LazyColumn item (avoids nested lazy)
                items(
                    items = recommendedListings.chunked(2),
                    key   = { pair -> pair.first().listingId }
                ) { pair ->
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        pair.forEach { listing ->
                            GroundedListingCardVertical(
                                listing  = listing,
                                modifier = Modifier.weight(1f),
                                onClick  = {
                                    navController.navigate("listing_detail/${listing.listingId}")
                                }
                            )
                        }
                        // Fill empty slot when odd number of listings
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            } else if (!isLoading) {
                item {
                    Box(
                        modifier          = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment  = Alignment.Center
                    ) {
                        Text(
                            "No recommendations yet",
                            fontSize = 13.sp,
                            color    = GroundedColors.TextMuted
                        )
                    }
                }
            }

            // ── Loading indicator ─────────────────────────────────────────────
            if (isLoading) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color       = GroundedColors.ClayWarm,
                                strokeWidth = 3.dp,
                                modifier    = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text     = "Finding your grounded home...",
                                fontSize = 13.sp,
                                color    = GroundedColors.TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Alert Preferences Screen (FILTERS tab) ────────────────────────────────────
// Users configure notification preferences here; active filters are in ListingsScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroundedFiltersScreen(bottomNavController: NavController) {
    val context = LocalContext.current
    var prefs   by remember { mutableStateOf(AlertPreferences.load(context)) }
    var saved   by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GroundedColors.backgroundGradient)
    ) {
        LeafShape(Modifier.size(200.dp).offset(x = 220.dp, y = (-40).dp))

        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Box(
                    Modifier.fillMaxWidth().height(3.dp)
                        .background(GroundedColors.topStripeGradient)
                )
                Spacer(Modifier.height(16.dp))
                Text("SMART ALERTS", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    color = GroundedColors.ClayWarm, letterSpacing = 3.sp)
                Text("Notification Preferences", fontSize = 24.sp, fontWeight = FontWeight.SemiBold,
                    color = GroundedColors.TextPrimary, modifier = Modifier.padding(bottom = 4.dp))
                Text("Get notified when a new listing matches your criteria.",
                    fontSize = 13.sp, color = GroundedColors.TextMuted,
                    modifier = Modifier.padding(bottom = 16.dp))
            }

            // Enable toggle
            item {
                GroundedFilterCard(title = "ALERTS", icon = Icons.Default.Notifications) {
                    Row(
                        Modifier.fillMaxWidth(),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable listing alerts", fontSize = 14.sp,
                                fontWeight = FontWeight.Medium, color = GroundedColors.TextPrimary)
                            Text("Receive push notifications for matching homes",
                                fontSize = 11.sp, color = GroundedColors.TextMuted)
                        }
                        Switch(
                            checked  = prefs.enabled,
                            onCheckedChange = { prefs = prefs.copy(enabled = it); saved = false },
                            colors   = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GroundedColors.ClayWarm
                            )
                        )
                    }
                }
            }

            // Max budget
            item {
                GroundedFilterCard(title = "MAX BUDGET", icon = Icons.Default.AttachMoney) {
                    Text("Up to BWP ${prefs.maxPrice.toInt()}/mo", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = GroundedColors.ClayWarm)
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value         = prefs.maxPrice,
                        onValueChange = { prefs = prefs.copy(maxPrice = it); saved = false },
                        valueRange    = 500f..15000f,
                        enabled       = prefs.enabled,
                        colors        = SliderDefaults.colors(
                            thumbColor       = GroundedColors.ClayWarm,
                            activeTrackColor = GroundedColors.ClayWarm
                        )
                    )
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("BWP 500",    fontSize = 10.sp, color = GroundedColors.TextMuted)
                        Text("BWP 15,000", fontSize = 10.sp, color = GroundedColors.TextMuted)
                    }
                }
            }

            // Property type
            item {
                GroundedFilterCard(title = "PROPERTY TYPE", icon = Icons.Default.House) {
                    Text("Leave empty to alert on any type", fontSize = 11.sp,
                        color = GroundedColors.TextMuted, modifier = Modifier.padding(bottom = 8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(8.dp)
                    ) {
                        ALL_PROPERTY_TYPES.forEach { type ->
                            val isSelected = type in prefs.types
                            FilterChip(
                                selected = isSelected,
                                onClick  = {
                                    val updated = if (isSelected) prefs.types - type else prefs.types + type
                                    prefs = prefs.copy(types = updated)
                                    saved = false
                                },
                                label    = { Text(type, fontSize = 12.sp) },
                                enabled  = prefs.enabled,
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GroundedColors.ClayWarm,
                                    selectedLabelColor     = Color(0xFFF5E8CC),
                                    containerColor         = GroundedColors.CreamField,
                                    labelColor             = GroundedColors.TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Amenities
            item {
                GroundedFilterCard(title = "REQUIRED AMENITIES", icon = Icons.Default.Star) {
                    Row(
                        Modifier.fillMaxWidth(),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Text("Must have Wi-Fi / Fibre", fontSize = 13.sp,
                            color = GroundedColors.TextPrimary)
                        Switch(
                            checked  = prefs.wifiRequired,
                            onCheckedChange = { prefs = prefs.copy(wifiRequired = it); saved = false },
                            enabled  = prefs.enabled,
                            colors   = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GroundedColors.ClayWarm
                            )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Text("Must be furnished", fontSize = 13.sp,
                            color = GroundedColors.TextPrimary)
                        Switch(
                            checked  = prefs.furnishedRequired,
                            onCheckedChange = { prefs = prefs.copy(furnishedRequired = it); saved = false },
                            enabled  = prefs.enabled,
                            colors   = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GroundedColors.ClayWarm
                            )
                        )
                    }
                }
            }

            // Save button
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        AlertPreferences.save(context, prefs)
                        saved = true
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = GroundedColors.ClayWarm)
                ) {
                    Icon(
                        if (saved) Icons.Default.Check else Icons.Default.NotificationsActive,
                        null, Modifier.size(18.dp), tint = Color(0xFFF5E8CC)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (saved) "PREFERENCES SAVED" else "SAVE PREFERENCES",
                        fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp, color = Color(0xFFF5E8CC)
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── GroundedProfileScreen ─────────────────────────────────────────────────────

@Composable
fun GroundedProfileScreen(navController: NavController) {
    val context = LocalContext.current
    var user           by remember { mutableStateOf<com.example.kwagae.data.models.User?>(null) }
    var memberSince    by remember { mutableStateOf("—") }
    var listingsCount  by remember { mutableStateOf(0) }
    var savedCount     by remember { mutableStateOf(0) }
    var isProvider     by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val db     = AppDatabase.getDatabase(context)
        val prefs  = context.getSharedPreferences("kwagae_prefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getLong("user_id", -1L)
        val role   = prefs.getString("role", "student") ?: "student"
        isProvider = role == "provider"

        // ── Member Since ─────────────────────────────────────────────────────
        // Store the first-view timestamp so it persists across relaunches
        if (prefs.getLong("member_since", 0L) == 0L) {
            prefs.edit().putLong("member_since", System.currentTimeMillis()).apply()
        }
        val cal = Calendar.getInstance().apply {
            timeInMillis = prefs.getLong("member_since", System.currentTimeMillis())
        }
        memberSince = cal.get(Calendar.YEAR).toString()

        // ── User object ───────────────────────────────────────────────────────
        if (userId != -1L) {
            user = db.userDao().getById(userId)
        }

        // ── Listings count ────────────────────────────────────────────────────
        listingsCount = if (isProvider) {
            val ownerUid = prefs.getString("firebase_uid", null)?.takeIf { it.isNotEmpty() }
                ?: prefs.getString("student_id", null)?.takeIf { it.isNotEmpty() }
                ?: userId.toString()
            db.listingDao().getByOwnerUid(ownerUid).first().size
        } else {
            db.listingDao().getReservedByStudent(userId.toString()).first().size
        }

        // ── Saved count ───────────────────────────────────────────────────────
        savedCount = prefs.getStringSet("saved_listings", emptySet())!!.size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GroundedColors.backgroundGradient)
    ) {
        LeafShape(Modifier.size(220.dp).offset(x = (-60).dp, y = 500.dp))
        LeafShape(Modifier.size(150.dp).offset(x = 250.dp,   y = 100.dp))

        LazyColumn(
            modifier               = Modifier.fillMaxSize(),
            contentPadding         = PaddingValues(16.dp),
            horizontalAlignment    = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(GroundedColors.topStripeGradient)
                )
                Spacer(Modifier.height(24.dp))
            }

            // ── Avatar + name ─────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(GroundedColors.ClayWarm, GroundedColors.BarkMid)
                            )
                        )
                        .border(3.dp, GroundedColors.AccentMoss, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color(0xFFF5E8CC))
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text       = user?.fullName?.split(" ")?.first() ?: "Explorer",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = GroundedColors.TextPrimary
                )

                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = GroundedColors.AccentMoss.copy(alpha = 0.15f),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        modifier           = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment  = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(12.dp), tint = GroundedColors.AccentMoss)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text          = if (user?.role == "student") "STUDENT HOMESEEKER" else "PROPERTY HOST",
                            fontSize      = 10.sp,
                            fontWeight    = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            color         = GroundedColors.AccentMoss
                        )
                    }
                }

                Text(
                    text     = user?.email ?: "email@example.com",
                    fontSize = 13.sp,
                    color    = GroundedColors.TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(32.dp))
            }

            // ── Stat cards row ────────────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GroundedProfileStatCard(
                        icon     = Icons.Default.CalendarToday,
                        label    = "Member Since",
                        value    = memberSince,
                        modifier = Modifier.weight(1f)
                    )
                    GroundedProfileStatCard(
                        icon     = if (isProvider) Icons.Default.Home else Icons.Default.Key,
                        label    = if (isProvider) "Listings" else "Reserved",
                        value    = listingsCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    GroundedProfileStatCard(
                        icon     = Icons.Default.Favorite,
                        label    = "Saved",
                        value    = savedCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            // ── Menu items card ───────────────────────────────────────────────
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GroundedColors.BorderDefault, RoundedCornerShape(16.dp))
                    ) {
                        // My Reservations — students only
                        if (user?.role == "student" || user == null) {
                            GroundedProfileMenuItem(
                                icon     = Icons.Default.Key,
                                title    = "My Reservations",
                                subtitle = "View your reserved properties",
                                onClick  = { navController.navigate("my_reservations") }
                            )
                            GroundedDivider()
                        }
                        GroundedProfileMenuItem(Icons.Default.Settings,    "Settings",           "Adjust your preferences")
                        GroundedDivider()
                        GroundedProfileMenuItem(Icons.Default.Palette,     "Appearance",         "Theme and display")
                        GroundedDivider()
                        GroundedProfileMenuItem(Icons.Default.Lock,        "Privacy",            "Security settings")
                        GroundedDivider()
                        GroundedProfileMenuItem(Icons.Default.Help,        "Help Center",        "FAQs and support")
                        GroundedDivider()
                        GroundedProfileMenuItem(Icons.Default.Description, "Terms & Conditions", "Legal stuff")
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // ── Logout button ─────────────────────────────────────────────────
            item {
                Button(
                    onClick  = {
                        FirebaseAuth.getInstance().signOut()
                        context.getSharedPreferences("kwagae_prefs", android.content.Context.MODE_PRIVATE)
                            .edit().clear().apply()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = GroundedColors.BarkMid)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color(0xFFF5E8CC), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text          = "LOGOUT",
                        fontSize      = 13.sp,
                        fontWeight    = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        color         = Color(0xFFF5E8CC)
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Shared helper composables ─────────────────────────────────────────────────
// LeafShape and GroundedDivider now come from ui/components/AppComponents.kt

@Composable
fun GroundedHeader(
    userName: String,
    userRole: String,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(16.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Welcome back,", fontSize = 12.sp, color = GroundedColors.TextMuted, letterSpacing = 1.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(userName, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = GroundedColors.TextPrimary)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp), tint = GroundedColors.ClayWarm)
                }
            }
            Row {
                IconButton(onClick = onNotificationClick) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = GroundedColors.BarkMid)
                }
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = GroundedColors.BarkMid)
                }
            }
        }
    }
}

@Composable
fun GroundedStatsPanel(
    availableCount: Int,
    areasCount: Int,
    providersCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            GroundedStatItem(
                icon  = Icons.Default.Home,
                count = availableCount.toString(),
                label = "Available",
                tint  = GroundedColors.AccentMoss
            )
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(GroundedColors.BorderDefault)
            )
            GroundedStatItem(
                icon  = Icons.Default.LocationOn,
                count = areasCount.toString(),
                label = "Areas",
                tint  = GroundedColors.BarkMid
            )
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(GroundedColors.BorderDefault)
            )
            GroundedStatItem(
                icon  = Icons.Default.Business,
                count = providersCount.toString(),
                label = "Landlords",
                tint  = GroundedColors.ClayWarm
            )
        }
    }
}

@Composable
fun GroundedStatItem(
    icon: ImageVector,
    count: String,
    label: String,
    tint: Color = GroundedColors.BarkMid
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(40.dp)
                .background(tint.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = tint)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text       = count,
            fontWeight = FontWeight.ExtraBold,
            fontSize   = 22.sp,
            color      = tint
        )
        Text(
            text          = label,
            fontSize      = 10.sp,
            color         = GroundedColors.TextMuted,
            letterSpacing = 0.5.sp,
            fontWeight    = FontWeight.Medium
        )
    }
}

// GroundedSectionHeader moved to ui/components/AppComponents.kt

@Composable
fun GroundedCategoryChip(
    category: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(10.dp),
        color   = if (isSelected) GroundedColors.ClayWarm else GroundedColors.CreamField,
        border  = BorderStroke(1.dp, if (isSelected) GroundedColors.ClayWarm else GroundedColors.BorderDefault)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector    = icon,
                contentDescription = category,
                modifier       = Modifier.size(16.dp),
                tint           = if (isSelected) Color(0xFFF5E8CC) else GroundedColors.BarkMid
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text          = category.uppercase(),
                fontSize      = 11.sp,
                letterSpacing = 1.sp,
                color         = if (isSelected) Color(0xFFF5E8CC) else GroundedColors.TextSecondary,
                fontWeight    = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
fun GroundedListingCardHorizontal(listing: Listing, onClick: () -> Unit) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("kwagae_prefs", android.content.Context.MODE_PRIVATE) }
    var isFavourite by remember(listing.listingId) {
        mutableStateOf(prefs.getStringSet("saved_listings", emptySet())!!.contains(listing.listingId.toString()))
    }
    val imageSource = remember(listing.listingId) {
        listing.imageUrls.split(",").firstOrNull { it.isNotBlank() }
            ?: listing.imageUrl.takeIf { it.isNotBlank() }
    }

    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable(onClick = onClick)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp),
                ambientColor = Color(0xFF1E1208).copy(alpha = 0.15f),
                spotColor    = Color(0xFF1E1208).copy(alpha = 0.1f)),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(GroundedColors.topStripeGradient))

            // ── Image area ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            ) {
                // Gradient fallback always rendered beneath the real image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(GroundedColors.BarkMid, GroundedColors.ClayWarm.copy(alpha = 0.55f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Home, null, modifier = Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.18f))
                }

                // Real property image
                if (imageSource != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageSource)
                            .crossfade(400)
                            .build(),
                        contentDescription = listing.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                }

                // Bottom scrim for readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .align(Alignment.BottomStart)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.52f))
                            )
                        )
                )

                // Property type badge — top-left
                if (listing.type.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 10.dp),
                        color = GroundedColors.ClayWarm.copy(alpha = 0.93f),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text          = listing.type.uppercase(),
                            fontSize      = 8.sp,
                            letterSpacing = 0.8.sp,
                            color         = Color(0xFFF5E8CC),
                            fontWeight    = FontWeight.Bold,
                            modifier      = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Favourite button — top-right
                IconButton(
                    onClick  = {
                        isFavourite = !isFavourite
                        val saved = prefs.getStringSet("saved_listings", emptySet())!!.toMutableSet()
                        if (isFavourite) saved.add(listing.listingId.toString()) else saved.remove(listing.listingId.toString())
                        prefs.edit().putStringSet("saved_listings", saved).apply()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.30f), CircleShape)
                ) {
                    Icon(
                        imageVector        = if (isFavourite) Icons.Default.Favorite
                                             else Icons.Default.FavoriteBorder,
                        contentDescription = "Save listing",
                        modifier           = Modifier.size(14.dp),
                        tint               = if (isFavourite) Color(0xFFFF6B6B) else Color.White
                    )
                }

                // Price chip — bottom-left overlaid on image
                Surface(
                    shape    = RoundedCornerShape(topEnd = 10.dp),
                    color    = GroundedColors.ClayWarm,
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text       = "BWP ${"%.0f".format(listing.price)}/mo",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFFF5E8CC),
                        modifier   = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Card content ──────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text       = listing.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    color      = GroundedColors.TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null,
                        Modifier.size(11.dp), tint = GroundedColors.BarkMid)
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text     = listing.location,
                        fontSize = 10.sp,
                        color    = GroundedColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(6.dp))
                // Availability badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (listing.isAvailable)
                        GroundedColors.AccentMoss.copy(alpha = 0.14f)
                    else Color(0xFFE65100).copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (listing.isAvailable) Icons.Default.CheckCircle
                                          else Icons.Default.Cancel,
                            contentDescription = null,
                            modifier           = Modifier.size(9.dp),
                            tint               = if (listing.isAvailable) GroundedColors.AccentMoss
                                                 else Color(0xFFE65100)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text       = if (listing.isAvailable) "Available" else "Taken",
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = if (listing.isAvailable) GroundedColors.AccentMoss
                                         else Color(0xFFE65100)
                        )
                    }
                }
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
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("kwagae_prefs", android.content.Context.MODE_PRIVATE) }
    var isFavourite by remember(listing.listingId) {
        mutableStateOf(prefs.getStringSet("saved_listings", emptySet())!!.contains(listing.listingId.toString()))
    }
    val imageSource = remember(listing.listingId) {
        listing.imageUrls.split(",").firstOrNull { it.isNotBlank() }
            ?: listing.imageUrl.takeIf { it.isNotBlank() }
    }

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF1E1208).copy(alpha = 0.1f),
                spotColor    = Color(0xFF1E1208).copy(alpha = 0.08f)),
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(GroundedColors.topStripeGradient))

            // ── Image area ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                // Gradient fallback
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(GroundedColors.BarkMid, GroundedColors.ClayWarm.copy(alpha = 0.55f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Apartment, null,
                        modifier = Modifier.size(36.dp),
                        tint     = Color.White.copy(alpha = 0.18f))
                }

                // Real property image
                if (imageSource != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageSource)
                            .crossfade(400)
                            .build(),
                        contentDescription = listing.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                }

                // Bottom scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .align(Alignment.BottomStart)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.48f))
                            )
                        )
                )

                // Type badge — top-left
                if (listing.type.isNotEmpty()) {
                    Surface(
                        shape    = RoundedCornerShape(topStart = 12.dp, bottomEnd = 8.dp),
                        color    = GroundedColors.ClayWarm.copy(alpha = 0.93f),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text          = listing.type.uppercase(),
                            fontSize      = 7.sp,
                            letterSpacing = 0.5.sp,
                            color         = Color(0xFFF5E8CC),
                            fontWeight    = FontWeight.Bold,
                            modifier      = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Favourite button — top-right
                IconButton(
                    onClick  = {
                        isFavourite = !isFavourite
                        val saved = prefs.getStringSet("saved_listings", emptySet())!!.toMutableSet()
                        if (isFavourite) saved.add(listing.listingId.toString()) else saved.remove(listing.listingId.toString())
                        prefs.edit().putStringSet("saved_listings", saved).apply()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.30f), CircleShape)
                ) {
                    Icon(
                        imageVector        = if (isFavourite) Icons.Default.Favorite
                                             else Icons.Default.FavoriteBorder,
                        contentDescription = "Save listing",
                        modifier           = Modifier.size(12.dp),
                        tint               = if (isFavourite) Color(0xFFFF6B6B) else Color.White
                    )
                }

                // Price chip — bottom-left
                Surface(
                    shape    = RoundedCornerShape(topEnd = 8.dp),
                    color    = GroundedColors.ClayWarm,
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text       = "BWP ${"%.0f".format(listing.price)}",
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFFF5E8CC),
                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // ── Card content ──────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)) {
                Text(
                    text       = listing.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    fontSize   = 12.sp,
                    color      = GroundedColors.TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null,
                        Modifier.size(9.dp), tint = GroundedColors.BarkMid)
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text     = listing.location,
                        fontSize = 9.sp,
                        color    = GroundedColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(5.dp))
                // Availability badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (listing.isAvailable)
                        GroundedColors.AccentMoss.copy(alpha = 0.14f)
                    else Color(0xFFE65100).copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (listing.isAvailable) Icons.Default.CheckCircle
                                          else Icons.Default.Cancel,
                            contentDescription = null,
                            modifier           = Modifier.size(8.dp),
                            tint               = if (listing.isAvailable) GroundedColors.AccentMoss
                                                 else Color(0xFFE65100)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text       = if (listing.isAvailable) "Available" else "Taken",
                            fontSize   = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = if (listing.isAvailable) GroundedColors.AccentMoss
                                         else Color(0xFFE65100)
                        )
                    }
                }
            }
        }
    }
}

// GroundedEmptyCard moved to ui/components/AppComponents.kt

@Composable
fun GroundedFilterCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = GroundedColors.ClayWarm)
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = GroundedColors.ClayWarm, letterSpacing = 1.5.sp)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun GroundedFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick  = onClick,
        label    = {
            Text(label, fontSize = 12.sp, color = if (isSelected) Color(0xFFF5E8CC) else GroundedColors.TextSecondary)
        },
        modifier = Modifier.height(36.dp),
        colors   = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GroundedColors.ClayWarm,
            selectedLabelColor     = Color(0xFFF5E8CC),
            containerColor         = GroundedColors.CreamField,
            labelColor             = GroundedColors.TextSecondary
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
        modifier  = modifier,
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier             = Modifier.padding(12.dp),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = GroundedColors.BarkMid)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GroundedColors.ClayWarm)
            Text(label, fontSize = 10.sp, color = GroundedColors.TextMuted, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
fun GroundedProfileMenuItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Row(
        modifier          = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = GroundedColors.BarkMid)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title,    fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GroundedColors.TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = GroundedColors.TextMuted)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = GroundedColors.TextMuted)
    }
}