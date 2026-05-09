package com.example.whisperwall.features.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.app.AlertDialog
import android.app.Activity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whisperwall.R
import com.example.whisperwall.core.di.AppContainer
import com.example.whisperwall.features.login.LoginActivity
import com.example.whisperwall.features.profile.ProfileActivity

class HomeActivity : Activity(), HomeContract.View {
    private val appContainer by lazy { AppContainer.from(this) }
    private lateinit var presenter: HomePresenter

    private lateinit var tvWelcome: TextView
    private lateinit var tvError: TextView
    private lateinit var adapter: ConfessionAdapter

    private var items = listOf<HomeContract.ConfessionItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_feature)

        presenter = HomePresenter(
            confessionRepository = appContainer.confessionRepository,
            reportRepository = appContainer.reportRepository,
            sessionManager = appContainer.sessionManager
        )
        presenter.attach(this)

        tvWelcome = findViewById(R.id.tvWelcome)
        tvError = findViewById(R.id.tvError)

        val rv = findViewById<RecyclerView>(R.id.rvConfessions)
        adapter = ConfessionAdapter(
            onLikeClick = { presenter.onToggleLike(it) },
            onSecondaryActionClick = { onSecondaryAction(it) }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            presenter.onLogout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnNewConfession).setOnClickListener {
            openCreateDialog()
        }

        presenter.loadFeed()
    }

    private fun onSecondaryAction(item: HomeContract.ConfessionItem) {
        if (item.isOwn) {
            AlertDialog.Builder(this)
                .setTitle("Delete confession")
                .setMessage("Are you sure you want to delete this confession?")
                .setPositiveButton("Delete") { _, _ ->
                    presenter.onDeleteConfession(item.id)
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            openReportDialog(item.id)
        }
    }

    private fun openCreateDialog() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 0)
        }

        val categories = listOf("Mental Health", "Relationships", "Personal", "Academic", "Other")
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        val input = EditText(this).apply {
            hint = "What's on your mind?"
            minLines = 4
            maxLines = 6
        }

        root.addView(spinner)
        root.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Share Your Confession")
            .setView(root)
            .setPositiveButton("Post") { _, _ ->
                val error = input.validateConfession()
                if (error != null) {
                    showError(error)
                    return@setPositiveButton
                }
                val content = input.text.toString().trim()
                val category = spinner.selectedItem.toString()
                presenter.onPostConfession(content, category)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openReportDialog(confessionId: Long) {
        val reasons = listOf("Inappropriate content", "Harassment", "Spam", "Hate speech", "Self-harm", "Other")
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, reasons)

        AlertDialog.Builder(this)
            .setTitle("Report Confession")
            .setView(spinner)
            .setPositiveButton("Submit") { _, _ ->
                val reason = spinner.selectedItem.toString().sanitizeReportReason()
                presenter.onReportConfession(confessionId, reason)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showWelcome(username: String) {
        tvWelcome.text = "Welcome back, $username!"
    }

    override fun showFeed(items: List<HomeContract.ConfessionItem>) {
        this.items = items
        adapter.submitList(items)
    }

    override fun showError(message: String?) {
        if (message.isNullOrBlank()) {
            tvError.visibility = View.GONE
            tvError.text = ""
            return
        }
        tvError.visibility = View.VISIBLE
        tvError.text = message
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        presenter.detach()
        presenter.destroy()
        super.onDestroy()
    }

    private class ConfessionAdapter(
        private val onLikeClick: (HomeContract.ConfessionItem) -> Unit,
        private val onSecondaryActionClick: (HomeContract.ConfessionItem) -> Unit
    ) : RecyclerView.Adapter<ConfessionAdapter.ConfessionViewHolder>() {

        private val items = mutableListOf<HomeContract.ConfessionItem>()

        fun submitList(next: List<HomeContract.ConfessionItem>) {
            items.clear()
            items.addAll(next)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfessionViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_confession, parent, false)
            return ConfessionViewHolder(view)
        }

        override fun onBindViewHolder(holder: ConfessionViewHolder, position: Int) {
            holder.bind(items[position], onLikeClick, onSecondaryActionClick)
        }

        override fun getItemCount(): Int = items.size

        class ConfessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
            private val tvOwnTag: TextView = itemView.findViewById(R.id.tvOwnTag)
            private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
            private val btnLike: TextView = itemView.findViewById(R.id.btnLike)
            private val btnSecondaryAction: TextView = itemView.findViewById(R.id.btnSecondaryAction)
            private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

            fun bind(
                item: HomeContract.ConfessionItem,
                onLikeClick: (HomeContract.ConfessionItem) -> Unit,
                onSecondaryActionClick: (HomeContract.ConfessionItem) -> Unit
            ) {
                tvCategory.text = item.category
                tvContent.text = item.content
                tvOwnTag.visibility = if (item.isOwn) View.VISIBLE else View.GONE
                tvTime.text = item.time
                btnLike.text = if (item.likedByMe) "Unlike ${item.likes}" else "Like ${item.likes}"
                btnSecondaryAction.text = if (item.isOwn) "Delete" else "Report"
                btnLike.setOnClickListener { onLikeClick(item) }
                btnSecondaryAction.setOnClickListener { onSecondaryActionClick(item) }
            }
        }
    }
}
