package com.example.quizapp.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.quizapp.R
import com.example.quizapp.data.api.RetrofitClient
import com.example.quizapp.data.db.AppDatabase
import com.example.quizapp.data.db.ScoreEntity
import com.example.quizapp.databinding.ActivityQuizBinding
import com.example.quizapp.model.Question
import com.example.quizapp.util.Extras
import com.example.quizapp.util.decodeHtml
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var optionButtons: List<MaterialButton>

    private var categoryId = 9
    private var categoryName = ""
    private var difficulty = "easy"

    private var questions: List<Question> = emptyList()
    private var currentIndex = 0
    private var score = 0
    private var answered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryId = intent.getIntExtra(Extras.CATEGORY_ID, 9)
        categoryName = intent.getStringExtra(Extras.CATEGORY_NAME) ?: "General Knowledge"
        difficulty = intent.getStringExtra(Extras.DIFFICULTY) ?: "easy"

        supportActionBar?.title = categoryName

        optionButtons = listOf(
            binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4
        )
        optionButtons.forEachIndexed { index, button ->
            button.setOnClickListener { onOptionSelected(index) }
        }
        binding.btnNext.setOnClickListener { onNextClicked() }
        binding.btnRetry.setOnClickListener { loadQuestions() }

        loadQuestions()
    }

    // ---------- Loading from Open Trivia DB ----------

    private fun loadQuestions() {
        showState(loading = true, error = false)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getQuestions(
                    category = categoryId,
                    difficulty = difficulty
                )
                if (response.responseCode != 0 || response.results.isEmpty()) {
                    showError("Not enough questions for this category and difficulty. Try another one.")
                    return@launch
                }

                questions = response.results.map { q ->
                    val correct = q.correctAnswer.decodeHtml()
                    val options = (q.incorrectAnswers.map { it.decodeHtml() } + correct).shuffled()
                    Question(q.question.decodeHtml(), options, correct)
                }
                currentIndex = 0
                score = 0
                showState(loading = false, error = false)
                showQuestion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    showError("Too many requests. Wait about 5 seconds and tap Retry.")
                } else {
                    showError("Server error (${e.code()}). Please retry.")
                }
            } catch (e: IOException) {
                showError("No internet connection. Please retry.")
            } catch (e: Exception) {
                showError("Something went wrong: ${e.localizedMessage}")
            }
        }
    }

    // ---------- Showing a question ----------

    private fun showQuestion() {
        answered = false
        val question = questions[currentIndex]
        val total = questions.size

        binding.tvProgress.text = "Question ${currentIndex + 1} / $total"
        binding.progressBar.max = total
        binding.progressBar.progress = currentIndex + 1
        binding.tvQuestion.text = question.text

        optionButtons.forEachIndexed { i, button ->
            if (i < question.options.size) {
                button.visibility = View.VISIBLE
                button.text = question.options[i]
                paint(button, R.color.option_bg, R.color.text_dark)
            } else {
                button.visibility = View.GONE
            }
        }

        binding.btnNext.visibility = View.INVISIBLE
        binding.btnNext.isEnabled = true
        binding.btnNext.text = if (currentIndex == total - 1) "Finish" else "Next"
    }

    private fun onOptionSelected(selectedIndex: Int) {
        if (answered) return
        answered = true

        val question = questions[currentIndex]
        val correctIndex = question.options.indexOf(question.correctAnswer)

        if (selectedIndex == correctIndex) {
            score++
            paint(optionButtons[selectedIndex], R.color.correct_green, R.color.white)
        } else {
            paint(optionButtons[selectedIndex], R.color.wrong_red, R.color.white)
            // also reveal the right answer
            if (correctIndex in optionButtons.indices) {
                paint(optionButtons[correctIndex], R.color.correct_green, R.color.white)
            }
        }
        binding.btnNext.visibility = View.VISIBLE
    }

    private fun onNextClicked() {
        if (currentIndex < questions.size - 1) {
            currentIndex++
            showQuestion()
        } else {
            finishQuiz()
        }
    }

    // ---------- Finish: save to Room and open the result screen ----------

    private fun finishQuiz() {
        binding.btnNext.isEnabled = false  // avoid saving twice on a double tap
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val total = questions.size

        lifecycleScope.launch {
            AppDatabase.get(this@QuizActivity).scoreDao().insert(
                ScoreEntity(
                    userId = userId,
                    timestamp = System.currentTimeMillis(),
                    categoryName = categoryName,
                    difficulty = difficulty,
                    score = score,
                    total = total
                )
            )
            val intent = Intent(this@QuizActivity, ResultActivity::class.java).apply {
                putExtra(Extras.SCORE, score)
                putExtra(Extras.TOTAL, total)
                putExtra(Extras.CATEGORY_ID, categoryId)
                putExtra(Extras.CATEGORY_NAME, categoryName)
                putExtra(Extras.DIFFICULTY, difficulty)
            }
            startActivity(intent)
            finish()
        }
    }

    // ---------- UI helpers ----------

    private fun paint(button: MaterialButton, backgroundColorRes: Int, textColorRes: Int) {
        button.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, backgroundColorRes))
        button.setTextColor(ContextCompat.getColor(this, textColorRes))
    }

    private fun showState(loading: Boolean, error: Boolean) {
        binding.loadingLayout.visibility = if (loading) View.VISIBLE else View.GONE
        binding.errorLayout.visibility = if (error) View.VISIBLE else View.GONE
        binding.contentLayout.visibility = if (!loading && !error) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        showState(loading = false, error = true)
    }
}
