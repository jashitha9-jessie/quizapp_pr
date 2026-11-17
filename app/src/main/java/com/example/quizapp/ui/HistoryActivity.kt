package com.example.quizapp.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.data.db.AppDatabase
import com.example.quizapp.databinding.ActivityHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val adapter = HistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Score History"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val dao = AppDatabase.get(this).scoreDao()

        // Room's Flow re-emits whenever a new score is inserted
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dao.observeScores(userId).collect { scores ->
                    adapter.submitList(scores)
                    binding.tvEmpty.visibility = if (scores.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        binding.btnClear.setOnClickListener {
            lifecycleScope.launch { dao.clearAll(userId) }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
