package com.example.kwagae

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kwagae.data.models.Listing
import com.example.kwagae.ui.theme.GroundedColors

// ── Provider Dashboard ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDashboardScreen(navController: NavController) {
    val viewModel: ProviderDashboardViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show deletion confirmation snackbar
    LaunchedEffect(state.deletedMessage) {
        if (state.deletedMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(state.deletedMessage)
            viewModel.clearDeletedMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("ADD LISTING", letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add listing") },
                onClick = { navController.navigate("add_listing") },
                containerColor = GroundedColors.ClayWarm,
                contentColor   = Color(0xFFF5E8CC),
                shape          = RoundedCornerShape(14.dp)
            )
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GroundedColors.backgroundGradient)
                .padding(scaffoldPadding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        color    = GroundedColors.ClayWarm,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        // ── Header ─────────────────────────────────────────────
                        item {
                            ProviderHeader(name = viewModel.ownerName)
                        }

                        // ── Stats row ──────────────────────────────────────────
                        item {
                            ProviderStatsRow(
                                total       = state.totalCount,
                                available   = state.availableCount,
                                unavailable = state.unavailableCount
                            )
                        }

                        // ── Search bar ─────────────────────────────────────────
                        item {
                            OutlinedTextField(
                                value         = searchQuery,
                                onValueChange = { viewModel.updateSearch(it) },
                                placeholder   = { Text("Search your listings…", fontSize = 13.sp) },
                                leadingIcon   = {
                                    Icon(Icons.Default.Search, null,
                                        modifier = Modifier.size(18.dp),
                                        tint     = GroundedColors.BarkMid)
                                },
                                trailingIcon  = if (searchQuery.isNotEmpty()) ({
                                    IconButton(onClick = { viewModel.updateSearch("") }) {
                                        Icon(Icons.Default.Clear, null,
                                            modifier = Modifier.size(16.dp),
                                            tint     = GroundedColors.TextMuted)
                                    }
                                }) else null,
                                singleLine    = true,
                                shape         = RoundedCornerShape(12.dp),
                                modifier      = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                colors        = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = GroundedColors.CreamCard,
                                    focusedContainerColor   = GroundedColors.CreamCard,
                                    unfocusedBorderColor    = GroundedColors.BorderDefault,
                                    focusedBorderColor      = GroundedColors.ClayWarm,
                                    cursorColor             = GroundedColors.ClayWarm,
                                    focusedTextColor        = GroundedColors.TextPrimary,
                                    unfocusedTextColor      = GroundedColors.TextPrimary
                                )
                            )
                        }

                        if (state.listings.isEmpty() && !state.isLoading) {
                            // ── Empty state ────────────────────────────────────
                            item {
                                if (searchQuery.isNotEmpty()) {
                                    // No search results
                                    Box(
                                        Modifier.fillMaxWidth().padding(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.SearchOff, null,
                                                modifier = Modifier.size(48.dp),
                                                tint     = GroundedColors.TextMuted)
                                            Spacer(Modifier.height(12.dp))
                                            Text("No listings match \"$searchQuery\"",
                                                fontSize   = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color      = Color(0xFFF5E8CC))
                                            TextButton(onClick = { viewModel.updateSearch("") }) {
                                                Text("Clear search", color = GroundedColors.ClayWarm)
                                            }
                                        }
                                    }
                                } else {
                                    ProviderEmptyState(
                                        onAddClick = { navController.navigate("add_listing") }
                                    )
                                }
                            }
                        } else {
                            // ── Listings ───────────────────────────────────────
                            item {
                                Text(
                                    text          = "MY LISTINGS",
                                    fontSize      = 11.sp,
                                    letterSpacing = 1.5.sp,
                                    fontWeight    = FontWeight.SemiBold,
                                    color         = Color(0xFFF5E8CC).copy(alpha = 0.7f),
                                    modifier      = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            items(state.listings, key = { it.listingId }) { listing ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter   = fadeIn(tween(300)) + slideInVertically()
                                ) {
                                    ProviderListingCard(
                                        listing        = listing,
                                        onEdit         = { navController.navigate("edit_listing/${listing.listingId}") },
                                        onDelete       = { viewModel.deleteListing(listing) },
                                        onToggleStatus = { viewModel.toggleAvailability(listing) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun ProviderHeader(name: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GroundedColors.BarkMid)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(GroundedColors.topStripeGradient)
            )
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GroundedColors.ClayWarm)
                ) {
                    Text(
                        text       = name.firstOrNull()?.uppercase() ?: "P",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFFF5E8CC)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text       = "Welcome back,",
                        fontSize   = 12.sp,
                        color      = Color(0xFFF5E8CC).copy(alpha = 0.7f)
                    )
                    Text(
                        text       = name,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFFF5E8CC)
                    )
                }
                Spacer(Modifier.weight(1f))
                // Provider badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GroundedColors.ClayWarm.copy(alpha = 0.25f)
                ) {
                    Text(
                        text          = "PROPERTY HOST",
                        fontSize      = 9.sp,
                        letterSpacing = 1.sp,
                        fontWeight    = FontWeight.SemiBold,
                        color         = Color(0xFFF5E8CC),
                        modifier      = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

// ── Stats Row ─────────────────────────────────────────────────────────────────

@Composable
private fun ProviderStatsRow(total: Int, available: Int, unavailable: Int) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProviderStatCard("TOTAL",       total.toString(),       GroundedColors.ClayWarm,   Icons.Default.Home,        Modifier.weight(1f))
        ProviderStatCard("AVAILABLE",   available.toString(),   GroundedColors.AccentMoss,  Icons.Default.CheckCircle, Modifier.weight(1f))
        ProviderStatCard("UNAVAILABLE", unavailable.toString(), GroundedColors.BarkMid,     Icons.Default.Cancel,      Modifier.weight(1f))
    }
}

