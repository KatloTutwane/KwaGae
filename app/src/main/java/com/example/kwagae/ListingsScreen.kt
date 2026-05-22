@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)

package com.example.kwagae

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kwagae.data.models.Listing
import com.example.kwagae.ui.components.*
import com.example.kwagae.ui.theme.GroundedColors
import java.text.SimpleDateFormat
import java.util.*

// All property types present in the seeded data
val ALL_PROPERTY_TYPES = listOf(
    "House", "Apartment", "Studio", "Room",
    "Townhouse", "Bachelor Flat", "Duplex", "Bedsitter"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(
    navController: NavController,
    viewModel: ListingsViewModel
) {
    val filters by viewModel.filters.collectAsState()
    val filteredListings by viewModel.filteredListings.collectAsState()
    val hasActiveFilters by viewModel.hasActiveFilters.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showDatePicker  by remember { mutableStateOf(false) }
    // Show a brief loading spinner until the Room Flow emits its first result
    var hasLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(filteredListings) { hasLoaded = true }
    val isLoading = !hasLoaded

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GroundedColors.backgroundGradient)
    ) {
        LeafShape(Modifier.size(250.dp).offset(x = (-80).dp, y = (-60).dp))
        LeafShape(Modifier.size(180.dp).offset(x = 240.dp, y = 800.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Top stripe
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(GroundedColors.topStripeGradient)
                )
            }

            // Title
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("FIND YOUR", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                        color = GroundedColors.ClayWarm, letterSpacing = 3.sp)
                    Text("GROUNDED HOME", fontSize = 28.sp, fontWeight = FontWeight.SemiBold,
                        color = GroundedColors.TextPrimary, letterSpacing = 1.sp)
                }
            }

            // Search + filter bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp),
                            ambientColor = Color(0xFF1E1208).copy(alpha = 0.2f),
                            spotColor    = Color(0xFF1E1208).copy(alpha = 0.15f)),
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GroundedSearchField(
                            value         = filters.searchQuery,
                            onValueChange = { viewModel.updateSearch(it) },
                            placeholder   = "Search by title or location..."
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Filter button
                            OutlinedButton(
                                onClick = { showFilterSheet = true },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape    = RoundedCornerShape(10.dp),
                                border   = BorderStroke(
                                    1.dp,
                                    if (hasActiveFilters) GroundedColors.ClayWarm
                                    else GroundedColors.BorderDefault
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (hasActiveFilters)
                                        GroundedColors.ClayWarm.copy(alpha = 0.1f)
                                    else GroundedColors.CreamField
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (hasActiveFilters) GroundedColors.ClayWarm else GroundedColors.BarkMid
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (hasActiveFilters) "Filters active" else "Filters",
                                    fontSize = 12.sp,
                                    color = if (hasActiveFilters) GroundedColors.ClayWarm else GroundedColors.BarkMid
                                )
                            }

                            // Clear all
                            AnimatedVisibility(visible = hasActiveFilters) {
                                OutlinedButton(
                                    onClick  = { viewModel.clearFilters() },
                                    modifier = Modifier.height(38.dp),
                                    shape    = RoundedCornerShape(10.dp),
                                    border   = BorderStroke(1.dp, GroundedColors.BorderDefault),
                                    colors   = ButtonDefaults.outlinedButtonColors(
                                        containerColor = GroundedColors.CreamField
                                    )
                                ) {
                                    Icon(Icons.Default.Clear, null, Modifier.size(14.dp),
                                        tint = GroundedColors.TextMuted)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Clear", fontSize = 12.sp, color = GroundedColors.TextMuted)
                                }
                            }
                        }

                        // Active filter chips row
                        AnimatedVisibility(visible = hasActiveFilters) {
                            Column {
                                Spacer(Modifier.height(8.dp))
                                ActiveFilterChipsRow(filters, viewModel)
                            }
                        }
                    }
                }
            }

            // Result count
            item {
                AnimatedVisibility(visible = !isLoading) {
                    Text(
                        text = "${filteredListings.size} listing${if (filteredListings.size != 1) "s" else ""} found",
                        fontSize = 11.sp,
                        color = GroundedColors.TextMuted,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
            }

            // Loading state
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = GroundedColors.ClayWarm,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("Finding your grounded home…", fontSize = 13.sp,
                                color = GroundedColors.TextMuted)
                        }
                    }
                }
            }

            // Empty state
            else if (filteredListings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = GroundedColors.TextMuted
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("No listings match your filters", fontSize = 18.sp,
                                fontWeight = FontWeight.Medium, color = GroundedColors.TextSecondary)
                            Text("Try clearing some filters", fontSize = 13.sp,
                                color = GroundedColors.TextMuted)
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { viewModel.clearFilters() },
                                shape   = RoundedCornerShape(10.dp),
                                border  = BorderStroke(1.dp, GroundedColors.ClayWarm)
                            ) {
                                Text("Clear Filters", color = GroundedColors.ClayWarm, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else if (!isLoading) {
                items(items = filteredListings, key = { it.listingId }) { listing ->
                    GroundedListingCard(
                        listing = listing,
                        navController = navController,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // ── Filter Bottom Sheet ────────────────────────────────────────────────────
    if (showFilterSheet) {
        FilterBottomSheet(
            filters     = filters,
            viewModel   = viewModel,
            onDateClick = { showFilterSheet = false; showDatePicker = true },
            onDismiss   = { showFilterSheet = false }
        )
    }

    // ── Date Picker Dialog ────────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = filters.availabilityDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateAvailabilityDate(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) {
                    Text("OK", color = GroundedColors.ClayWarm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = GroundedColors.TextMuted)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = GroundedColors.ClayWarm,
                    todayDateBorderColor      = GroundedColors.ClayWarm
                )
            )
        }
    }
}

