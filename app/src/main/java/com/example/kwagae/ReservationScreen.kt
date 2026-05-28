package com.example.kwagae

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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

                    // Focus chain: Name → Card Number → Expiry → CVV → Pay
                    val cardNumberFocus = remember { FocusRequester() }
                    val expiryFocus     = remember { FocusRequester() }
                    val cvvFocus        = remember { FocusRequester() }
                    val keyboard        = LocalSoftwareKeyboardController.current

                    // Cardholder name
                    PaymentField(
                        label          = "Cardholder Name",
                        value          = state.cardHolder,
                        onValueChange  = viewModel::onCardHolderChange,
                        placeholder    = "e.g. Kabo Mosetlhi",
                        error          = state.cardHolderError,
                        keyboardType   = KeyboardType.Text,
                        imeAction      = ImeAction.Next,
                        onImeAction    = { cardNumberFocus.requestFocus() }
                    )

                    Spacer(Modifier.height(12.dp))

                    // Card number
                    PaymentField(
                        label          = "Card Number",
                        value          = state.cardNumber,
                        onValueChange  = viewModel::onCardNumberChange,
                        placeholder    = "0000 0000 0000 0000",
                        error          = state.cardNumberError,
                        keyboardType   = KeyboardType.Number,
                        imeAction      = ImeAction.Next,
                        onImeAction    = { expiryFocus.requestFocus() },
                        focusRequester = cardNumberFocus
                    )

                    Spacer(Modifier.height(12.dp))

                    // Expiry + CVV row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            PaymentField(
                                label                = "Expiry (MM/YY)",
                                value                = state.expiry,
                                onValueChange        = viewModel::onExpiryChange,
                                placeholder          = "MM/YY",
                                error                = state.expiryError,
                                keyboardType         = KeyboardType.Number,
                                visualTransformation = ExpiryVisualTransformation(),
                                imeAction            = ImeAction.Next,
                                onImeAction          = { cvvFocus.requestFocus() },
                                focusRequester       = expiryFocus
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            PaymentField(
                                label          = "CVV",
                                value          = state.cvv,
                                onValueChange  = viewModel::onCvvChange,
                                placeholder    = "123",
                                error          = state.cvvError,
                                keyboardType   = KeyboardType.Number,
                                imeAction      = ImeAction.Done,
                                onImeAction    = {
                                    keyboard?.hide()
                                    viewModel.pay()
                                },
                                focusRequester = cvvFocus
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

/**
 * Displays raw expiry digits (e.g. "1027") as "MM/YY" (e.g. "10/27").
 * The slash is purely visual — it is never stored in state, which avoids the
 * cursor-position bug where subsequent digits appeared in the wrong order.
 */
private class ExpiryVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val out = buildString {
            text.forEachIndexed { i, ch ->
                append(ch)
                if (i == 1 && text.length > 2) append('/')
            }
        }
        val offsetMapping = object : OffsetMapping {
            // original pos → transformed pos  (insert 1 extra char after position 2)
            override fun originalToTransformed(offset: Int) =
                if (offset <= 2) offset else offset + 1
            // transformed pos → original pos
            override fun transformedToOriginal(offset: Int) =
                if (offset <= 2) offset else (offset - 1).coerceAtLeast(0)
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

@Composable
private fun PaymentField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction                      = ImeAction.Next,
    onImeAction: () -> Unit                   = {},
    focusRequester: FocusRequester?           = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value                = value,
        onValueChange        = onValueChange,
        label                = { Text(label, fontSize = 12.sp) },
        placeholder          = { Text(placeholder, fontSize = 13.sp, color = GroundedColors.TextHint) },
        isError              = error.isNotEmpty(),
        supportingText       = if (error.isNotEmpty()) ({ Text(error, fontSize = 11.sp, color = MaterialTheme.colorScheme.error) }) else null,
        singleLine           = true,
        keyboardOptions      = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction    = imeAction
        ),
        keyboardActions      = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        visualTransformation = visualTransformation,
        modifier             = Modifier
            .fillMaxWidth()
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
        shape                = RoundedCornerShape(10.dp),
        colors               = OutlinedTextFieldDefaults.colors(
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
