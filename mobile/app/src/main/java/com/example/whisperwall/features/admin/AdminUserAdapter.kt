package com.example.whisperwall.features.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import com.example.whisperwall.R
import com.example.whisperwall.core.repository.AdminUser

class AdminUserAdapter(
    private val users: MutableList<AdminUser>,
    private val onDelete: (Long) -> Unit,
    private val onRestrict: (Long) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvUserId: TextView = itemView.findViewById(R.id.tvUserId)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val tvUserRole: TextView = itemView.findViewById(R.id.tvUserRole)
        val btnRestrict: Button = itemView.findViewById(R.id.btnRestrictUser)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeleteUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvUserId.text = user.username
        holder.tvUserEmail.text = user.email
        holder.tvUserRole.text = user.role

        holder.btnRestrict.setOnClickListener {
            onRestrict(user.id)
        }

        holder.btnDelete.setOnClickListener {
            onDelete(user.id)
        }
    }

    override fun getItemCount() = users.size

    fun updateUsers(newUsers: List<AdminUser>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}
