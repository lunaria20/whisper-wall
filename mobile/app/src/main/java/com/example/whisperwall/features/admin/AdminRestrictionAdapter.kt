package com.example.whisperwall.features.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import com.example.whisperwall.R
import com.example.whisperwall.core.repository.AdminRestrictionRequest

class AdminRestrictionAdapter(
    private val requests: MutableList<AdminRestrictionRequest>,
    private val onApprove: (Long) -> Unit,
    private val onReject: (Long) -> Unit
) : RecyclerView.Adapter<AdminRestrictionAdapter.RestrictionViewHolder>() {

    class RestrictionViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvRestrictionUser: TextView = itemView.findViewById(R.id.tvRestrictionUser)
        val tvRestrictionDays: TextView = itemView.findViewById(R.id.tvRestrictionDays)
        val tvRestrictionReason: TextView = itemView.findViewById(R.id.tvRestrictionReason)
        val btnApprove: Button = itemView.findViewById(R.id.btnApproveRequest)
        val btnReject: Button = itemView.findViewById(R.id.btnRejectRequest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestrictionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_restriction, parent, false)
        return RestrictionViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestrictionViewHolder, position: Int) {
        val request = requests[position]
        holder.tvRestrictionUser.text = "Request #${request.id}"
        holder.tvRestrictionDays.text = "User: ${request.userToRestrict} (${request.requestedDurationDays}d)"
        holder.tvRestrictionReason.text = "Reason: ${request.reason}"

        holder.btnApprove.setOnClickListener {
            onApprove(request.id)
        }

        holder.btnReject.setOnClickListener {
            onReject(request.id)
        }
    }

    override fun getItemCount() = requests.size

    fun updateRequests(newRequests: List<AdminRestrictionRequest>) {
        requests.clear()
        requests.addAll(newRequests)
        notifyDataSetChanged()
    }
}
