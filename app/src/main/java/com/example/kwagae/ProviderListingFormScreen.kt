@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.kwagae

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kwagae.ui.theme.GroundedColors
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction

// ── Provider Listing Form ─────────────────────────────────────────────────────

@Composable
fun ProviderListingFormScreen(
    navController: NavController,
    listingId: Long = 0L
) {
    val viewModel: ProviderListingFormViewModel = viewModel(
        factory = ProviderListingFormViewModel.factory(listingId)
    )
    val state   by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Image picker — uses Storage Access Framework (no permission needed)
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some providers don't support persistable permissions — URI still works in session
            }
        }
        viewModel.addImages(uris.map { it.toString() })
    }

    // Navigate back after successful save
    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(GroundedColors.topStripeGradient)
                )
                TopAppBar(
                    title = {
                        Text(
                            text       = if (listingId == 0L) "New Listing" else "Edit Listing",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier    = Modifier
                                    .padding(end = 16.dp)
                                    .size(20.dp),
                                strokeWidth = 2.5.dp,
                                color       = Color(0xFFF5E8CC)
                            )
                        } else {
                            TextButton(onClick = viewModel::save) {
                                Text(
                                    text       = "SAVE",
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color      = Color(0xFFF5E8CC)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor          = GroundedColors.BarkMid,
                        titleContentColor       = Color(0xFFF5E8CC),
                        navigationIconContentColor = Color(0xFFF5E8CC),
                        actionIconContentColor  = Color(0xFFF5E8CC)
                    )
                )
            }
        }
    ) { scaffoldPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier       = Modifier
                    .fillMaxSize()
                    .background(GroundedColors.backgroundGradient)
                    .padding(scaffoldPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Images ────────────────────────────────────────────────────
                item {
                    FormSectionCard(title = "PHOTOS", icon = Icons.Default.PhotoCamera) {
                        ImagePickerSection(
                            imageUris = state.imageUris,
                            onAdd     = { imagePicker.launch("image/*") },
                            onRemove  = viewModel::removeImage
                        )
                    }
                }

                // ── Basic Info ────────────────────────────────────────────────
                item {
                    FormSectionCard(title = "BASIC INFORMATION", icon = Icons.Default.Info) {
                        BasicInfoSection(state = state, viewModel = viewModel)
                    }
                }

                // ── Availability ──────────────────────────────────────────────
                item {
                    FormSectionCard(title = "AVAILABILITY", icon = Icons.Default.CalendarToday) {
                        AvailabilitySection(state = state, viewModel = viewModel)
                    }
                }

                // ── Included Utilities ────────────────────────────────────────
                item {
                    FormSectionCard(title = "INCLUDED UTILITIES", icon = Icons.Default.ElectricBolt) {
                        UtilitiesSection(state = state, viewModel = viewModel)
                    }
                }

                // ── Property Features ─────────────────────────────────────────
                item {
                    FormSectionCard(title = "PROPERTY FEATURES", icon = Icons.Default.Apartment) {
                        FeaturesSection(state = state, viewModel = viewModel)
                    }
                }

                // ── Room Details ──────────────────────────────────────────────
                item {
                    FormSectionCard(title = "ROOM DETAILS", icon = Icons.Default.Bed) {
                        RoomDetailsSection(state = state, viewModel = viewModel)
                    }
                }

                // ── Additional Details ────────────────────────────────────────
                item {
                    FormSectionCard(title = "ADDITIONAL DETAILS", icon = Icons.Default.School) {
                        AdditionalDetailsSection(state = state, viewModel = viewModel)
                    }
                }

                // Error message (if any)
                if (state.errorMessage.isNotEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFD32F2F).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(state.errorMessage, fontSize = 12.sp, color = Color(0xFFD32F2F))
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(60.dp)) }
            }

            // Loading overlay
            if (state.isSaving) {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard)
                    ) {
                        Column(
                            modifier            = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = GroundedColors.ClayWarm)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text       = "Saving listing…",
                                fontSize   = 14.sp,
                                color      = GroundedColors.TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Section wrapper card ──────────────────────────────────────────────────────

@Composable
private fun FormSectionCard(
    title:   String,
    icon:    ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Section header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon, null,
                    modifier = Modifier.size(15.dp),
                    tint     = GroundedColors.ClayWarm
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text          = title,
                    fontSize      = 10.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = GroundedColors.ClayWarm
                )
            }
            HorizontalDivider(
                modifier  = Modifier.padding(vertical = 10.dp),
                color     = GroundedColors.BorderDefault,
                thickness = 0.5.dp
            )
            content()
        }
    }
}

