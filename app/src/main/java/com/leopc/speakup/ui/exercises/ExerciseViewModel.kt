package com.leopc.speakup.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.leopc.speakup.data.Exercise
import com.leopc.speakup.data.ExerciseRepository
import com.leopc.speakup.data.UserProgress
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

/**
 * Snapshot of everything the ExerciseScreen (and HomeScreen) needs to render.
 *
 * [levelProgressRatio] = completedExercises / totalExercisesInLevel (0f..1f)
 * This drives the CircularProgressIndicator on the HomeScreen.
 */
data class ExerciseUiState(
    val isLoading: Boolean = true,
    val exercises: List<Exercise> = emptyList(),
    val currentIndex: Int = 0,
    val userProgress: UserProgress = UserProgress(),
    val totalExercisesInLevel: Int = 0,
    val answerState: AnswerState = AnswerState.Idle,
    val errorMessage: String? = null,
    val isFinished: Boolean = false
) {
    /** The exercise currently on screen, or null when the list is empty / loading. */
    val currentExercise: Exercise?
        get() = exercises.getOrNull(currentIndex)

    /**
     * Progress ratio for the circular indicator.
     * completedExercises / totalExercisesInLevel, clamped to [0, 1].
     */
    val levelProgressRatio: Float
        get() {
            if (totalExercisesInLevel == 0) return 0f
            val completed = userProgress.completedExercises.size.toFloat()
            return (completed / totalExercisesInLevel).coerceIn(0f, 1f)
        }

    /** For the minutes display: treat each completed exercise as 1 minute. */
    val completedMinutes: Int
        get() = userProgress.completedExercises.size

    val xpPoints: Int get() = userProgress.xpPoints
    val currentLevel: Int get() = userProgress.currentLevel
    val levelName: String get() = userProgress.levelName
}

/** Feedback state after the user submits an answer. */
sealed class AnswerState {
    /** Waiting for the user to pick an answer. */
    object Idle : AnswerState()
    /** User answered correctly. */
    data class Correct(val explanation: String) : AnswerState()
    /** User answered incorrectly. */
    data class Wrong(val selectedIndex: Int, val explanation: String) : AnswerState()
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

/**
 * ExerciseViewModel
 *
 * Single source of truth for both the ExerciseScreen and the dynamic parts
 * of HomeScreen (XP, level, progress ratio).
 *
 * Level-up rule: when the user completes ≥ 80 % of exercises in their current
 * level, [currentLevel] is incremented in Firestore automatically.
 */
class ExerciseViewModel(
    private val repository: ExerciseRepository = ExerciseRepository()
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    // Tracks the active exercises listener so we can cancel it before relaunching
    private var exercisesJob: Job? = null

    // XP awarded per correct answer
    private val XP_PER_CORRECT = 10

    // Percentage of exercises that must be completed to trigger a level-up
    private val LEVEL_UP_THRESHOLD = 0.80f

    init {
        loadData()
    }

    // -----------------------------------------------------------------------
    // Load
    // -----------------------------------------------------------------------

    /**
     * Loads the authenticated user's progress and then starts listening for
     * exercises matching their current level. Both flows are combined so the
     * UI always shows consistent data.
     */
    fun loadData() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Usuario no autenticado.") }
            return
        }

