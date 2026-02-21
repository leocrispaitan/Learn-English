package com.leopc.speakup.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leopc.speakup.ui.theme.*
import androidx.compose.foundation.layout.WindowInsets

// ─── Entry point ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {

    // ── Persisted states ─────────────────────────────────────────────────────
    var darkModeEnabled   by remember { mutableStateOf(false) }
    var autoPlayEnabled   by remember { mutableStateOf(true) }
    var autoUpdateEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Primary
                        )
                    }
                },
                title = {
                    Text(
                        text = "App Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Spacer(Modifier.height(4.dp))

            // ── GENERAL ──────────────────────────────────────────────────────
            SettingsSection(title = "GENERAL") {
                SettingsChevronRow(
                    icon = Icons.Outlined.Palette,
                    label = "Theme",
                    value = "System"
                )
                SettingsDivider()
                SettingsChevronRow(
                    icon = Icons.Outlined.Translate,
                    label = "Language",
                    value = "English"
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Outlined.DarkMode,
                    label = "Dark Mode",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }

            // ── PLAYBACK ─────────────────────────────────────────────────────
            SettingsSection(title = "PLAYBACK") {
                SettingsToggleRow(
                    icon = Icons.Outlined.PlayCircle,
                    label = "Auto-play next lesson",
                    checked = autoPlayEnabled,
                    onCheckedChange = { autoPlayEnabled = it }
                )
                SettingsDivider()
                SettingsChevronRow(
                    icon = Icons.Outlined.HighQuality,
                    label = "Default Quality",
                    value = "Standard"
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Outlined.Sync,
                    label = "Auto-update content",
                    checked = autoUpdateEnabled,
                    onCheckedChange = { autoUpdateEnabled = it }
                )
            }

            // ── ABOUT ─────────────────────────────────────────────────────────
            SettingsSection(title = "ABOUT") {
                SettingsInfoRow(
                    icon = Icons.Outlined.Info,
                    label = "App Version",
                    value = "v1.0.0 (Edu Edition)"
                )
                SettingsDivider()
                SettingsChevronRow(
                    icon = Icons.Outlined.Gavel,
                    label = "Legal Credits & Licensing"
                )
            }

            // ── Branding footer ───────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SettingsBrandFooter()
        }
    }
}

// ─── Section wrapper ─────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        // Section header label
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = Primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )

        // Card container
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

// ─── Row: Chevron (navigate / select) ────────────────────────────────────────

@Composable
private fun SettingsChevronRow(
    icon: ImageVector,
    label: String,
    value: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Primary.copy(alpha = 0.12f)),
                role = Role.Button,
                onClick = {}
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsIconBox(icon = icon)

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(4.dp))
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ─── Row: Toggle ─────────────────────────────────────────────────────────────

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsIconBox(icon = icon)

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        AnimatedSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ─── Row: Info (read-only value, no chevron) ─────────────────────────────────

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsIconBox(icon = icon)

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Icon box ────────────────────────────────────────────────────────────────

@Composable
private fun SettingsIconBox(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = Primary.copy(alpha = 0.10f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ─── Subtle divider between rows ─────────────────────────────────────────────

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 70.dp, end = 16.dp),
        thickness = 0.8.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

// ─── Animated custom Switch ──────────────────────────────────────────────────

@Composable
private fun AnimatedSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) Primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(durationMillis = 250),
        label = "track_color"
    )
    val thumbColor by animateColorAsState(
        targetValue = Color.White,
        animationSpec = tween(durationMillis = 200),
        label = "thumb_color"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "thumb_offset"
    )

    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) },
                role = Role.Switch
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(24.dp)
                .clip(CircleShape)
                .background(thumbColor)
                .shadow(3.dp, CircleShape)
        )
    }
}

// ─── Branding footer ─────────────────────────────────────────────────────────

@Composable
private fun SettingsBrandFooter() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Gradient icon mark
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(PrimaryLight, Primary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Text(
            text = "SPEAKUP  ·  EDU EDITION",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