// ── Image picker section ──────────────────────────────────────────────────────

@Composable
private fun ImagePickerSection(
    imageUris: List<String>,
    onAdd:     () -> Unit,
    onRemove:  (Int) -> Unit
) {
    if (imageUris.isEmpty()) {
        // Placeholder / prompt
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clickable(onClick = onAdd),
            shape  = RoundedCornerShape(12.dp),
            color  = GroundedColors.CreamField,
            border = BorderStroke(1.5.dp, GroundedColors.BorderDefault)
        ) {
            Column(
                modifier            = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.AddPhotoAlternate, null,
                    modifier = Modifier.size(36.dp),
                    tint     = GroundedColors.ClayWarm
                )
                Spacer(Modifier.height(8.dp))
                Text("Tap to add photos", fontSize = 13.sp, color = GroundedColors.TextSecondary)
                Text("JPG, PNG supported", fontSize = 10.sp, color = GroundedColors.TextMuted)
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding        = PaddingValues(end = 10.dp)
        ) {
            itemsIndexed(imageUris) { index, uri ->
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model              = uri,
                        contentDescription = "Photo ${index + 1}",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                    // Remove button
                    IconButton(
                        onClick  = { onRemove(index) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            Icons.Default.Close, null,
                            modifier = Modifier.size(14.dp),
                            tint     = Color.White
                        )
                    }
                    // Primary badge for first image
                    if (index == 0) {
                        Surface(
                            shape    = RoundedCornerShape(topStart = 12.dp, bottomEnd = 8.dp),
                            color    = GroundedColors.ClayWarm.copy(alpha = 0.85f),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text     = "COVER",
                                fontSize = 7.sp,
                                color    = Color(0xFFF5E8CC),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // "Add more" button at end
            item {
                Surface(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onAdd),
                    color  = GroundedColors.CreamField,
                    border = BorderStroke(1.dp, GroundedColors.BorderDefault)
                ) {
                    Column(
                        modifier            = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = GroundedColors.ClayWarm, modifier = Modifier.size(24.dp))
                        Text("Add", fontSize = 10.sp, color = GroundedColors.TextSecondary)
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text     = "${imageUris.size} photo${if (imageUris.size != 1) "s" else ""} selected · First image is the cover",
            fontSize = 10.sp,
            color    = GroundedColors.TextMuted
        )
    }
}

// ── Basic Info section ────────────────────────────────────────────────────────

@Composable
private fun BasicInfoSection(state: ListingFormState, viewModel: ProviderListingFormViewModel) {
    val locationFocus = remember { FocusRequester() }
    val contactFocus  = remember { FocusRequester() }
    val priceFocus    = remember { FocusRequester() }
    val depositFocus  = remember { FocusRequester() }
    val keyboard      = LocalSoftwareKeyboardController.current

    // Title
    FormTextField(
        label          = "LISTING TITLE *",
        value          = state.title,
        onChange       = viewModel::setTitle,
        error          = state.titleError,
        hint           = "e.g. Furnished Studio Near UB Campus",
        leadingIcon    = Icons.Default.Home,
        imeAction      = ImeAction.Next,
        onImeAction    = { locationFocus.requestFocus() }
    )

    Spacer(Modifier.height(10.dp))

    // Type dropdown
    FormDropdown(
        label    = "PROPERTY TYPE",
        value    = state.type,
        options  = listOf("Apartment", "House", "Studio", "Room", "Townhouse", "Bachelor Flat", "Duplex", "Bedsitter"),
        onSelect = viewModel::setType,
        icon     = Icons.Default.Apartment
    )

    Spacer(Modifier.height(10.dp))

    // Description (multi-line — Enter inserts newlines; no imeAction override)
    FormTextField(
        label       = "DESCRIPTION",
        value       = state.description,
        onChange    = viewModel::setDescription,
        hint        = "Describe the property, surroundings, and unique features…",
        singleLine  = false,
        minLines    = 3,
        maxLines    = 6,
        leadingIcon = Icons.Default.Notes
    )

    Spacer(Modifier.height(10.dp))

    // Location
    FormTextField(
        label          = "LOCATION / ADDRESS *",
        value          = state.location,
        onChange       = viewModel::setLocation,
        error          = state.locationError,
        hint           = "e.g. Plot 1234, Gaborone West",
        leadingIcon    = Icons.Default.LocationOn,
        imeAction      = ImeAction.Next,
        onImeAction    = { contactFocus.requestFocus() },
        focusRequester = locationFocus
    )

    Spacer(Modifier.height(10.dp))

    // Contact
    FormTextField(
        label          = "CONTACT NUMBER",
        value          = state.contactInfo,
        onChange       = viewModel::setContactInfo,
        hint           = "WhatsApp / Phone e.g. +267 71234567",
        keyboardType   = KeyboardType.Phone,
        leadingIcon    = Icons.Default.Phone,
        imeAction      = ImeAction.Next,
        onImeAction    = { priceFocus.requestFocus() },
        focusRequester = contactFocus
    )

    Spacer(Modifier.height(10.dp))

    // Price + Deposit row
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            FormTextField(
                label          = "MONTHLY RENT (BWP) *",
                value          = state.price,
                onChange       = viewModel::setPrice,
                error          = state.priceError,
                hint           = "e.g. 3500",
                keyboardType   = KeyboardType.Number,
                leadingIcon    = Icons.Default.AttachMoney,
                imeAction      = ImeAction.Next,
                onImeAction    = { depositFocus.requestFocus() },
                focusRequester = priceFocus
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            FormTextField(
                label          = "DEPOSIT (BWP)",
                value          = state.depositAmount,
                onChange       = viewModel::setDeposit,
                hint           = "e.g. 7000",
                keyboardType   = KeyboardType.Number,
                leadingIcon    = Icons.Default.Lock,
                imeAction      = ImeAction.Done,
                onImeAction    = { keyboard?.hide() },
                focusRequester = depositFocus
            )
        }
    }
}

// ── Availability section ──────────────────────────────────────────────────────

@Composable
private fun AvailabilitySection(state: ListingFormState, viewModel: ProviderListingFormViewModel) {
    val keyboard = LocalSoftwareKeyboardController.current

    FormTextField(
        label       = "AVAILABLE FROM",
        value       = state.availabilityDate,
        onChange    = viewModel::setAvailabilityDate,
        hint        = "e.g. 01 Jan 2025 or Immediately",
        leadingIcon = Icons.Default.CalendarToday,
        imeAction   = ImeAction.Done,
        onImeAction = { keyboard?.hide() }
    )

    Spacer(Modifier.height(12.dp))

    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Currently Available", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = GroundedColors.TextPrimary)
            Text(
                text     = if (state.isAvailable) "Listing is visible to students" else "Hidden from student searches",
                fontSize = 11.sp,
                color    = GroundedColors.TextMuted
            )
        }
        Switch(
            checked         = state.isAvailable,
            onCheckedChange = viewModel::setIsAvailable,
            colors          = SwitchDefaults.colors(
                checkedTrackColor   = GroundedColors.AccentMoss,
                uncheckedTrackColor = GroundedColors.TextMuted.copy(alpha = 0.35f)
            )
        )
    }
}

