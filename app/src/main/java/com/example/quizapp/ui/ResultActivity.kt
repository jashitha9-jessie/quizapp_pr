package com.example.quizapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.quizapp.databinding.ActivityResultBinding
import com.example.quizapp.util.Extras

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Result"

        val score = intent.getIntExtra(Extras.SCORE, 0)
        val total = intent.getIntExtra(Extras.TOTAL, 10)
        val categoryId = intent.getIntExtra(Extras.CATEGORY_ID, 9)
        val categoryName = intent.getStringExtra(Extras.CATEGORY_NAME) ?: ""
        val difficulty = intent.getStringExtra(Extras.DIFFICULTY) ?: "easy"

        val percent = if (total > 0) score * 100 / total else 0

        binding.tvScore.text = "$score / $total"
        binding.tvCategory.text = "$categoryName  •  ${difficulty.replaceFirstChar { it.uppercase() }}"
        binding.tvMessage.text = when {
            percent == 100 -> "Perfect score! Amazing!"
            percent >= 70 -> "Great job!"
            percent >= 40 -> "Not bad, keep practising!"
            else -> "Better luck next time!"
        }

        // Play Again: same category + difficulty, fresh questions
        binding.btnPlayAgain.setOnClickListener {
            val again = Intent(this, QuizActivity::class.java).apply {
                putExtra(Extras.CATEGORY_ID, categoryId)
                putExtra(Extras.CATEGORY_NAME, categoryName)
                putExtra(Extras.DIFFICULTY, difficulty)
            }
            startActivity(again)
            finish()
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.btnHome.setOnClickListener {
            val home = Intent(this, HomeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(home)
            finish()
        }
    }
}
