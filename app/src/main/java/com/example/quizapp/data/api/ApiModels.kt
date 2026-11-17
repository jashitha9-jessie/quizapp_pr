package com.example.quizapp.data.api

import com.google.gson.annotations.SerializedName

/** Response of https://opentdb.com/api_category.php */
data class CategoryResponse(
    @SerializedName("trivia_categories") val categories: List<Category>
)

data class Category(
    val id: Int,
    val name: String
) {
    // ArrayAdapter shows toString() in the spinner
    override fun toString(): String = name
}

/** Response of https://opentdb.com/api.php */
data class QuestionResponse(
    @SerializedName("response_code") val responseCode: Int,
    val results: List<ApiQuestion>
)

data class ApiQuestion(
    val question: String,
    @SerializedName("correct_answer") val correctAnswer: String,
    @SerializedName("incorrect_answers") val incorrectAnswers: List<String>
)
