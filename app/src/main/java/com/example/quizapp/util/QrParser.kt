package com.example.quizapp.util

/** Parses QR text in the format "category=9;difficulty=easy". */
object QrParser {

    data class QuizParams(val categoryId: Int, val difficulty: String)

    private val validDifficulties = setOf("easy", "medium", "hard")

    fun parse(text: String): QuizParams? {
        val values = text.trim().split(";").mapNotNull { part ->
            val kv = part.split("=", limit = 2)
            if (kv.size == 2) kv[0].trim().lowercase() to kv[1].trim() else null
        }.toMap()

        val category = values["category"]?.toIntOrNull() ?: return null
        val difficulty = values["difficulty"]?.lowercase() ?: return null
        if (difficulty !in validDifficulties) return null

        return QuizParams(category, difficulty)
    }
}
