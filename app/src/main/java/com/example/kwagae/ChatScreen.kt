package com.example.kwagae

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kwagae.data.models.Message
import com.example.kwagae.ui.theme.GroundedColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController) {
    val viewModel: ChatViewModel = viewModel()
    val state     by viewModel.uiState.collectAsState()
    val threadId  = viewModel.threadId
    val listState = rememberLazyListState()

    // Guard: if no thread context was set, go back immediately
    if (threadId.isBlank()) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    // Scroll to bottom whenever the message count changes
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GroundedColors.backgroundGradient)
    ) {

        // ── Top bar ───────────────────────────────────────────────────────────
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
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (navController.previousBackStackEntry != null) navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint               = Color(0xFFF5E8CC)
                        )
                    }

                    // Avatar circle
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GroundedColors.ClayWarm)
                    ) {
                        Text(
                            text       = state.otherName.firstOrNull()?.uppercase() ?: "L",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFFF5E8CC)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = state.otherName,
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color(0xFFF5E8CC)
                        )
                        if (viewModel.listingTitle.isNotEmpty()) {
                            Text(
                                text     = viewModel.listingTitle,
                                fontSize = 11.sp,
                                color    = Color(0xFFF5E8CC).copy(alpha = 0.75f),
                                maxLines = 1
                            )
                        }
                    }

                    // Online indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GroundedColors.AccentMoss)
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }
        }

        // ── Message list ──────────────────────────────────────────────────────
        LazyColumn(
            state             = listState,
            modifier          = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding    = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (state.messages.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint     = Color(0xFFF5E8CC).copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text     = "No messages yet",
                                fontSize = 14.sp,
                                color    = Color(0xFFF5E8CC).copy(alpha = 0.6f)
                            )
                            Text(
                                text     = "Send a message to get started",
                                fontSize = 12.sp,
                                color    = Color(0xFFF5E8CC).copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            items(state.messages, key = { it.id }) { message ->
                val isMine = message.senderId == viewModel.currentUserId
                MessageBubble(message = message, isMine = isMine)
            }
        }

        // ── Input bar ─────────────────────────────────────────────────────────
        Surface(
            modifier  = Modifier.fillMaxWidth(),
            color     = GroundedColors.CreamCard,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value         = state.inputText,
                    onValueChange = viewModel::onInputChange,
                    placeholder   = {
                        Text(
                            "Type a message…",
                            color    = GroundedColors.TextHint,
                            fontSize = 14.sp
                        )
                    },
                    modifier   = Modifier
                        .weight(1f)
                        .padding(end = 10.dp),
                    shape      = RoundedCornerShape(24.dp),
                    singleLine = false,
                    maxLines   = 4,
                    colors     = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = GroundedColors.CreamField,
                        focusedContainerColor   = GroundedColors.CreamFocus,
                        unfocusedBorderColor    = GroundedColors.BorderDefault,
                        focusedBorderColor      = GroundedColors.BorderFocus,
                        cursorColor             = GroundedColors.ClayWarm,
                        focusedTextColor        = GroundedColors.TextPrimary,
                        unfocusedTextColor      = GroundedColors.TextPrimary
                    )
                )

                // Send button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (state.inputText.isNotBlank()) GroundedColors.buttonGradient
                            else GroundedColors.buttonGradientLoading
                        )
                ) {
                    IconButton(
                        onClick  = viewModel::sendMessage,
                        enabled  = state.inputText.isNotBlank() && !state.isSending,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (state.isSending) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                color       = Color(0xFFF5E8CC),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint               = Color(0xFFF5E8CC),
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Message bubble ────────────────────────────────────────────────────────────

@Composable
private fun MessageBubble(message: Message, isMine: Boolean) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString    = message.sentAt?.let { timeFormatter.format(it) } ?: ""

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        if (!isMine) {
            // Avatar for received messages
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(GroundedColors.ClayWarm.copy(alpha = 0.3f))
                    .align(Alignment.Bottom)
            ) {
                Text(
                    text       = message.senderName.firstOrNull()?.uppercase() ?: "L",
                    fontSize   = 12.sp,
                    color      = GroundedColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier            = Modifier.widthIn(max = 280.dp)
        ) {
            if (!isMine) {
                Text(
                    text     = message.senderName,
                    fontSize = 10.sp,
                    color    = GroundedColors.TextMuted,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            val bubbleShape = if (isMine) {
                RoundedCornerShape(
                    topStart    = 16.dp, topEnd     = 16.dp,
                    bottomStart = 16.dp, bottomEnd  = 4.dp
                )
            } else {
                RoundedCornerShape(
                    topStart    = 4.dp,  topEnd     = 16.dp,
                    bottomStart = 16.dp, bottomEnd  = 16.dp
                )
            }

            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(
                        if (isMine) GroundedColors.BarkMid
                        else GroundedColors.CreamCard
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text       = message.text,
                    fontSize   = 14.sp,
                    color      = if (isMine) Color(0xFFF5E8CC) else GroundedColors.TextPrimary,
                    lineHeight = 20.sp
                )
            }

            if (timeString.isNotEmpty()) {
                Text(
                    text     = timeString,
                    fontSize = 9.sp,
                    color    = GroundedColors.TextHint,
                    modifier = Modifier.padding(
                        top   = 2.dp,
                        start = if (isMine) 0.dp else 4.dp,
                        end   = if (isMine) 4.dp else 0.dp
                    )
                )
            }
        }
    }
}