// ── Utilities section ─────────────────────────────────────────────────────────

@Composable
private fun UtilitiesSection(state: ListingFormState, viewModel: ProviderListingFormViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        ToggleRow("Wi-Fi Included",           Icons.Default.Wifi,          state.wifiIncluded,          viewModel::setWifi)
        ToggleRow("Water Included",           Icons.Default.Water,         state.waterIncluded,         viewModel::setWater)
        ToggleRow("Electricity Included",     Icons.Default.ElectricBolt,  state.electricityIncluded,   viewModel::setElectricity)
    }
}

// ── Property features section ─────────────────────────────────────────────────

@Composable
private fun FeaturesSection(state: ListingFormState, viewModel: ProviderListingFormViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        ToggleRow("Parking Available",  Icons.Default.LocalParking, state.parkingAvailable,  viewModel::setParking)
        ToggleRow("Security / Guard",   Icons.Default.Security,     state.securityAvailable,  viewModel::setSecurity)
        ToggleRow("Furnished",          Icons.Default.Chair,        state.isFurnished,        viewModel::setFurnished)
        ToggleRow("Kitchen Available",  Icons.Default.Kitchen,      state.kitchenAvailable,   viewModel::setKitchen)
    }
}

// ── Room details section ──────────────────────────────────────────────────────

