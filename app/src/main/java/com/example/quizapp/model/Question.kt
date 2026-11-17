package com.example.quizapp.model

/** A question ready for the UI: text decoded, options already shuffled. */
data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswer: String
)
