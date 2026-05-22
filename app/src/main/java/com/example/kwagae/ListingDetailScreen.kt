package com.example.kwagae

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kwagae.data.models.Listing
import com.example.kwagae.ui.components.*   // ← all shared widgets live here
// ChatNavArgs is in the same package — no import needed
import com.example.kwagae.ui.theme.GroundedColors
import android.content.Context

// ── Screen

@Composable
fun ListingDetailScreen(navController: NavController, listingId: Long) {
    val viewModel: ListingDetailViewModel = viewModel(
        factory = ListingDetailViewModel.factory(listingId)
    )
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GroundedColors.backgroundGradient)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                color    = GroundedColors.ClayWarm,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (state.listing == null) {
            Column(
                modifier            = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Home, null, modifier = Modifier.size(64.dp), tint = GroundedColors.TextMuted)
                Spacer(Modifier.height(8.dp))
                Text("Listing not found", fontSize = 16.sp, color = GroundedColors.TextMuted)
            }
        } else {
            DetailContent(
                listing       = state.listing!!,
                navController = navController
            )
        }
    }
}

// ── Main content ──────────────────────────────────────────────────────────────

@Composable
private fun DetailContent(listing: Listing, navController: NavController) {
    val context = LocalContext.current
    val currentUserUid = remember {
        val prefs = context.getSharedPreferences("kwagae_prefs", Context.MODE_PRIVATE)
        prefs.getString("firebase_uid", null)?.takeIf { it.isNotEmpty() }
            ?: prefs.getLong("user_id", -1L).toString()
    }
    val amenitiesList = listing.amenities
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    // Build the full image list: prefer imageUrls, fallback to imageUrl
    val allImages = remember(listing) {
        listing.imageUrls.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOfNotNull(listing.imageUrl.takeIf { it.isNotEmpty() }) }
    }

    val carouselState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Image carousel ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            if (allImages.isNotEmpty()) {
                LazyRow(
                    state             = carouselState,
                    modifier          = Modifier.fillMaxSize(),
                    userScrollEnabled = allImages.size > 1
                ) {
                    itemsIndexed(allImages) { idx, imageSource ->
                        Box(modifier = Modifier.fillParentMaxWidth()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageSource)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "${listing.title} photo ${idx + 1}",
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Dot indicators (only when multiple images)
                if (allImages.size > 1) {
                    val visibleIndex = carouselState.firstVisibleItemIndex
                    Row(
                        modifier              = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        allImages.indices.forEach { idx ->
                            Box(
                                modifier = Modifier
                                    .size(if (idx == visibleIndex) 8.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (idx == visibleIndex) Color.White
                                        else Color.White.copy(alpha = 0.45f)
                                    )
                            )
                        }
                    }

                    // Image count chip
                    Surface(
                        shape    = RoundedCornerShape(topStart = 10.dp),
                        color    = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Row(
                            modifier          = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(12.dp), tint = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text("${visibleIndex + 1} / ${allImages.size}", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            } else {
                Box(
                    modifier         = Modifier.fillMaxSize().background(GroundedColors.CreamField),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Home, null,
                        modifier = Modifier.size(80.dp),
                        tint     = GroundedColors.TextMuted
                    )
                }
            }

            // Top gradient overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent)
                        )
                    )
                    .align(Alignment.TopStart)
            )

            // Back button
            IconButton(
                onClick  = { if (navController.previousBackStackEntry != null) navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = Color.White,
                    modifier           = Modifier.size(20.dp)
                )
            }

            // Type badge top-right
            if (listing.type.isNotEmpty()) {
                Surface(
                    shape    = RoundedCornerShape(bottomStart = 10.dp),
                    color    = GroundedColors.ClayWarm.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text          = listing.type.uppercase(),
                        fontSize      = 10.sp,
                        letterSpacing = 1.5.sp,
                        color         = Color(0xFFF5E8CC),
                        fontWeight    = FontWeight.SemiBold,
                        modifier      = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Status badge bottom-left
            val badgeColor = when {
                listing.isReserved  -> Color(0xFFE65100).copy(alpha = 0.92f)
                listing.isAvailable -> GroundedColors.AccentMoss.copy(alpha = 0.9f)
                else                -> Color(0xFF757575).copy(alpha = 0.9f)
            }
            val badgeText = when {
                listing.isReserved  -> "RESERVED"
                listing.isAvailable -> "AVAILABLE"
                else                -> "UNAVAILABLE"
            }
            Surface(
                shape    = RoundedCornerShape(topEnd = 10.dp),
                color    = badgeColor,
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text          = badgeText,
                        fontSize      = 9.sp,
                        color         = Color.White,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // ── Top colour stripe ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(GroundedColors.topStripeGradient)
        )

        // ── Content card ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {

            // Title
            Text(
                text       = listing.title,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = GroundedColors.TextPrimary,
                lineHeight = 28.sp
            )

            Spacer(Modifier.height(8.dp))

            // Location row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn, null,
                    modifier = Modifier.size(16.dp),
                    tint     = GroundedColors.BarkMid
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text     = listing.location,
                    fontSize = 14.sp,
                    color    = GroundedColors.TextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            //  Price & Deposit card
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(14.dp),
                colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PriceStatItem(
                        label  = "MONTHLY RENT",
                        value  = "BWP ${"%,.0f".format(listing.price)}",
                        icon   = Icons.Default.Home,
                        color  = GroundedColors.ClayWarm
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(48.dp)
                            .background(GroundedColors.BorderDefault)
                    )
                    PriceStatItem(
                        label  = "DEPOSIT",
                        value  = "BWP ${"%,.0f".format(listing.depositAmount)}",
                        icon   = Icons.Default.Lock,
                        color  = GroundedColors.BarkMid
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Key details ───────────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(14.dp),
                colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailSectionTitle("KEY DETAILS")
                    Spacer(Modifier.height(8.dp))

                    if (listing.type.isNotEmpty()) {
                        DetailRow(Icons.Default.House, "Property Type", listing.type)
                    }
                    DetailRow(
                        Icons.Default.CalendarToday,
                        "Available From",
                        listing.availabilityDate.ifEmpty { "Immediately" }
                    )
                    DetailRow(
                        Icons.Default.LocationOn,
                        "Location",
                        listing.location
                    )
                    DetailRow(
                        Icons.Default.CheckCircle,
                        "Status",
                        when {
                            listing.isReserved  -> "Reserved"
                            listing.isAvailable -> "Available"
                            else                -> "Not Available"
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Description ───────────────────────────────────────────────────
            if (listing.description.isNotEmpty()) {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailSectionTitle("DESCRIPTION")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text      = listing.description,
                            fontSize  = 14.sp,
                            color     = GroundedColors.TextSecondary,
                            lineHeight = 22.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Amenities ─────────────────────────────────────────────────────
            if (amenitiesList.isNotEmpty()) {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailSectionTitle("AMENITIES")
                        Spacer(Modifier.height(10.dp))

                        // Wrap chips manually since we can't use FlowRow without Accompanist
                        val chunked = amenitiesList.chunked(2)
                        chunked.forEach { pair ->
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { amenity ->
                                    AmenityChip(
                                        label    = amenity,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Safety tip ───────────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(14.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = GroundedColors.AccentMoss.copy(alpha = 0.12f)
                ),
                border    = BorderStroke(1.dp, GroundedColors.AccentMoss.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier          = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security, null,
                        modifier = Modifier.size(20.dp),
                        tint     = GroundedColors.AccentMoss
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text       = "Always visit the property before paying any deposit.",
                        fontSize   = 12.sp,
                        color      = GroundedColors.AccentMoss,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Reservation / status button ───────────────────────────────────
            when {
                // This user's own reservation — show receipt info
                listing.isReserved && listing.reservedByUid == currentUserUid -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(28.dp), tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("YOU RESERVED THIS PROPERTY", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF2E7D32))
                                Text("Ref: ${listing.reservationRef}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                }

                // Reserved by someone else — block
                listing.isReserved -> {
                    Button(
                        onClick  = {},
                        enabled  = false,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFF757575).copy(alpha = 0.4f)
                        )
                    ) {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.7f))
                        Spacer(Modifier.width(10.dp))
                        Text("ALREADY RESERVED", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }

                // Unavailable (but not reserved)
                !listing.isAvailable -> {
                    Button(
                        onClick  = {},
                        enabled  = false,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            disabledContainerColor = GroundedColors.BorderDefault
                        )
                    ) {
                        Text("UNAVAILABLE", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp, color = GroundedColors.TextMuted)
                    }
                }

                // Available — show reserve button
                else -> {
                    Button(
                        onClick  = { navController.navigate("reserve/${listing.listingId}") },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = GroundedColors.ClayWarm)
                    ) {
                        Icon(Icons.Default.Payment, null, modifier = Modifier.size(18.dp), tint = Color(0xFFF5E8CC))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "RESERVE — PAY BWP ${"%,.0f".format(listing.depositAmount)} DEPOSIT",
                            fontSize      = 13.sp,
                            fontWeight    = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            color         = Color(0xFFF5E8CC)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Contact button
            OutlinedButton(
                onClick  = {
                    val userId = context
                        .getSharedPreferences("kwagae_prefs", android.content.Context.MODE_PRIVATE)
                        .getLong("user_id", -1L)
                    if (userId == -1L) {
                        Toast.makeText(context, "Please log in to contact the landlord.", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }
                    ChatNavArgs.apply {
                        threadId     = "chat_u${userId}_l${listing.listingId}"
                        providerId   = listing.ownerUid.ifEmpty { "PR001" }
                        providerName = listing.providerName.ifEmpty { "KwaGae Housing" }
                        listingId    = listing.listingId
                        listingTitle = listing.title
                    }
                    navController.navigate("chat")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape  = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, GroundedColors.BarkMid),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GroundedColors.BarkMid
                )
            ) {
                Icon(
                    Icons.Default.Phone, null,
                    modifier = Modifier.size(16.dp),
                    tint     = GroundedColors.BarkMid
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text          = "CONTACT LANDLORD",
                    fontSize      = 13.sp,
                    fontWeight    = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color         = GroundedColors.BarkMid
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun PriceStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
        Spacer(Modifier.height(4.dp))
        Text(
            text          = label,
            fontSize      = 9.sp,
            letterSpacing = 1.sp,
            color         = GroundedColors.TextMuted,
            fontWeight    = FontWeight.Medium
        )
        Text(
            text       = value,
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            color      = color
        )
    }
}

@Composable
private fun DetailSectionTitle(text: String) {
    Text(
        text          = text,
        fontSize      = 10.sp,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 2.sp,
        color         = GroundedColors.ClayWarm
    )
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = GroundedColors.BarkMid)
        Spacer(Modifier.width(10.dp))
        Text(
            text       = label,
            fontSize   = 12.sp,
            color      = GroundedColors.TextMuted,
            modifier   = Modifier.width(110.dp)
        )
        Text(
            text       = value,
            fontSize   = 13.sp,
            color      = GroundedColors.TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(GroundedColors.BorderDefault)
    )
}

@Composable
private fun AmenityChip(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(8.dp),
        color    = GroundedColors.CreamField,
        border   = BorderStroke(1.dp, GroundedColors.BorderDefault)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Check, null,
                modifier = Modifier.size(12.dp),
                tint     = GroundedColors.AccentMoss
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text     = label,
                fontSize = 11.sp,
                color    = GroundedColors.TextSecondary,
                maxLines = 1
            )
        }
    }
}
