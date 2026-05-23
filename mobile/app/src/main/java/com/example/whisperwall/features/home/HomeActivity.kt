package com.example.whisperwall.features.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
    private val createRequestCode = 2001
    private val reportRequestCode = 2002

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
            startActivityForResult(Intent(this, NewConfessionActivity::class.java), createRequestCode)
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
            val intent = Intent(this, ReportConfessionActivity::class.java).apply {
                putExtra(ReportConfessionActivity.EXTRA_CONFESSION_ID, item.id)
                putExtra(ReportConfessionActivity.EXTRA_CONFESSION_CONTENT, item.content)
            }
            startActivityForResult(intent, reportRequestCode)
        }
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return

        when (requestCode) {
            createRequestCode -> {
                showMessage("Confession posted")
                presenter.loadFeed()
            }
            reportRequestCode -> showMessage("Report submitted")
        }
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
