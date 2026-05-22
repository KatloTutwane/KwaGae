package com.example.kwagae

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kwagae.ui.theme.GroundedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(navController: NavController, listingId: Long) {
    val viewModel: ReservationViewModel = viewModel(
        factory = ReservationViewModel.factory(listingId)
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigateToReceipt.collect { ref ->
            navController.navigate("receipt/$listingId/$ref") {
                popUpTo("listing_detail/$listingId") { inclusive = false }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PAY DEPOSIT",
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
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GroundedColors.ClayWarm)
            }
            return@Scaffold
        }

        val listing = state.listing
        if (listing == null) {
            Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Listing not found", color = GroundedColors.TextMuted)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GroundedColors.backgroundGradient)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Booking summary ───────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "BOOKING SUMMARY",
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        color         = GroundedColors.ClayWarm
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(listing.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = GroundedColors.TextPrimary)
                    Text(listing.location, fontSize = 13.sp, color = GroundedColors.TextSecondary, modifier = Modifier.padding(top = 2.dp))

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = GroundedColors.BorderDefault)
                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Monthly Rent", fontSize = 13.sp, color = GroundedColors.TextSecondary)
                        Text("BWP ${"%,.0f".format(listing.price)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = GroundedColors.TextPrimary)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Deposit Due Now", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GroundedColors.BarkMid)
                        Text(
                            "BWP ${"%,.0f".format(listing.depositAmount)}",
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color      = GroundedColors.ClayWarm
                        )
                    }
                }
            }

            // ── Card details form ─────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, null, modifier = Modifier.size(18.dp), tint = GroundedColors.ClayWarm)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "CARD DETAILS",
                            fontSize      = 10.sp,
                            fontWeight    = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color         = GroundedColors.ClayWarm
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    // Cardholder name
                    PaymentField(
                        label         = "Cardholder Name",
                        value         = state.cardHolder,
                        onValueChange = viewModel::onCardHolderChange,
                        placeholder   = "e.g. Kabo Mosetlhi",
                        error         = state.cardHolderError,
                        keyboardType  = KeyboardType.Text
                    )

                    Spacer(Modifier.height(12.dp))

                    // Card number
                    PaymentField(
                        label         = "Card Number",
                        value         = state.cardNumber,
                        onValueChange = viewModel::onCardNumberChange,
                        placeholder   = "0000 0000 0000 0000",
                        error         = state.cardNumberError,
                        keyboardType  = KeyboardType.Number
                    )

                    Spacer(Modifier.height(12.dp))

                    // Expiry + CVV row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            PaymentField(
                                label         = "Expiry (MM/YY)",
                                value         = state.expiry,
                                onValueChange = viewModel::onExpiryChange,
                                placeholder   = "MM/YY",
                                error         = state.expiryError,
                                keyboardType  = KeyboardType.Number
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            PaymentField(
                                label         = "CVV",
                                value         = state.cvv,
                                onValueChange = viewModel::onCvvChange,
                                placeholder   = "123",
                                error         = state.cvvError,
                                keyboardType  = KeyboardType.Number
                            )
                        }
                    }
                }
            }

            // ── Simulated payment notice ───────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = GroundedColors.AccentMoss.copy(alpha = 0.10f)
                )
            ) {
                Row(
                    modifier          = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp), tint = GroundedColors.AccentMoss)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "This is a simulated payment for demo purposes. No real transaction will occur.",
                        fontSize   = 11.sp,
                        color      = GroundedColors.AccentMoss,
                        lineHeight = 16.sp
                    )
                }
            }

            if (state.errorMessage.isNotEmpty()) {
                Text(state.errorMessage, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
            }

            // ── Pay button ────────────────────────────────────────────────────
            Button(
                onClick  = viewModel::pay,
                enabled  = !state.isPaying,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = GroundedColors.ClayWarm)
            ) {
                if (state.isPaying) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = Color(0xFFF5E8CC),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Payment, null, modifier = Modifier.size(20.dp), tint = Color(0xFFF5E8CC))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "PAY BWP ${"%,.0f".format(listing.depositAmount)} DEPOSIT",
                        fontSize      = 14.sp,
                        fontWeight    = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color         = Color(0xFFF5E8CC)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PaymentField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, fontSize = 12.sp) },
        placeholder   = { Text(placeholder, fontSize = 13.sp, color = GroundedColors.TextHint) },
        isError       = error.isNotEmpty(),
        supportingText = if (error.isNotEmpty()) ({ Text(error, fontSize = 11.sp, color = MaterialTheme.colorScheme.error) }) else null,
        singleLine    = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(10.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = GroundedColors.CreamField,
            focusedContainerColor   = GroundedColors.CreamFocus,
            unfocusedBorderColor    = if (error.isNotEmpty()) MaterialTheme.colorScheme.error else GroundedColors.BorderDefault,
            focusedBorderColor      = GroundedColors.BorderFocus,
            cursorColor             = GroundedColors.ClayWarm,
            focusedTextColor        = GroundedColors.TextPrimary,
            unfocusedTextColor      = GroundedColors.TextPrimary
        )
    )
}