@Composable
private fun RoomDetailsSection(state: ListingFormState, viewModel: ProviderListingFormViewModel) {
    // Number of rooms stepper
    FormStepperRow(
        label    = "Number of Rooms",
        value    = state.roomCount,
        onMinus  = { viewModel.setRoomCount(state.roomCount - 1) },
        onPlus   = { viewModel.setRoomCount(state.roomCount + 1) },
        icon     = Icons.Default.Bed
    )

    Spacer(Modifier.height(10.dp))

    // Max occupants
    FormStepperRow(
        label    = "Maximum Occupants",
        value    = state.maxOccupants,
        onMinus  = { viewModel.setMaxOccupants(state.maxOccupants - 1) },
        onPlus   = { viewModel.setMaxOccupants(state.maxOccupants + 1) },
        icon     = Icons.Default.Group
    )

    Spacer(Modifier.height(10.dp))

    // Bathroom type
    FormDropdown(
        label    = "BATHROOM TYPE",
        value    = state.bathroomType,
        options  = listOf("Shared", "En-suite", "Private"),
        onSelect = viewModel::setBathroomType,
        icon     = Icons.Default.Bathtub
    )

    Spacer(Modifier.height(10.dp))

    // Gender preference
    FormDropdown(
        label    = "GENDER PREFERENCE",
        value    = state.genderPreference,
        options  = listOf("Any", "Male only", "Female only"),
        onSelect = viewModel::setGenderPreference,
        icon     = Icons.Default.People
    )
}

// ── Additional details section ────────────────────────────────────────────────

@Composable
private fun AdditionalDetailsSection(state: ListingFormState, viewModel: ProviderListingFormViewModel) {
    FormTextField(
        label       = "NEARBY SCHOOLS / UNIVERSITIES",
        value       = state.nearbySchools,
        onChange    = viewModel::setNearbySchools,
        hint        = "e.g. UB Main Campus, Botho University",
        leadingIcon = Icons.Default.School,
        singleLine  = false,
        maxLines    = 2
    )

    Spacer(Modifier.height(10.dp))

    FormTextField(
        label       = "HOUSE RULES",
        value       = state.rules,
        onChange    = viewModel::setRules,
        hint        = "e.g. No smoking, No pets, Quiet hours after 22:00",
        leadingIcon = Icons.Default.Gavel,
        singleLine  = false,
        minLines    = 2,
        maxLines    = 5
    )
}

// ── Reusable helper composables ───────────────────────────────────────────────

