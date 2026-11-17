package com.example.quizapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quizapp.data.api.Category
import com.example.quizapp.data.api.RetrofitClient
import com.example.quizapp.databinding.ActivityHomeBinding
import com.example.quizapp.util.Extras
import com.example.quizapp.util.GoogleAuthHelper
import com.example.quizapp.util.QrParser
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val auth = FirebaseAuth.getInstance()

    private var categories: List<Category> = emptyList()
    private val difficulties = listOf("easy", "medium", "hard")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = auth.currentUser
        if (user == null) {
            goToLogin()
            return
        }
        binding.tvWelcome.text = "Hello, ${user.displayName ?: "Player"}!"

        binding.spinnerDifficulty.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Easy", "Medium", "Hard")
        )

        binding.btnScan.setOnClickListener { scanQr() }
        binding.btnStart.setOnClickListener { startManualQuiz() }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnSignOut.setOnClickListener { signOut() }
        binding.btnRetryCategories.setOnClickListener { loadCategories() }

        loadCategories()
    }

    // ---------- Categories (https://opentdb.com/api_category.php) ----------

    private fun loadCategories() {
        binding.progressCategories.visibility = View.VISIBLE
        binding.btnRetryCategories.visibility = View.GONE
        binding.btnStart.isEnabled = false

        lifecycleScope.launch {
            try {
                categories = RetrofitClient.api.getCategories().categories
                binding.spinnerCategory.adapter = ArrayAdapter(
                    this@HomeActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    categories
                )
                binding.btnStart.isEnabled = categories.isNotEmpty()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.btnRetryCategories.visibility = View.VISIBLE
                toast("Could not load categories. Check your internet connection.")
            } finally {
                binding.progressCategories.visibility = View.GONE
            }
        }
    }

    // ---------- Manual start ----------

    private fun startManualQuiz() {
        val category = binding.spinnerCategory.selectedItem as? Category ?: return
        val difficulty = difficulties[binding.spinnerDifficulty.selectedItemPosition]
        startQuiz(category.id, category.name, difficulty)
    }

    // ---------- QR start: "category=9;difficulty=easy" ----------

    private fun scanQr() {
        GmsBarcodeScanning.getClient(this)
            .startScan()
            .addOnSuccessListener { barcode ->
                val params = barcode.rawValue?.let { QrParser.parse(it) }
                if (params == null) {
                    toast("Invalid QR code. Expected: category=9;difficulty=easy")
                } else {
                    startQuiz(params.categoryId, categoryNameFor(params.categoryId), params.difficulty)
                }
            }
            .addOnCanceledListener {
                // user closed the scanner - nothing to do
            }
            .addOnFailureListener { e ->
                toast("Scanner error: ${e.localizedMessage}")
            }
    }

    private fun categoryNameFor(id: Int): String =
        categories.firstOrNull { it.id == id }?.name ?: "Category $id"

    // ---------- Navigation ----------

    private fun startQuiz(categoryId: Int, categoryName: String, difficulty: String) {
        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra(Extras.CATEGORY_ID, categoryId)
            putExtra(Extras.CATEGORY_NAME, categoryName)
            putExtra(Extras.DIFFICULTY, difficulty)
        }
        startActivity(intent)
    }

    private fun signOut() {
        auth.signOut()
        GoogleAuthHelper.client(this).signOut().addOnCompleteListener { goToLogin() }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
