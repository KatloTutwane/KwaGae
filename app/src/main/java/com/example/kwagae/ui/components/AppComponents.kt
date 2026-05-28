package com.example.kwagae.ui.components

/**
 * AppComponents.kt — the single source of truth for every reusable UI atom.
 *
 * When you create a new screen, just do:
 *
 *   import com.example.kwagae.ui.components.*
 *
 * Then start with:
 *
 *   AppBackground {
 *       TopColorStripe()
 *       // your content here
 *   }
 *
 * All colours/brushes live in GroundedColors; all shared widgets live here.
 * Never re-declare backgroundGradient, topStripeGradient, LeafShape etc. in
 * individual screen files again.
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kwagae.ui.theme.GroundedColors

// ═══════════════════════════════════════════════════════════════════════════
//  LAYOUT  ──  background, leaf shapes, top stripe
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Wrap any new screen in [AppBackground] and you instantly get:
 *  • the standard earth-tone gradient
 *  • two decorative leaf shapes (pass showLeaves = false to hide them)
 *
 * Example:
 *   AppBackground {
 *       TopColorStripe()
 *       LazyColumn { … }
 *   }
 */
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    showLeaves: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GroundedColors.backgroundGradient)
    ) {
        if (showLeaves) {
            LeafShape(
                Modifier
                    .size(260.dp)
                    .offset(x = (-80).dp, y = (-50).dp)
            )
            LeafShape(
                Modifier
                    .size(180.dp)
                    .offset(x = 240.dp, y = 600.dp)
            )
        }
        content()
    }
}

/**
 * The 3 dp moss → clay → bark stripe that sits at the very top of every
 * screen and card.  Drop it as the first item inside any Column or LazyColumn.
 */
@Composable
fun TopColorStripe(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(GroundedColors.topStripeGradient)
    )
}

/**
 * Soft leaf-shaped blob used as a decorative background element.
 * Supply an explicit Modifier with .size() and .offset() to place it.
 */
@Composable
fun LeafShape(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStartPercent = 50, bottomEndPercent = 50))
            .background(GroundedColors.LeafOverlay)
    )
}

// ═══════════════════════════════════════════════════════════════════════════
//  CARDS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * The cream-coloured rounded card used on login / register / any auth screen.
 * Automatically applies a warm shadow and optional top colour stripe.
 */
@Composable
fun GroundedCard(
    modifier: Modifier = Modifier,
    showTopStripe: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        border = BorderStroke(1.dp, GroundedColors.BorderDefault.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showTopStripe) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                        .background(GroundedColors.topStripeGradient)
                )
            }
            content()
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  DIVIDERS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Horizontal rule with a centred text label — used for "SIGN IN", "OR", etc.
 */
@Composable
fun EarthDivider(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = GroundedColors.BorderDefault)
        Text(
            text          = "  $label  ",
            fontSize      = 10.sp,
            color         = GroundedColors.TextMuted,
            letterSpacing = 1.5.sp,
            fontWeight    = FontWeight.Normal
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = GroundedColors.BorderDefault)
    }
}

/**
 * Thin 1 dp horizontal rule in the app's border colour.
 * Use inside cards or between list items.
 */
@Composable
fun GroundedDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GroundedColors.BorderDefault)
    )
}