// ── Active filter chips displayed in the search card ──────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveFilterChipsRow(filters: FilterState, viewModel: ListingsViewModel) {
    val dateFmt = remember { SimpleDateFormat("d MMM yyyy", Locale.ENGLISH) }

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (filters.minPrice > 0f || filters.maxPrice < 15000f) {
            FilterActiveChip(
                label   = "BWP ${filters.minPrice.toInt()}–${filters.maxPrice.toInt()}",
                onRemove = { viewModel.updatePriceRange(0f, 15000f) }
            )
        }
        filters.selectedTypes.forEach { type ->
            FilterActiveChip(label = type, onRemove = { viewModel.toggleType(type) })
        }
        filters.availabilityDateMillis?.let { millis ->
            FilterActiveChip(
                label   = "By ${dateFmt.format(Date(millis))}",
                onRemove = { viewModel.updateAvailabilityDate(null) }
            )
        }
        if (filters.wifiOnly)     FilterActiveChip("Wi-Fi",     { viewModel.setWifiOnly(false) })
        if (filters.furnishedOnly) FilterActiveChip("Furnished", { viewModel.setFurnishedOnly(false) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterActiveChip(label: String, onRemove: () -> Unit) {
    InputChip(
        selected  = true,
        onClick   = onRemove,
        label     = { Text(label, fontSize = 11.sp) },
        trailingIcon = {
            Icon(Icons.Default.Close, null, Modifier.size(14.dp))
        },
        modifier = Modifier.height(30.dp),
        colors   = InputChipDefaults.inputChipColors(
            selectedContainerColor = GroundedColors.ClayWarm.copy(alpha = 0.15f),
            selectedLabelColor     = GroundedColors.ClayWarm,
            selectedTrailingIconColor = GroundedColors.ClayWarm
        )
    )
}

// ── Filter Bottom Sheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    filters: FilterState,
    viewModel: ListingsViewModel,
    onDateClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFmt    = remember { SimpleDateFormat("d MMM yyyy", Locale.ENGLISH) }

    ModalBottomSheet(
        onDismissRequest   = onDismiss,
        sheetState         = sheetState,
        containerColor     = GroundedColors.CreamCard,
        dragHandle         = {
            Box(
                Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GroundedColors.BorderDefault)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Sheet header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FILTERS", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = GroundedColors.TextPrimary, letterSpacing = 2.sp)
                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text("Clear all", fontSize = 12.sp, color = GroundedColors.ClayWarm)
                    }
                }
                GroundedDivider(Modifier.padding(bottom = 16.dp))
            }

            // Price range
            item {
                FilterSection(title = "PRICE RANGE", icon = Icons.Default.AttachMoney) {
                    Text(
                        text = "BWP ${filters.minPrice.toInt()} – BWP ${filters.maxPrice.toInt()}/mo",
                        fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                        color = GroundedColors.ClayWarm
                    )
                    Spacer(Modifier.height(8.dp))
                    RangeSlider(
                        value         = filters.minPrice..filters.maxPrice,
                        onValueChange = { r -> viewModel.updatePriceRange(r.start, r.endInclusive) },
                        valueRange    = 0f..15000f,
                        colors        = SliderDefaults.colors(
                            thumbColor       = GroundedColors.ClayWarm,
                            activeTrackColor = GroundedColors.ClayWarm,
                            inactiveTrackColor = GroundedColors.BorderDefault
                        )
                    )
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("BWP 0", fontSize = 10.sp, color = GroundedColors.TextMuted)
                        Text("BWP 15,000", fontSize = 10.sp, color = GroundedColors.TextMuted)
                    }
                }
            }

            // Property type
            item {
                FilterSection(title = "PROPERTY TYPE", icon = Icons.Default.House) {
                    FlowRowFilterChips(
                        options   = ALL_PROPERTY_TYPES,
                        selected  = filters.selectedTypes,
                        onToggle  = { viewModel.toggleType(it) }
                    )
                }
            }

            // Availability date
            item {
                FilterSection(title = "AVAILABILITY DATE", icon = Icons.Default.CalendarToday) {
                    Text(
                        text     = "Show listings available by:",
                        fontSize = 12.sp,
                        color    = GroundedColors.TextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onDateClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(10.dp),
                        border   = BorderStroke(1.dp,
                            if (filters.availabilityDateMillis != null) GroundedColors.ClayWarm
                            else GroundedColors.BorderDefault
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = GroundedColors.CreamField)
                    ) {
                        Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp),
                            tint = GroundedColors.BarkMid)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = filters.availabilityDateMillis?.let {
                                dateFmt.format(Date(it))
                            } ?: "Select move-in date",
                            fontSize = 13.sp,
                            color = if (filters.availabilityDateMillis != null)
                                GroundedColors.ClayWarm else GroundedColors.TextHint
                        )
                    }
                    if (filters.availabilityDateMillis != null) {
                        TextButton(
                            onClick  = { viewModel.updateAvailabilityDate(null) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Remove date filter", fontSize = 11.sp, color = GroundedColors.TextMuted)
                        }
                    }
                }
            }

            // Amenity toggles
            item {
                FilterSection(title = "AMENITIES", icon = Icons.Default.Star) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, null, Modifier.size(18.dp),
                                tint = GroundedColors.BarkMid)
                            Spacer(Modifier.width(8.dp))
                            Text("Wi-Fi / Fibre available", fontSize = 13.sp,
                                color = GroundedColors.TextPrimary)
                        }
                        Switch(
                            checked  = filters.wifiOnly,
                            onCheckedChange = { viewModel.setWifiOnly(it) },
                            colors   = SwitchDefaults.colors(
                                checkedThumbColor  = Color.White,
                                checkedTrackColor  = GroundedColors.ClayWarm
                            )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Chair, null, Modifier.size(18.dp),
                                tint = GroundedColors.BarkMid)
                            Spacer(Modifier.width(8.dp))
                            Text("Furnished only", fontSize = 13.sp,
                                color = GroundedColors.TextPrimary)
                        }
                        Switch(
                            checked  = filters.furnishedOnly,
                            onCheckedChange = { viewModel.setFurnishedOnly(it) },
                            colors   = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GroundedColors.ClayWarm
                            )
                        )
                    }
                }
            }

            // Apply button
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick   = onDismiss,
                    modifier  = Modifier.fillMaxWidth().height(50.dp),
                    shape     = RoundedCornerShape(10.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = GroundedColors.ClayWarm)
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(18.dp), tint = Color(0xFFF5E8CC))
                    Spacer(Modifier.width(8.dp))
                    Text("SHOW RESULTS", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp, color = Color(0xFFF5E8CC))
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(14.dp), tint = GroundedColors.ClayWarm)
            Spacer(Modifier.width(6.dp))
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Medium,
                color = GroundedColors.ClayWarm, letterSpacing = 1.5.sp)
        }
        Spacer(Modifier.height(10.dp))
        content()
        Spacer(Modifier.height(12.dp))
        GroundedDivider()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowFilterChips(
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option in selected
            FilterChip(
                selected = isSelected,
                onClick  = { onToggle(option) },
                label    = { Text(option, fontSize = 12.sp) },
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

// ── Listing card (full-width) ─────────────────────────────────────────────────

@Composable
fun GroundedListingCard(
    listing: Listing,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { navController.navigate("listing_detail/${listing.listingId}") }
            .shadow(12.dp, RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF1E1208).copy(alpha = 0.15f),
                spotColor    = Color(0xFF1E1208).copy(alpha = 0.1f)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard)
    ) {
        Column {
            // ── Listing image ─────────────────────────────────────────────────
            val imageSource = listing.imageUrls.split(",").firstOrNull { it.isNotBlank() }
                ?: listing.imageUrl.takeIf { it.isNotBlank() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                if (imageSource != null) {
                    AsyncImage(
                        model              = imageSource,
                        contentDescription = listing.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .background(GroundedColors.CreamField),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Home, null,
                            modifier = Modifier.size(56.dp),
                            tint     = GroundedColors.TextMuted
                        )
                    }
                }

                // Bottom gradient for readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .align(Alignment.BottomStart)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                            )
                        )
                )

                // Property type badge (top-left)
                if (listing.type.isNotEmpty()) {
                    Surface(
                        shape    = RoundedCornerShape(topStart = 16.dp, bottomEnd = 10.dp),
                        color    = GroundedColors.ClayWarm.copy(alpha = 0.92f),
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

                // Reserved badge (top-right)
                if (listing.isReserved) {
                    Surface(
                        shape    = RoundedCornerShape(topEnd = 16.dp, bottomStart = 10.dp),
                        color    = Color(0xFFE65100).copy(alpha = 0.92f),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text          = "RESERVED",
                            fontSize      = 9.sp,
                            letterSpacing = 1.sp,
                            color         = Color.White,
                            fontWeight    = FontWeight.SemiBold,
                            modifier      = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Title + price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(listing.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                        color = GroundedColors.TextPrimary, maxLines = 1,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(8.dp),
                        color = GroundedColors.ClayWarm.copy(alpha = 0.15f),
                        modifier = Modifier.padding(start = 8.dp)) {
                        Text("BWP ${"%.0f".format(listing.price)}/mo", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, color = GroundedColors.ClayWarm,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Location
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, "Location", Modifier.size(14.dp),
                        tint = GroundedColors.BarkMid)
                    Spacer(Modifier.width(4.dp))
                    Text(listing.location, fontSize = 13.sp, color = GroundedColors.TextSecondary)
                }
                Spacer(Modifier.height(8.dp))
                // Availability + bed/bath
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(6.dp),
                        color = GroundedColors.AccentMoss.copy(alpha = 0.15f)) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, "Available", Modifier.size(12.dp),
                                tint = GroundedColors.AccentMoss)
                            Spacer(Modifier.width(4.dp))
                            Text("Available ${listing.availabilityDate}", fontSize = 11.sp,
                                color = GroundedColors.AccentMoss, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (listing.amenities.isNotBlank()) {
                        val list = listing.amenities.split(",").map { it.trim() }
                        val bed  = list.find { it.contains("bed", ignoreCase = true) }
                        val bath = list.find { it.contains("bath", ignoreCase = true) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            bed?.let  { Text("🛏️ $it", fontSize = 11.sp, color = GroundedColors.TextMuted) }
                            bath?.let { Text("🚿 $it", fontSize = 11.sp, color = GroundedColors.TextMuted) }
                        }
                    }
                }
                // Amenity chips
                if (listing.amenities.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    val chips = listing.amenities.split(",").map { it.trim() }
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp), Alignment.CenterVertically) {
                        chips.take(3).forEach { amenity ->
                            AssistChip(
                                onClick = {},
                                label   = { Text(amenity, fontSize = 10.sp, maxLines = 1) },
                                modifier = Modifier.height(28.dp).weight(1f, fill = false),
                                colors   = AssistChipDefaults.assistChipColors(
                                    containerColor = GroundedColors.CreamField,
                                    labelColor     = GroundedColors.BarkMid
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        if (chips.size > 3) {
                            Text("+${chips.size - 3}", fontSize = 10.sp, color = GroundedColors.TextMuted)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                GroundedDivider(Modifier.padding(vertical = 4.dp))
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    Arrangement.End, Alignment.CenterVertically
                ) {
                    Text("View Details", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                        color = GroundedColors.ClayWarm, letterSpacing = 1.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, null, Modifier.size(14.dp),
                        tint = GroundedColors.ClayWarm)
                }
            }
        }
    }
}