@Composable
private fun ProviderStatCard(
    label: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 8.sp, letterSpacing = 1.sp, color = GroundedColors.TextMuted, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Listing Card ──────────────────────────────────────────────────────────────

@Composable
private fun ProviderListingCard(
    listing: Listing,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete listing?", fontWeight = FontWeight.Bold) },
            text  = { Text("\"${listing.title}\" will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("DELETE", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column {
            // ── Image + type badge ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val imageSource = listing.imageUrls.split(",").firstOrNull { it.isNotBlank() }
                    ?: listing.imageUrl.takeIf { it.isNotBlank() }

                if (imageSource != null) {
                    AsyncImage(
                        model              = imageSource,
                        contentDescription = listing.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier         = Modifier
                            .fillMaxSize()
                            .background(GroundedColors.CreamField),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier           = Modifier.size(48.dp),
                            tint               = GroundedColors.TextMuted
                        )
                    }
                }

                // Bottom gradient for readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomStart)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                            )
                        )
                )

                // Type chip
                if (listing.type.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(bottomEnd = 10.dp),
                        color = GroundedColors.ClayWarm.copy(alpha = 0.9f),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text          = listing.type.uppercase(),
                            fontSize      = 9.sp,
                            letterSpacing = 1.sp,
                            color         = Color(0xFFF5E8CC),
                            fontWeight    = FontWeight.SemiBold,
                            modifier      = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Image count badge (if multiple)
                val imgCount = listing.imageUrls.split(",").count { it.isNotBlank() }
                if (imgCount > 1) {
                    Surface(
                        shape    = RoundedCornerShape(topStart = 10.dp),
                        color    = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Row(
                            modifier          = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(12.dp), tint = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text("$imgCount", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }

            // ── Details ────────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = listing.title,
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = GroundedColors.TextPrimary,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn, null,
                                modifier = Modifier.size(12.dp),
                                tint     = GroundedColors.BarkMid
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text     = listing.location,
                                fontSize = 11.sp,
                                color    = GroundedColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    // Price
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text       = "BWP ${"%,.0f".format(listing.price)}",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = GroundedColors.ClayWarm
                        )
                        Text(
                            text    = "/month",
                            fontSize = 10.sp,
                            color   = GroundedColors.TextMuted
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // ── Action bar ─────────────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Availability toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked         = listing.isAvailable,
                            onCheckedChange = { onToggleStatus() },
                            modifier        = Modifier.height(24.dp),
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor       = Color.White,
                                checkedTrackColor       = GroundedColors.AccentMoss,
                                uncheckedThumbColor     = Color.White,
                                uncheckedTrackColor     = GroundedColors.TextMuted.copy(alpha = 0.4f)
                            )
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text      = if (listing.isAvailable) "Available" else "Unavailable",
                            fontSize  = 11.sp,
                            color     = if (listing.isAvailable) GroundedColors.AccentMoss else GroundedColors.TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Edit + Delete buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.Edit, null,
                                modifier = Modifier.size(18.dp),
                                tint     = GroundedColors.BarkMid
                            )
                        }
                        IconButton(
                            onClick  = { showDeleteDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete, null,
                                modifier = Modifier.size(18.dp),
                                tint     = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun ProviderEmptyState(onAddClick: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(GroundedColors.ClayWarm.copy(alpha = 0.15f))
        ) {
            Icon(
                Icons.Default.AddHome, null,
                modifier = Modifier.size(40.dp),
                tint     = GroundedColors.ClayWarm
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text       = "No listings yet",
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFFF5E8CC)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text      = "Create your first accommodation listing to start receiving enquiries from students.",
            fontSize  = 13.sp,
            color     = Color(0xFFF5E8CC).copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            shape   = RoundedCornerShape(12.dp),
            colors  = ButtonDefaults.buttonColors(containerColor = GroundedColors.ClayWarm)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("CREATE FIRST LISTING", letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
