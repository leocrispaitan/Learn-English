package com.leopc.speakup.data

// ---------------------------------------------------------------------------
// Exercise Types
// ---------------------------------------------------------------------------

/**
 * Supported exercise types.
 * Add new types here when expanding the content catalog (e.g. WORD_ORDER, LISTENING).
 */
enum class ExerciseType {
    MULTIPLE_CHOICE,
    FILL_IN_THE_BLANK
}

// ---------------------------------------------------------------------------
// Exercise
// ---------------------------------------------------------------------------

/**
 * Represents a single exercise document stored in Firestore collection "exercises".
 *
 * Firestore document shape:
 * {
 *   id: String,
 *   level: String,          // e.g. "A1", "A2"
 *   type: String,           // ExerciseType name
 *   question: String,
 *   options: List<String>,  // 4 options for MULTIPLE_CHOICE; empty for FILL_IN_THE_BLANK
 *   correctAnswerIndex: Int,
 *   explanation: String
 * }
 */
data class Exercise(
    val id: String = "",
    val level: String = "A1",
    val type: ExerciseType = ExerciseType.MULTIPLE_CHOICE,
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0,
    val explanation: String = ""
) {
    /**
     * Convert to a plain Map for Firestore writes.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "level" to level,
        "type" to type.name,
        "question" to question,
        "options" to options,
        "correctAnswerIndex" to correctAnswerIndex,
        "explanation" to explanation
    )

    companion object {
        /**
         * Build an Exercise from a Firestore document snapshot map.
         */
        fun fromMap(id: String, map: Map<String, Any>): Exercise {
            val rawType = map["type"] as? String ?: ExerciseType.MULTIPLE_CHOICE.name
            val type = runCatching { ExerciseType.valueOf(rawType) }
                .getOrDefault(ExerciseType.MULTIPLE_CHOICE)

            @Suppress("UNCHECKED_CAST")
            val options = (map["options"] as? List<*>)
                ?.filterIsInstance<String>()
                ?: emptyList()

            return Exercise(
                id = id,
                level = map["level"] as? String ?: "A1",
                type = type,
                question = map["question"] as? String ?: "",
                options = options,
                correctAnswerIndex = (map["correctAnswerIndex"] as? Long)?.toInt() ?: 0,
                explanation = map["explanation"] as? String ?: ""
            )
        }
    }
}

// ---------------------------------------------------------------------------
// User Progress
// ---------------------------------------------------------------------------

/**
 * Represents a user's learning progress stored in Firestore at "userProgress/{uid}".
 *
 * [currentLevel] is an Int (1 = A1, 2 = A2, 3 = B1 …) used to look up the correct
 * exercise set. [levelName] converts it to the human-readable string used in Firestore
 * exercise documents.
 */
data class UserProgress(
    val uid: String = "",
    val currentLevel: Int = 1,
    val xpPoints: Int = 0,
    val completedExercises: List<String> = emptyList()
) {
    /** Human-readable level label that matches the "level" field in exercise documents. */
    val levelName: String
        get() = when (currentLevel) {
            1 -> "A1"
            2 -> "A2"
            3 -> "B1"
            4 -> "B2"
            5 -> "C1"
            else -> "A1"
        }

    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "currentLevel" to currentLevel,
        "xpPoints" to xpPoints,
        "completedExercises" to completedExercises
    )

    companion object {
        fun fromMap(map: Map<String, Any>): UserProgress {
            @Suppress("UNCHECKED_CAST")
            val completed = (map["completedExercises"] as? List<*>)
                ?.filterIsInstance<String>()
                ?: emptyList()

            return UserProgress(
                uid = map["uid"] as? String ?: "",
                currentLevel = (map["currentLevel"] as? Long)?.toInt() ?: 1,
                xpPoints = (map["xpPoints"] as? Long)?.toInt() ?: 0,
                completedExercises = completed
            )
        }
    }
}
