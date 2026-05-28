package com.example.kwagae

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kwagae.data.models.Listing
import com.example.kwagae.ui.components.GroundedDivider
import com.example.kwagae.ui.components.LeafShape
import com.example.kwagae.ui.theme.GroundedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(navController: NavController) {

    val viewModel: MyReservationsViewModel = viewModel()
    val reservations by viewModel.reservations.collectAsState()
    var hasLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(reservations) { hasLoaded = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MY RESERVATIONS",
                        fontSize      = 14.sp,
                        fontWeight    = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        color         = Color(0xFFF5E8CC)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFF5E8CC)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GroundedColors.BarkMid
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GroundedColors.backgroundGradient)
                .padding(padding)
        ) {
            // Decorative leaf shapes — always rendered so slot table stays consistent
            LeafShape(Modifier.size(220.dp).offset(x = (-70).dp, y = (-40).dp))
            LeafShape(Modifier.size(160.dp).offset(x = 240.dp, y = 600.dp))

            // ── Content: loading / empty / list (use if-else, never return@Box) ──
            when {

                // Loading
                !hasLoaded -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color       = GroundedColors.ClayWarm,
                            strokeWidth = 3.dp,
                            modifier    = Modifier.size(36.dp)
                        )
                    }
                }

                // Empty state
                reservations.isEmpty() -> {
                    Column(
                        modifier            = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    GroundedColors.ClayWarm.copy(alpha = 0.15f),
                                    RoundedCornerShape(24.dp)
                                )
                        ) {
                            Icon(
                                Icons.Default.HomeWork,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint     = GroundedColors.ClayWarm
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "No reservations yet",
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color(0xFFF5E8CC)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text      = "When you pay a deposit on a listing,\nyour reservation will appear here.",
                            fontSize  = 13.sp,
                            color     = Color(0xFFF5E8CC).copy(alpha = 0.70f),
                            lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(28.dp))
                        Button(
                            // Navigate back to main so the student can browse listings
                            onClick  = { navController.navigate("main") {
                                popUpTo("main") { inclusive = false }
                            }},
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = GroundedColors.ClayWarm
                            )
                        ) {
                            Icon(Icons.Default.Search, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Browse Listings", fontSize = 14.sp)
                        }
                    }
                }

                // Reservations list
                else -> {
                    LazyColumn(
                        contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ReservationSummaryBanner(count = reservations.size)
                        }

                        items(items = reservations, key = { it.listingId }) { listing ->
                            ReservationCard(
                                listing = listing,
                                onClick = {
                                    navController.navigate("listing_detail/${listing.listingId}")
                                }
                            )
                        }

                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

// ── Summary banner ─────────────────────────────────────────────────────────────

@Composable
private fun ReservationSummaryBanner(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(GroundedColors.ClayWarm, GroundedColors.BarkMid)),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    Icons.Default.Key,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint     = Color(0xFFF5E8CC)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "$count Active Reservation${if (count != 1) "s" else ""}",
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFFF5E8CC)
                )
                Text(
                    "Tap a card to view full details",
                    fontSize  = 11.sp,
                    color     = Color(0xFFF5E8CC).copy(alpha = 0.75f),
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

// ── Reservation card ───────────────────────────────────────────────────────────

@Composable
private fun ReservationCard(listing: Listing, onClick: () -> Unit) {
    val context = LocalContext.current

    val imageSource = remember(listing.listingId) {
        listing.imageUrls.split(",").firstOrNull { it.isNotBlank() }
            ?: listing.imageUrl.takeIf { it.isNotBlank() }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                8.dp, RoundedCornerShape(18.dp),
                ambientColor = Color(0xFF1E1208).copy(alpha = 0.12f),
                spotColor    = Color(0xFF1E1208).copy(alpha = 0.08f)
            )
            .clickable(onClick = onClick),
        shape  = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard)
    ) {
        Column {

            // ── Image area ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            ) {
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
                } else {
                    Box(
                        modifier         = Modifier
                            .fillMaxSize()
                            .background(GroundedColors.ClayWarm.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Home, null,
                            Modifier.size(48.dp),
                            GroundedColors.ClayWarm.copy(alpha = 0.4f)
                        )
                    }
                }

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to Color(0xFF1E1208).copy(alpha = 0.55f)
                            )
                        )
                )

                // Type badge — top left
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                    shape    = RoundedCornerShape(8.dp),
                    color    = GroundedColors.BarkMid.copy(alpha = 0.85f)
                ) {
                    Text(
                        text          = listing.type,
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.Medium,
                        color         = Color(0xFFF5E8CC),
                        modifier      = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        letterSpacing = 0.5.sp
                    )
                }

                // RESERVED badge — top right
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopEnd),
                    shape    = RoundedCornerShape(8.dp),
                    color    = GroundedColors.AccentMoss
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(11.dp), Color(0xFFF5E8CC))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "RESERVED",
                            fontSize      = 9.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = Color(0xFFF5E8CC),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Price chip — bottom left
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.BottomStart),
                    shape    = RoundedCornerShape(10.dp),
                    color    = GroundedColors.ClayWarm
                ) {
                    Text(
                        text       = "BWP ${listing.price.toInt()}/mo",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFFF5E8CC),
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // ── Details section ───────────────────────────────────────────────
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text       = listing.title,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = GroundedColors.TextPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn, null,
                        Modifier.size(13.dp), GroundedColors.ClayWarm
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text     = listing.location,
                        fontSize = 12.sp,
                        color    = GroundedColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(12.dp))
                GroundedDivider()
                Spacer(Modifier.height(12.dp))

                // Ref + availability date + chevron
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Ref number
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ConfirmationNumber, null,
                            Modifier.size(14.dp), GroundedColors.BarkMid
                        )
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(
                                "Ref Number",
                                fontSize      = 10.sp,
                                color         = GroundedColors.TextMuted,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                text       = listing.reservationRef.ifEmpty { "—" },
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = GroundedColors.TextPrimary
                            )
                        }
                    }

                    // Availability date
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday, null,
                            Modifier.size(14.dp), GroundedColors.BarkMid
                        )
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(
                                "Available",
                                fontSize      = 10.sp,
                                color         = GroundedColors.TextMuted,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                text       = listing.availabilityDate.ifEmpty { "—" },
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = GroundedColors.TextPrimary
                            )
                        }
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "View details",
                        modifier = Modifier.size(20.dp),
                        tint     = GroundedColors.TextMuted
                    )
                }
            }
        }
    }
}
