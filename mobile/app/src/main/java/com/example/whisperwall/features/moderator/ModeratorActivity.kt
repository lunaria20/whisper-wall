package com.example.whisperwall.features.moderator

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.app.Activity
import android.app.AlertDialog
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whisperwall.R
import com.example.whisperwall.core.di.AppContainer
import com.example.whisperwall.core.model.Report
import com.example.whisperwall.features.common.TimeFormatter
import com.example.whisperwall.features.login.LoginActivity
import com.example.whisperwall.features.profile.ProfileActivity

class ModeratorActivity : Activity(), ModeratorContract.View {
    private val appContainer by lazy { AppContainer.from(this) }
    private lateinit var presenter: ModeratorPresenter

    private lateinit var tvStatus: TextView
    private lateinit var adapter: ModeratorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moderator_feature)

        presenter = ModeratorPresenter(appContainer.moderatorRepository, appContainer.sessionManager)
        presenter.attach(this)

        tvStatus = findViewById(R.id.tvStatus)

        val rv = findViewById<RecyclerView>(R.id.rvReports)
        adapter = ModeratorAdapter(
            onDismiss = { presenter.dismissReport(it.id) },
            onRemove = { presenter.removeConfession(it.id) }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<Button>(R.id.btnPending).setOnClickListener { presenter.selectStatus("PENDING") }
        findViewById<Button>(R.id.btnReviewed).setOnClickListener { presenter.selectStatus("REVIEWED") }
        findViewById<Button>(R.id.btnDismissed).setOnClickListener { presenter.selectStatus("DISMISSED") }

        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            presenter.logout()
        }

        findViewById<Button>(R.id.btnRequestRestriction).setOnClickListener {
            showRestrictionRequestDialog()
        }

        presenter.selectStatus("PENDING")
    }

    private fun showRestrictionRequestDialog() {
        val userIdInput = EditText(this).apply { hint = "User ID" }
        val reasonInput = EditText(this).apply { hint = "Reason" }
        val durationInput = EditText(this).apply { hint = "Duration days"; setText("7") }

        val dialogView = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(userIdInput)
            addView(reasonInput)
            addView(durationInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Request Restriction")
            .setView(dialogView)
            .setPositiveButton("Send") { _, _ ->
                val userId = userIdInput.text.toString().trim().toLongOrNull()
                val reason = reasonInput.text.toString().trim()
                val durationDays = durationInput.text.toString().trim().toIntOrNull() ?: 7

                if (userId == null || reason.isBlank()) {
                    Toast.makeText(this, "Enter a valid user ID and reason", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                presenter.sendRestrictionRequest(userId, reason, durationDays)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showReports(reports: List<Report>) {
        adapter.submitList(reports)
    }

    override fun showStatus(message: String?) {
        if (message.isNullOrBlank()) {
            tvStatus.visibility = View.GONE
            tvStatus.text = ""
            return
        }
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = message
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onLogoutCompleted() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        presenter.detach()
        presenter.destroy()
        super.onDestroy()
    }

    private class ModeratorAdapter(
        private val onDismiss: (Report) -> Unit,
        private val onRemove: (Report) -> Unit
    ) : RecyclerView.Adapter<ModeratorAdapter.ReportViewHolder>() {

        private val items = mutableListOf<Report>()

        fun submitList(next: List<Report>) {
            items.clear()
            items.addAll(next)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
            return ReportViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
            holder.bind(items[position], onDismiss, onRemove)
        }

        override fun getItemCount(): Int = items.size

        class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvHeader: TextView = itemView.findViewById(R.id.tvHeader)
            private val tvReason: TextView = itemView.findViewById(R.id.tvReason)
            private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
            private val tvWhen: TextView = itemView.findViewById(R.id.tvWhen)
            private val btnDismiss: Button = itemView.findViewById(R.id.btnDismiss)
            private val btnRemove: Button = itemView.findViewById(R.id.btnRemove)

            fun bind(report: Report, onDismiss: (Report) -> Unit, onRemove: (Report) -> Unit) {
                tvHeader.text = "#${report.id} ${report.confessionCategory} by ${report.reportedByUsername}"
                tvReason.text = "Reason: ${report.reason}"
                tvContent.text = report.confessionContent
                tvWhen.text = TimeFormatter.relativeTime(report.createdAt)

                val actionable = report.status.equals("PENDING", ignoreCase = true)
                btnDismiss.visibility = if (actionable) View.VISIBLE else View.GONE
                btnRemove.visibility = if (actionable) View.VISIBLE else View.GONE
                btnDismiss.setOnClickListener { onDismiss(report) }
                btnRemove.setOnClickListener { onRemove(report) }
            }
        }
    }
}
