package com.leopc.speakup.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * ExerciseRepository
 *
 * All Firebase Firestore access is centralised here.
 * The repository is stateless — it delegates state management to the ViewModel.
 *
 * Collections:
 *   - "exercises"        → Exercise documents
 *   - "userProgress"     → UserProgress documents (keyed by uid)
 */
class ExerciseRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // -----------------------------------------------------------------------
    // Exercises
    // -----------------------------------------------------------------------

    /**
     * Returns a [Flow] that emits the list of exercises for [level] ("A1", "A2", …).
     * The flow stays active and re-emits whenever Firestore data changes.
     */
    fun getExercisesForLevel(level: String): Flow<List<Exercise>> = callbackFlow {
        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("exercises")
                .whereEqualTo("level", level)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val exercises = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            @Suppress("UNCHECKED_CAST")
                            Exercise.fromMap(doc.id, doc.data as Map<String, Any>)
                        } catch (e: Exception) {
                            null // skip malformed documents silently
                        }
                    } ?: emptyList()
                    trySend(exercises)
                }
        } catch (e: Exception) {
            close(e)
        }
        awaitClose { registration?.remove() }
    }

    // -----------------------------------------------------------------------
    // User Progress
    // -----------------------------------------------------------------------

    /**
     * Returns a [Flow] that emits the [UserProgress] for [uid].
     * Emits `null` if the document doesn't exist yet (first-time users).
     */
    fun getUserProgress(uid: String): Flow<UserProgress?> = callbackFlow {
        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("userProgress")
                .document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val progress = if (snapshot != null && snapshot.exists()) {
                        try {
                            @Suppress("UNCHECKED_CAST")
                            UserProgress.fromMap(snapshot.data as Map<String, Any>)
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                    trySend(progress)
                }
        } catch (e: Exception) {
            close(e)
        }
        awaitClose { registration?.remove() }
    }

    /**
     * Writes (merges) a [UserProgress] document.
     * Throws on network failures — callers should wrap in try-catch.
     */
    suspend fun saveUserProgress(progress: UserProgress) {
        db.collection("userProgress")
            .document(progress.uid)
            .set(progress.toMap())
            .await()
    }

    // -----------------------------------------------------------------------
    // Database Seeding (run ONCE, then remove the call)
    // -----------------------------------------------------------------------

    /**
     * Seeds Firestore with 5 real A1-level English exercises.
     *
     * Call this once (e.g. via a debug button) and verify the data in
     * the Firebase Console. After seeding, remove the call from your code.
     *
     * @return `true` on success, `false` on failure.
     */
    suspend fun seedDatabase(): Boolean {
        val a1Exercises = listOf(
            Exercise(
                id = "a1_001",
                level = "A1",
                type = ExerciseType.MULTIPLE_CHOICE,
                question = "How do you greet someone in the morning?",
                options = listOf("Good morning", "Good night", "Goodbye", "See you"),
                correctAnswerIndex = 0,
                explanation = "'Good morning' is the standard greeting used in the morning hours."
            ),
            Exercise(
                id = "a1_002",
                level = "A1",
                type = ExerciseType.MULTIPLE_CHOICE,
                question = "Which sentence is correct?",
                options = listOf(
                    "She is a teacher.",
                    "She am a teacher.",
                    "She are a teacher.",
                    "She be a teacher."
                ),
                correctAnswerIndex = 0,
                explanation = "For he/she/it we use 'is'. The verb 'to be': I am, You are, He/She/It is."
            ),
            Exercise(
                id = "a1_003",
                level = "A1",
                type = ExerciseType.MULTIPLE_CHOICE,
                question = "What does 'Hello, how are you?' mean in common usage?",
                options = listOf(
                    "A greeting asking about someone's wellbeing",
                    "A farewell expression",
                    "A way to say thank you",
                    "A question about the weather"
                ),
                correctAnswerIndex = 0,
                explanation = "'Hello, how are you?' is a basic greeting used to acknowledge and check on someone."
            ),
            Exercise(
                id = "a1_004",
                level = "A1",
                type = ExerciseType.MULTIPLE_CHOICE,
                question = "Choose the correct response to 'What is your name?'",
                options = listOf(
                    "My name is Maria.",
                    "I have 20 years.",
                    "I am fine, thank you.",
                    "Nice to meet you."
                ),
                correctAnswerIndex = 0,
                explanation = "The correct answer to 'What is your name?' introduces your name. 'My name is ___' is the standard form."
            ),
            Exercise(
                id = "a1_005",
                level = "A1",
                type = ExerciseType.FILL_IN_THE_BLANK,
                question = "Please ___ down. (invitar a sentarse)",
                options = listOf("sit", "sat", "sits", "sitting"),
                correctAnswerIndex = 0,
                explanation = "After 'please', use the base form of the verb. 'Please sit down' is a polite invitation."
            )
        )

        return try {
            val batch = db.batch()
            a1Exercises.forEach { exercise ->
                val ref = db.collection("exercises").document(exercise.id)
                batch.set(ref, exercise.toMap())
            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
