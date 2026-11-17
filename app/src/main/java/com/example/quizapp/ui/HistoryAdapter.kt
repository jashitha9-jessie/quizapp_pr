package com.example.quizapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.data.db.ScoreEntity
import com.example.quizapp.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<ScoreEntity, HistoryAdapter.ViewHolder>(Diff) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    inner class ViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvCategory.text = item.categoryName
            tvDate.text = "${dateFormat.format(Date(item.timestamp))}  •  " +
                item.difficulty.replaceFirstChar { it.uppercase() }
            tvScore.text = "${item.score}/${item.total}"
        }
    }

    private object Diff : DiffUtil.ItemCallback<ScoreEntity>() {
        override fun areItemsTheSame(a: ScoreEntity, b: ScoreEntity) = a.id == b.id
        override fun areContentsTheSame(a: ScoreEntity, b: ScoreEntity) = a == b
    }
}
