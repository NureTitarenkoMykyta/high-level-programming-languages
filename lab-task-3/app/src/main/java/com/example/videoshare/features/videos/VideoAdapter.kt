package com.example.videoshare.features.videos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videoshare.R
import com.example.videoshare.data.remote.dto.responses.VideoResponse

class VideoAdapter(
    private val videos: List<VideoResponse>,
    private val onClick: (VideoResponse) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvVideoTitle)
        val tvAuthor: TextView = view.findViewById(R.id.tvAuthor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holder.tvTitle.text = video.title
        holder.tvAuthor.text = "Uploaded by: ${video.username}"
        holder.itemView.setOnClickListener { onClick(video) }
    }

    override fun getItemCount(): Int = videos.size
}