// ═══════════════════════════════════════════════════════════════════════════
//  INPUTS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Styled outlined text field with a small caps label above and an optional
 * leading icon.  Used on the Login screen and any new screen that needs a
 * standalone input.
 *
 * For password fields pass isPassword = true and wire passwordVisible /
 * onTogglePassword.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroundedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    focusRequester: FocusRequester? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text          = label,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Medium,
            color         = GroundedColors.ClayWarm,
            letterSpacing = 1.5.sp,
            modifier      = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(text = placeholder, color = GroundedColors.TextHint, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector        = leadingIcon,
                    contentDescription = null,
                    tint               = GroundedColors.TextMuted,
                    modifier           = Modifier.size(18.dp)
                )
            },
            trailingIcon = if (isPassword) ({
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        imageVector        = if (passwordVisible)
                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility",
                        tint               = GroundedColors.TextMuted,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }) else null,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            modifier   = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
            shape      = RoundedCornerShape(10.dp),
            enabled    = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
                imeAction    = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() }
            ),
            textStyle  = LocalTextStyle.current.copy(
                fontSize = 14.sp,
                color    = GroundedColors.TextPrimary
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor    = GroundedColors.CreamField,
                focusedContainerColor      = GroundedColors.CreamFocus,
                disabledContainerColor     = GroundedColors.CreamField,
                unfocusedBorderColor       = GroundedColors.BorderDefault,
                focusedBorderColor         = GroundedColors.BorderFocus,
                cursorColor                = GroundedColors.ClayWarm,
                unfocusedLeadingIconColor  = GroundedColors.TextMuted,
                focusedLeadingIconColor    = GroundedColors.ClayWarm,
                focusedTextColor           = GroundedColors.TextPrimary,
                unfocusedTextColor         = GroundedColors.TextPrimary,
                disabledTextColor          = GroundedColors.TextSecondary
            )
        )
    }
}

/**
 * Simple labelled text field — used on the Register screen for name / email.
 * Pass [icon] as the leading icon.
 */
@Composable
fun GroundedField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    focusRequester: FocusRequester? = null
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        label         = { Text(label, fontSize = 11.sp, letterSpacing = 1.sp) },
        leadingIcon   = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier      = modifier
            .fillMaxWidth()
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
        shape         = RoundedCornerShape(10.dp),
        singleLine    = true,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        colors        = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor  = GroundedColors.BorderDefault,
            focusedBorderColor    = GroundedColors.BorderFocus,
            cursorColor           = GroundedColors.ClayWarm,
            focusedTextColor      = GroundedColors.TextPrimary,
            unfocusedTextColor    = GroundedColors.TextPrimary
        )
    )
    Spacer(Modifier.height(4.dp))
}

/**
 * Password field variant of [GroundedField] — includes a show/hide toggle.
 */
@Composable
fun GroundedPasswordField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    visible: Boolean,
    toggle: () -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    focusRequester: FocusRequester? = null
) {
    OutlinedTextField(
        value               = value,
        onValueChange       = onChange,
        label               = { Text(label, fontSize = 11.sp, letterSpacing = 1.sp) },
        leadingIcon         = {
            Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
        },
        trailingIcon        = {
            IconButton(onClick = toggle) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle password"
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None
                               else PasswordVisualTransformation(),
        modifier   = modifier
            .fillMaxWidth()
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
        shape      = RoundedCornerShape(10.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction    = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        colors     = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = GroundedColors.BorderDefault,
            focusedBorderColor   = GroundedColors.BorderFocus,
            cursorColor          = GroundedColors.ClayWarm,
            focusedTextColor     = GroundedColors.TextPrimary,
            unfocusedTextColor   = GroundedColors.TextPrimary
        )
    )
    Spacer(Modifier.height(4.dp))
}

/** Red validation error message shown below an input field. */
@Composable
fun FieldErrorText(msg: String) {
    if (msg.isNotEmpty()) {
        Text(
            text     = msg,
            color    = Color(0xFFA32D2D),
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
    }
}

/**
 * Search bar used on the Listings and Filter screens.
 * Clears itself when the trailing X is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroundedSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        placeholder   = {
            Text(text = placeholder, color = GroundedColors.TextHint, fontSize = 13.sp)
        },
        leadingIcon = {
            Icon(
                imageVector        = Icons.Default.Search,
                contentDescription = "Search",
                tint               = GroundedColors.TextMuted,
                modifier           = Modifier.size(18.dp)
            )
        },
        trailingIcon = if (value.isNotEmpty()) ({
            IconButton(onClick = { onValueChange("") }) {
                Icon(
                    imageVector        = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint               = GroundedColors.TextMuted,
                    modifier           = Modifier.size(16.dp)
                )
            }
        }) else null,
        modifier   = modifier.fillMaxWidth(),
        shape      = RoundedCornerShape(10.dp),
        singleLine = true,
        textStyle  = LocalTextStyle.current.copy(
            fontSize = 14.sp,
            color    = GroundedColors.TextPrimary
        ),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor   = GroundedColors.CreamField,
            focusedContainerColor     = GroundedColors.CreamFocus,
            unfocusedBorderColor      = GroundedColors.BorderDefault,
            focusedBorderColor        = GroundedColors.BorderFocus,
            cursorColor               = GroundedColors.ClayWarm,
            unfocusedLeadingIconColor = GroundedColors.TextMuted,
            focusedLeadingIconColor   = GroundedColors.ClayWarm
        )
    )
}

// ═══════════════════════════════════════════════════════════════════════════
//  BUTTONS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Gradient-filled primary action button (Login, Register, etc.).
 * Shows a spinner + [loadingText] while [isLoading] is true.
 */
@Composable
fun GroundedPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    loadingText: String = "LOADING..."
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isLoading) GroundedColors.buttonGradientLoading
                else GroundedColors.buttonGradient
            )
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    color       = Color(0xFFF5E8CC),
                    strokeWidth = 2.dp
                )
                Text(
                    text          = loadingText,
                    color         = Color(0xFFF5E8CC),
                    fontSize      = 13.sp,
                    fontWeight    = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
            }
        } else {
            Text(
                text          = text,
                color         = Color(0xFFF5E8CC),
                fontSize      = 13.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }
    }
}

