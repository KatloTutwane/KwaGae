package com.example.kwagae

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kwagae.data.models.ChatThread
import com.example.kwagae.ui.components.*
import com.example.kwagae.ui.theme.GroundedColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConversationsScreen(navController: NavController) {
    val viewModel: ConversationsViewModel = viewModel()
    val state         by viewModel.uiState.collectAsState()
    val currentUserId = viewModel.currentUserId
    val currentRole   = viewModel.currentRole

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
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
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                            tint               = Color(0xFFF5E8CC),
                            modifier           = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text       = "Messages",
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color(0xFFF5E8CC)
                        )
                    }
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GroundedColors.ClayWarm)
                    }
                }

                state.threads.isEmpty() -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint     = Color(0xFFF5E8CC).copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text     = "No conversations yet",
                                fontSize = 16.sp,
                                color    = Color(0xFFF5E8CC).copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text     = "Tap \"Contact Landlord\" on any listing to start chatting",
                                fontSize = 13.sp,
                                color    = Color(0xFFF5E8CC).copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.threads, key = { it.threadId }) { thread ->
                            ThreadCard(
                                thread        = thread,
                                currentUserId = currentUserId,
                                currentRole   = currentRole,
                                onClick       = {
                                    ChatNavArgs.apply {
                                        threadId     = thread.threadId
                                        providerId   = thread.providerId
                                        providerName = thread.providerName
                                        listingId    = thread.listingId
                                        listingTitle = thread.listingTitle
                                    }
                                    navController.navigate("chat")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Thread card ───────────────────────────────────────────────────────────────

@Composable
private fun ThreadCard(
    thread: ChatThread,
    currentUserId: String,
    currentRole: String,
    onClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val otherName = if (currentRole == "provider") thread.studentName else thread.providerName
    val timeLabel = thread.lastMessageAt?.let { date ->
        val now = Calendar.getInstance()
        val msg = Calendar.getInstance().apply { time = date }
        if (now.get(Calendar.DATE) == msg.get(Calendar.DATE)) {
            timeFormatter.format(date)
        } else {
            dateFormatter.format(date)
        }
    } ?: ""

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(GroundedColors.ClayWarm)
            ) {
                Text(
                    text       = otherName.firstOrNull()?.uppercase() ?: "?",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFFF5E8CC)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = otherName,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = GroundedColors.TextPrimary,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                    if (timeLabel.isNotEmpty()) {
                        Text(
                            text     = timeLabel,
                            fontSize = 11.sp,
                            color    = GroundedColors.TextMuted
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text     = thread.listingTitle,
                    fontSize = 11.sp,
                    color    = GroundedColors.ClayWarm,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    text     = thread.lastMessage.ifEmpty { "No messages yet" },
                    fontSize = 12.sp,
                    color    = GroundedColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.ChevronRight, null,
                modifier = Modifier.size(18.dp),
                tint     = GroundedColors.TextHint
            )
        }
    }
}