@Composable
private fun FormTextField(
    label:          String,
    value:          String,
    onChange:       (String) -> Unit,
    hint:           String          = "",
    error:          String          = "",
    leadingIcon:    ImageVector?    = null,
    keyboardType:   KeyboardType    = KeyboardType.Text,
    singleLine:     Boolean         = true,
    minLines:       Int             = 1,
    maxLines:       Int             = 1,
    imeAction:      ImeAction       = ImeAction.Next,
    onImeAction:    () -> Unit      = {},
    focusRequester: FocusRequester? = null
) {
    Column {
        Text(
            text          = label,
            fontSize      = 10.sp,
            letterSpacing = 1.sp,
            fontWeight    = FontWeight.Medium,
            color         = GroundedColors.ClayWarm,
            modifier      = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onChange,
            placeholder   = { Text(hint, color = GroundedColors.TextHint, fontSize = 12.sp) },
            leadingIcon   = leadingIcon?.let { icon ->
                { Icon(icon, null, modifier = Modifier.size(18.dp), tint = GroundedColors.TextMuted) }
            },
            isError       = error.isNotEmpty(),
            singleLine    = singleLine,
            minLines      = minLines,
            maxLines      = if (singleLine) 1 else maxLines,
            modifier      = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
            shape         = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction    = if (singleLine) imeAction else ImeAction.Default
            ),
            keyboardActions = if (singleLine) KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() }
            ) else KeyboardActions.Default,
            textStyle     = LocalTextStyle.current.copy(
                fontSize = 13.sp,
                color    = GroundedColors.TextPrimary
            ),
            colors        = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = GroundedColors.CreamField,
                focusedContainerColor   = GroundedColors.CreamFocus,
                unfocusedBorderColor    = if (error.isNotEmpty()) Color(0xFFD32F2F) else GroundedColors.BorderDefault,
                focusedBorderColor      = GroundedColors.BorderFocus,
                cursorColor             = GroundedColors.ClayWarm,
                focusedTextColor        = GroundedColors.TextPrimary,
                unfocusedTextColor      = GroundedColors.TextPrimary,
                errorBorderColor        = Color(0xFFD32F2F),
                errorLeadingIconColor   = Color(0xFFD32F2F)
            )
        )
        if (error.isNotEmpty()) {
            Text(
                text     = error,
                fontSize = 11.sp,
                color    = Color(0xFFD32F2F),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@Composable
private fun FormDropdown(
    label:    String,
    value:    String,
    options:  List<String>,
    onSelect: (String) -> Unit,
    icon:     ImageVector? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text          = label,
            fontSize      = 10.sp,
            letterSpacing = 1.sp,
            fontWeight    = FontWeight.Medium,
            color         = GroundedColors.ClayWarm,
            modifier      = Modifier.padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded         = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value         = value,
                onValueChange = {},
                readOnly      = true,
                leadingIcon   = icon?.let { ic ->
                    { Icon(ic, null, modifier = Modifier.size(18.dp), tint = GroundedColors.TextMuted) }
                },
                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape         = RoundedCornerShape(10.dp),
                textStyle     = LocalTextStyle.current.copy(
                    fontSize = 13.sp,
                    color    = GroundedColors.TextPrimary
                ),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = GroundedColors.CreamField,
                    focusedContainerColor   = GroundedColors.CreamFocus,
                    unfocusedBorderColor    = GroundedColors.BorderDefault,
                    focusedBorderColor      = GroundedColors.BorderFocus,
                    cursorColor             = GroundedColors.ClayWarm,
                    focusedTextColor        = GroundedColors.TextPrimary,
                    unfocusedTextColor      = GroundedColors.TextPrimary
                )
            )
            ExposedDropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text    = { Text(option, fontSize = 13.sp) },
                        onClick = { onSelect(option); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label:   String,
    icon:    ImageVector,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon, null,
                modifier = Modifier.size(18.dp),
                tint     = if (checked) GroundedColors.AccentMoss else GroundedColors.TextMuted
            )
            Spacer(Modifier.width(10.dp))
            Text(
                label,
                fontSize   = 13.sp,
                color      = GroundedColors.TextPrimary,
                fontWeight = if (checked) FontWeight.Medium else FontWeight.Normal
            )
        }
        Switch(
            checked         = checked,
            onCheckedChange = onToggle,
            colors          = SwitchDefaults.colors(
                checkedTrackColor   = GroundedColors.AccentMoss,
                uncheckedTrackColor = GroundedColors.TextMuted.copy(alpha = 0.35f)
            )
        )
    }
}

@Composable
private fun FormStepperRow(
    label:   String,
    value:   Int,
    onMinus: () -> Unit,
    onPlus:  () -> Unit,
    icon:    ImageVector
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = GroundedColors.TextMuted)
            Spacer(Modifier.width(10.dp))
            Text(label, fontSize = 13.sp, color = GroundedColors.TextPrimary)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick  = onMinus,
                enabled  = value > 1,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GroundedColors.CreamField)
            ) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp), tint = GroundedColors.BarkMid)
            }
            Text(
                text      = value.toString(),
                fontSize  = 16.sp,
                fontWeight = FontWeight.Bold,
                color     = GroundedColors.TextPrimary,
                modifier  = Modifier.widthIn(min = 36.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick  = onPlus,
                enabled  = value < 20,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GroundedColors.ClayWarm.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = GroundedColors.ClayWarm)
            }
        }
    }
}