/**
 * Outlined social / secondary button (Google, Apple, etc.).
 */
@Composable
fun GroundedSocialButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier.height(46.dp),
        shape    = RoundedCornerShape(10.dp),
        border   = BorderStroke(1.5.dp, GroundedColors.BorderDefault),
        colors   = ButtonDefaults.outlinedButtonColors(
            containerColor = GroundedColors.CreamField,
            contentColor   = GroundedColors.EspressoDeep
        )
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier.size(16.dp),
            tint               = GroundedColors.BarkMid
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text       = text,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium,
            color      = GroundedColors.EspressoDeep
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  MISC ATOMS
// ═══════════════════════════════════════════════════════════════════════════

/** "Secure · Student Verified" footer row shown on the login card. */
@Composable
fun TrustBadge(modifier: Modifier = Modifier) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        TrustDot()
        Text("  Secure  ",          fontSize = 10.sp, color = GroundedColors.TextMuted, letterSpacing = 0.5.sp)
        TrustDot()
        Text("  Student Verified  ", fontSize = 10.sp, color = GroundedColors.TextMuted, letterSpacing = 0.5.sp)
        TrustDot()
    }
}

@Composable
fun TrustDot() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(GroundedColors.ClayWarm)
    )
}

/**
 * Empty-state card with centred text.
 * Use when a list is empty — e.g. "No listings available".
 */
@Composable
fun GroundedEmptyCard(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        border   = BorderStroke(1.dp, GroundedColors.BorderDefault)
    ) {
        Text(
            text      = text,
            modifier  = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize  = 13.sp,
            color     = GroundedColors.TextMuted
        )
    }
}

/**
 * Small-caps section header with an optional "See all / More" action link.
 *
 * Usage:
 *   GroundedSectionHeader("RECENT LISTINGS", actionText = "See all") { … }
 */
@Composable
fun GroundedSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text          = title,
            fontSize      = 12.sp,
            fontWeight    = FontWeight.Medium,
            color         = GroundedColors.ClayWarm,
            letterSpacing = 2.sp
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text       = actionText,
                    fontSize   = 11.sp,
                    color      = GroundedColors.BarkMid,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
