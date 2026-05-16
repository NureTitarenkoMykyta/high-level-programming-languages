package com.example.videoshare.features.videos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videoshare.R
import com.example.videoshare.data.remote.dto.responses.CommentResponse
import java.text.SimpleDateFormat
import java.util.Locale

class CommentAdapter(
    private val comments: List<CommentResponse>,
    private val currentUserRole: String,
    private val onDeleteClick: (CommentResponse) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.tvCommentUsername)
        val tvDate: TextView = view.findViewById(R.id.tvCommentDate)
        val tvText: TextView = view.findViewById(R.id.tvCommentText)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvUsername.text = comment.username
        holder.tvText.text = comment.text

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        holder.tvDate.text = formatter.format(comment.createdAt)

        if (currentUserRole == "admin" || currentUserRole == "moderator") {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener { onDeleteClick(comment) }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = comments.size
}