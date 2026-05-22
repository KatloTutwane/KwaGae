package com.example.kwagae

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kwagae.ui.theme.GroundedColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReceiptScreen(navController: NavController, listingId: Long, refNumber: String) {
    val detailViewModel: ListingDetailViewModel = viewModel(
        factory = ListingDetailViewModel.factory(listingId)
    )
    val state by detailViewModel.uiState.collectAsState()
    val listing = state.listing

    val dateStr = remember {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GroundedColors.backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Success icon ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                    .border(2.dp, Color(0xFF4CAF50).copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint     = Color(0xFF4CAF50)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "RESERVATION CONFIRMED",
                fontSize      = 13.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.sp,
                color         = Color(0xFF4CAF50)
            )
            Text(
                "Your deposit payment was successful",
                fontSize  = 13.sp,
                color     = GroundedColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // ── Receipt card ──────────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Reference number
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .background(GroundedColors.ClayWarm.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .border(1.dp, GroundedColors.ClayWarm.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "REFERENCE NUMBER",
                            fontSize      = 9.sp,
                            fontWeight    = FontWeight.Medium,
                            letterSpacing = 2.sp,
                            color         = GroundedColors.TextMuted
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            refNumber,
                            fontSize   = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = GroundedColors.ClayWarm,
                            letterSpacing = 3.sp
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = GroundedColors.BorderDefault)
                    Spacer(Modifier.height(16.dp))

                    if (listing != null) {
                        ReceiptRow(Icons.Default.Home,       "Property",  listing.title)
                        ReceiptRow(Icons.Default.LocationOn, "Location",  listing.location)
                        ReceiptRow(
                            Icons.Default.AttachMoney,
                            "Amount Paid",
                            "BWP ${"%,.0f".format(listing.depositAmount)}"
                        )
                    }
                    ReceiptRow(Icons.Default.CalendarToday, "Date",       dateStr)
                    ReceiptRow(Icons.Default.CheckCircle,   "Status",     "Reserved")

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = GroundedColors.BorderDefault)
                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Keep this reference number safe. The landlord will use it to confirm your reservation.",
                        fontSize   = 11.sp,
                        color      = GroundedColors.TextMuted,
                        textAlign  = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier   = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Done button ───────────────────────────────────────────────────
            Button(
                onClick  = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = false }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = GroundedColors.ClayWarm)
            ) {
                Icon(Icons.Default.Home, null, modifier = Modifier.size(18.dp), tint = Color(0xFFF5E8CC))
                Spacer(Modifier.width(10.dp))
                Text(
                    "BACK TO HOME",
                    fontSize      = 14.sp,
                    fontWeight    = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color         = Color(0xFFF5E8CC)
                )
            }
        }
    }
}

@Composable
private fun ReceiptRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(15.dp), tint = GroundedColors.BarkMid)
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 12.sp, color = GroundedColors.TextMuted, modifier = Modifier.width(90.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = GroundedColors.TextPrimary)
    }
}
