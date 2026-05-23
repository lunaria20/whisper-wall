package com.example.whisperwall.features.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import com.example.whisperwall.R
import com.example.whisperwall.core.repository.AdminPost

class AdminPostAdapter(
    private val posts: MutableList<AdminPost>,
    private val onDelete: (Long) -> Unit
) : RecyclerView.Adapter<AdminPostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvPostId: TextView = itemView.findViewById(R.id.tvPostId)
        val tvContent: TextView = itemView.findViewById(R.id.tvPostContent)
        val tvUsername: TextView = itemView.findViewById(R.id.tvPostUsername)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeletePost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.tvPostId.text = "Post #${post.id}"
        holder.tvContent.text = post.content.take(80) + if (post.content.length > 80) "..." else ""
        holder.tvUsername.text = "By: ${post.username}"
        holder.btnDelete.setOnClickListener {
            onDelete(post.id)
        }
    }

    override fun getItemCount() = posts.size

    fun updatePosts(newPosts: List<AdminPost>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }
}
