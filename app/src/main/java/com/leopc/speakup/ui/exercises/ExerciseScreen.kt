package com.leopc.speakup.ui.exercises

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leopc.speakup.data.ExerciseType

// ---------------------------------------------------------------------------
// Colors for feedback
// ---------------------------------------------------------------------------
private val CorrectGreen = Color(0xFF4CAF50)
private val WrongRed = Color(0xFFF44336)
private val NeutralGray = Color(0xFF757575)

// ---------------------------------------------------------------------------
// ExerciseScreen
// ---------------------------------------------------------------------------

/**
 * Full-screen exercise quiz.
 *
 * Reads state from [ExerciseViewModel] and delegates all user actions back to it.
 * Call [onNavigateBack] to return to the HomeScreen.
 *
 * Debug tip: add a "Seed DB" button here temporarily while testing.
 * Example:
 *   Button(onClick = { viewModel.seedDatabase() }) { Text("Seed DB") }
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    viewModel: ExerciseViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error messages via Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nivel ${uiState.levelName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> LoadingContent()
                uiState.isFinished -> FinishedContent(
                    xpPoints = uiState.xpPoints,
                    levelName = uiState.levelName,
                    onRestart = { viewModel.restartSession() },
                    onBack = onNavigateBack
                )
                uiState.currentExercise == null -> EmptyContent(
                    onBack = onNavigateBack,
                    onSeedDatabase = { viewModel.seedDatabase() } // DEBUG ONLY – remove after seeding
                )
                else -> QuizContent(
                    uiState = uiState,
                    onAnswerSelected = { index -> viewModel.checkAnswer(index) },
                    onNext = { viewModel.nextExercise() }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Quiz Content
// ---------------------------------------------------------------------------

@Composable
private fun QuizContent(
    uiState: ExerciseUiState,
    onAnswerSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    val exercise = uiState.currentExercise ?: return
    val answerState = uiState.answerState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Progress bar
        val progressFraction = if (uiState.exercises.isEmpty()) 0f
        else (uiState.currentIndex + 1).toFloat() / uiState.exercises.size
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Exercise counter
        Text(
            text = "Ejercicio ${uiState.currentIndex + 1} de ${uiState.exercises.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Question card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = exercise.question,
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Answer options
        when (exercise.type) {
            ExerciseType.MULTIPLE_CHOICE, ExerciseType.FILL_IN_THE_BLANK -> {
                exercise.options.forEachIndexed { index, option ->
                    AnswerButton(
                        text = option,
                        index = index,
                        answerState = answerState,
                        correctIndex = exercise.correctAnswerIndex,
                        onClick = { if (answerState is AnswerState.Idle) onAnswerSelected(index) }
                    )
                }
            }
        }

        // Feedback + explanation
        AnimatedVisibility(
            visible = answerState !is AnswerState.Idle,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(tween(300)),
            exit = fadeOut()
        ) {
            val (feedbackText, feedbackColor, explanation) = when (answerState) {
                is AnswerState.Correct -> Triple(
                    "✅ ¡Correcto! +10 XP",
                    CorrectGreen,
                    answerState.explanation
                )
                is AnswerState.Wrong -> Triple(
                    "❌ Incorrecto",
                    WrongRed,
                    answerState.explanation
                )
                else -> Triple("", Color.Transparent, "")
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = feedbackColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = feedbackText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = feedbackColor
                    )
                    if (explanation.isNotEmpty()) {
                        Text(
                            text = explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // "Next" button — only visible after answering
        AnimatedVisibility(
            visible = answerState !is AnswerState.Idle,
            enter = fadeIn(tween(300)),
            exit = fadeOut()
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Siguiente →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Answer Button
// ---------------------------------------------------------------------------

@Composable
private fun AnswerButton(
    text: String,
    index: Int,
    answerState: AnswerState,
    correctIndex: Int,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        answerState is AnswerState.Correct && index == correctIndex ->
            CorrectGreen.copy(alpha = 0.15f)
        answerState is AnswerState.Wrong && index == answerState.selectedIndex ->
            WrongRed.copy(alpha = 0.15f)
        answerState is AnswerState.Wrong && index == correctIndex ->
            CorrectGreen.copy(alpha = 0.10f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        answerState is AnswerState.Correct && index == correctIndex -> CorrectGreen
        answerState is AnswerState.Wrong && index == answerState.selectedIndex -> WrongRed
        answerState is AnswerState.Wrong && index == correctIndex -> CorrectGreen
        else -> MaterialTheme.colorScheme.outline
    }

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ---------------------------------------------------------------------------
// States: Loading / Finished / Empty
// ---------------------------------------------------------------------------

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Cargando ejercicios…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FinishedContent(
    xpPoints: Int,
    levelName: String,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = "🎉", fontSize = 64.sp)
            Text(
                text = "¡Sesión completada!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Has terminado todos los ejercicios del nivel $levelName.\nTotal XP: $xpPoints",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Repetir sesión", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Volver al inicio")
            }
        }
    }
}

@Composable
private fun EmptyContent(
    onBack: () -> Unit,
    onSeedDatabase: () -> Unit   // DEBUG ONLY – remove after seeding
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "📂", fontSize = 48.sp)
            Text(
                text = "No hay ejercicios disponibles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Primero carga los ejercicios de prueba presionando el botón de abajo, luego verifica en Firebase Console que aparezcan 5 documentos en la colección 'exercises'.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // ⚠️ DEBUG ONLY – quitar después del primer seed
            Button(
                onClick = onSeedDatabase,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF388E3C) // verde para distinguirlo
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🌱 Seed Database (DEBUG)",
                    fontWeight = FontWeight.Bold
                )
            }
            // ⚠️ Fin del bloque DEBUG

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Volver al inicio")
            }
        }
    }
}
