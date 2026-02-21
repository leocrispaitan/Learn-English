package com.leopc.speakup.ui.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.leopc.speakup.ui.theme.*
import androidx.compose.foundation.layout.WindowInsets

// ─── Data models ────────────────────────────────────────────────────────────

data class GoalItem(
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val title: String,
    val progress: Float,          // 0f..1f
    val progressColor: Color,
    val percentLabel: String,
    val subtitle: String
)

// ─── Main screen ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String = "Alex Johnson",
    userPhotoUrl: String? = null,
    levelLabel: String = "Intermediate – B1",
    memberSince: String = "Member since Jan 2024",
    dayStreak: Int = 12,
    wordsLearned: Int = 1240,
    lessonsCompleted: Int = 45,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val goals = listOf(
        GoalItem(
            icon = Icons.Filled.MenuBook,
            iconBg = Color(0xFFE8F0FE),
            iconTint = Primary,
            title = "Daily Vocabulary",
            progress = 0.75f,
            progressColor = Primary,
            percentLabel = "75%",
            subtitle = "15/20"
        ),
        GoalItem(
            icon = Icons.Filled.Mic,
            iconBg = Color(0xFFFFF3E0),
            iconTint = OrangeFlame,
            title = "Speaking Practice",
            progress = 0.40f,
            progressColor = OrangeFlame,
            percentLabel = "40%",
            subtitle = "4/10m"
        ),
        GoalItem(
            icon = Icons.Filled.EditNote,
            iconBg = Color(0xFFE8F5E9),
            iconTint = GreenSuccess,
            title = "Weekly Grammar Quiz",
            progress = 1.00f,
            progressColor = GreenSuccess,
            percentLabel = "100%",
            subtitle = "1/1"
        )
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "Settings",
                                    tint = Primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
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
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Avatar + name + level ──────────────────────────────────────
            ProfileHeader(
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                levelLabel = levelLabel,
                memberSince = memberSince
            )

            Spacer(Modifier.height(24.dp))

            // ── Stats Row ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Filled.LocalFireDepartment,
                    iconTint = OrangeFlame,
                    value = dayStreak.toString(),
                    label = "Day Streak",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Filled.MenuBook,
                    iconTint = Primary,
                    value = "%,d".format(wordsLearned),
                    label = "Words",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Filled.EmojiEvents,
                    iconTint = GreenSuccess,
                    value = lessonsCompleted.toString(),
                    label = "Lessons",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── My Goals ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { /* Edit goals */ }) {
                    Text(
                        text = "Edit",
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                goals.forEachIndexed { index, goal ->
                    AnimatedGoalCard(goal = goal, animDelay = index * 80)
                }

                // Add New Goal button
                OutlinedButton(
                    onClick = { /* Add goal */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Gray300, Gray200)
                        )
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Gray500
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Add New Goal",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─── Profile header ─────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(
    userName: String,
    userPhotoUrl: String?,
    levelLabel: String,
    memberSince: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Avatar with verified badge
        Box(contentAlignment = Alignment.BottomEnd) {
            // Avatar circle
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(8.dp, CircleShape),
                shape = CircleShape,
                color = Gray200
            ) {
                if (userPhotoUrl != null) {
                    AsyncImage(
                        model = userPhotoUrl,
                        contentDescription = "Profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Gray500
                        )
                    }
                }
            }

            // Verified badge
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .offset(x = (-2).dp, y = (-4).dp),
                shape = CircleShape,
                color = Primary,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verified",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Name
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Level pill
        Surface(
            shape = RoundedCornerShape(50),
            color = Primary.copy(alpha = 0.12f)
        ) {
            Text(
                text = levelLabel,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = Primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Member since
        Text(
            text = memberSince,
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
    }
}

// ─── Stat card ──────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Gray200)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Gray500,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Animated goal card ─────────────────────────────────────────────────────

@Composable
private fun AnimatedGoalCard(goal: GoalItem, animDelay: Int = 0) {
    // Animate progress bar width from 0 → actual value on first composition
    var targeted by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (targeted) goal.progress else 0f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = animDelay,
            easing = FastOutSlowInEasing
        ),
        label = "progress_${goal.title}"
    )
    LaunchedEffect(Unit) { targeted = true }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Gray200)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon box
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = goal.iconBg
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = goal.icon,
                        contentDescription = null,
                        tint = goal.iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Title + progress
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = goal.percentLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = goal.progressColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LinearProgressBar(
                        progress = animatedProgress,
                        color = goal.progressColor,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = goal.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
            }
        }
    }
}

// ─── Linear progress bar ────────────────────────────────────────────────────

@Composable
private fun LinearProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(Gray200)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}