        viewModelScope.launch {
            try {
                // Collect user progress first; every emission re-subscribes to exercises
                repository.getUserProgress(uid)
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Error al cargar progreso: ${e.localizedMessage}"
                            )
                        }
                    }
                    .collect { progress ->
                        val currentProgress = progress ?: UserProgress(uid = uid)

                        // Update state with the freshest progress data
                        _uiState.update {
                            it.copy(userProgress = currentProgress, errorMessage = null)
                        }

                        // Re-subscribe to the exercise list whenever the level changes
                        loadExercisesForProgress(currentProgress)
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun loadExercisesForProgress(progress: UserProgress) {
        // Cancel any previous listener before starting a new one
        exercisesJob?.cancel()
        exercisesJob = viewModelScope.launch {
            try {
                repository.getExercisesForLevel(progress.levelName)
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Error al cargar ejercicios: ${e.localizedMessage}"
                            )
                        }
                    }
                    .collect { exercises ->
                        // Only show exercises the user hasn't completed yet
                        val pending = exercises.filter { ex ->
                            ex.id !in progress.completedExercises
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                exercises = pending,
                                totalExercisesInLevel = exercises.size,
                                currentIndex = 0,
                                // FIX: isFinished only when exercises EXIST but ALL are done.
                                // Empty list = no data loaded yet (show EmptyContent, not FinishedContent).
                                isFinished = exercises.isNotEmpty() && pending.isEmpty(),
                                answerState = AnswerState.Idle,
                                errorMessage = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado al cargar ejercicios: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Answer
    // -----------------------------------------------------------------------

    /**
     * Validates the user's answer for the current exercise.
     *
     * On a correct answer:
     *   1. Awards [XP_PER_CORRECT] XP.
     *   2. Marks the exercise as completed.
     *   3. Persists the updated [UserProgress] to Firestore.
     *   4. Checks the level-up threshold and upgrades if needed.
     *
     * All Firestore writes are wrapped in try-catch so network errors surface
     * as an [errorMessage] on the UI state without crashing.
     *
     * @param selectedIndex The index of the option chosen by the user.
     */
    fun checkAnswer(selectedIndex: Int) {
        val state = _uiState.value
        val exercise = state.currentExercise ?: return
        if (state.answerState !is AnswerState.Idle) return // already answered

        val isCorrect = selectedIndex == exercise.correctAnswerIndex

        if (isCorrect) {
            _uiState.update { it.copy(answerState = AnswerState.Correct(exercise.explanation)) }
            persistCorrectAnswer(exercise.id)
        } else {
            _uiState.update {
                it.copy(
                    answerState = AnswerState.Wrong(
                        selectedIndex = selectedIndex,
                        explanation = exercise.explanation
                    )
                )
            }
        }
    }

    private fun persistCorrectAnswer(exerciseId: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val currentProgress = _uiState.value.userProgress
                val newCompleted = (currentProgress.completedExercises + exerciseId).distinct()
                val newXp = currentProgress.xpPoints + XP_PER_CORRECT

                var updatedProgress = currentProgress.copy(
                    uid = uid,
                    xpPoints = newXp,
                    completedExercises = newCompleted
                )

                // Level-up check
                val totalInLevel = _uiState.value.totalExercisesInLevel
                if (totalInLevel > 0) {
                    val ratio = newCompleted.size.toFloat() / totalInLevel
                    if (ratio >= LEVEL_UP_THRESHOLD) {
                        updatedProgress = updatedProgress.copy(
                            currentLevel = currentProgress.currentLevel + 1,
                            completedExercises = emptyList() // reset for new level
                        )
                    }
                }

                repository.saveUserProgress(updatedProgress)

                // Optimistically update local state so the UI is instant
                _uiState.update { it.copy(userProgress = updatedProgress) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "No se pudo guardar tu progreso: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Navigation
    // -----------------------------------------------------------------------

    /**
     * Advances to the next exercise after the user has seen the feedback.
     * If there are no more exercises, marks the session as finished.
     */
    fun nextExercise() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1
        if (nextIndex < state.exercises.size) {
            _uiState.update {
                it.copy(currentIndex = nextIndex, answerState = AnswerState.Idle)
            }
        } else {
            _uiState.update { it.copy(isFinished = true) }
        }
    }

    /** Resets the session to the first pending exercise. */
    fun restartSession() {
        _uiState.update {
            it.copy(currentIndex = 0, answerState = AnswerState.Idle, isFinished = false)
        }
    }

    /** Clears the current error message so the UI can dismiss the snackbar. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // -----------------------------------------------------------------------
    // Seeding (debug only)
    // -----------------------------------------------------------------------

    /**
     * Seeds Firestore with the 5 built-in A1 exercises.
     *
     * Call once via a debug button in your app.
     * Check the Firebase Console to confirm documents are created, then
     * remove this call from your production code.
     */
    fun seedDatabase() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val success = repository.seedDatabase()
                if (success) {
                    // Seed worked — relaunch the exercises listener so the UI picks up the new docs
                    val currentProgress = _uiState.value.userProgress
                    loadExercisesForProgress(currentProgress)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "⚠️ Seed falló. Verifica las Firestore Security Rules en Firebase Console:\n" +
                                "exercises → allow read: if request.auth != null; allow write: if request.auth != null;"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al hacer seed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}
