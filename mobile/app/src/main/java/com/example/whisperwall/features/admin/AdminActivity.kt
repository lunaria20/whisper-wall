package com.example.whisperwall.features.admin

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whisperwall.R
import com.example.whisperwall.core.di.AppContainer
import com.example.whisperwall.core.repository.AdminPost
import com.example.whisperwall.core.repository.AdminUser
import com.example.whisperwall.core.repository.AdminRestrictionRequest
import com.example.whisperwall.core.repository.AdminUsageStats
import com.example.whisperwall.features.login.LoginActivity
import com.example.whisperwall.features.profile.ProfileActivity

class AdminActivity : Activity(), AdminContract.View {
    private val appContainer by lazy { AppContainer.from(this) }
    private lateinit var presenter: AdminPresenter

    // Tab buttons
    private lateinit var tabStats: Button
    private lateinit var tabPosts: Button
    private lateinit var tabRestrictions: Button
    private lateinit var tabUsers: Button

    // Tab contents
    private lateinit var statsContent: LinearLayout
    private lateinit var postsContent: android.widget.FrameLayout
    private lateinit var restrictionsContent: android.widget.FrameLayout
    private lateinit var usersContent: android.widget.FrameLayout

    // Stats views
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalPosts: TextView
    private lateinit var tvTotalComments: TextView
    private lateinit var tvTotalReports: TextView
    private lateinit var tvActiveUsers: TextView
    private lateinit var tvRestrictedUsers: TextView

    // RecyclerViews
    private lateinit var rvPosts: RecyclerView
    private lateinit var rvUsers: RecyclerView
    private lateinit var rvRestrictions: RecyclerView

    // Adapters
    private lateinit var postAdapter: AdminPostAdapter
    private lateinit var userAdapter: AdminUserAdapter
    private lateinit var restrictionAdapter: AdminRestrictionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        presenter = AdminPresenter(appContainer.adminRepository, appContainer.sessionManager)
        presenter.attach(this)

        initializeViews()
        setupListeners()
        setupRecyclerViews()
        showTab("stats")
        presenter.loadStats()
    }

    private fun initializeViews() {
        // Tab buttons
        tabStats = findViewById(R.id.tabStats)
        tabPosts = findViewById(R.id.tabPosts)
        tabRestrictions = findViewById(R.id.tabRestrictions)
        tabUsers = findViewById(R.id.tabUsers)

        // Tab contents
        statsContent = findViewById(R.id.statsContent)
        postsContent = findViewById(R.id.postsContent)
        restrictionsContent = findViewById(R.id.restrictionsContent)
        usersContent = findViewById(R.id.usersContent)

        // Stats views
        tvTotalUsers = findViewById(R.id.statTotalUsers)
        tvTotalPosts = findViewById(R.id.statTotalPosts)
        tvTotalComments = findViewById(R.id.statTotalComments)
        tvTotalReports = findViewById(R.id.statTotalReports)
        tvActiveUsers = findViewById(R.id.statActiveUsers)
        tvRestrictedUsers = findViewById(R.id.statRestrictedUsers)

        // RecyclerViews
        rvPosts = findViewById(R.id.rvPosts)
        rvUsers = findViewById(R.id.rvUsers)
        rvRestrictions = findViewById(R.id.rvRestrictions)

        // Header buttons
        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            presenter.logout()
        }
    }

    private fun setupListeners() {
        tabStats.setOnClickListener { showTab("stats") }
        tabPosts.setOnClickListener { showTab("posts") }
        tabRestrictions.setOnClickListener { showTab("restrictions") }
        tabUsers.setOnClickListener { showTab("users") }

        findViewById<Button>(R.id.btnRefreshStats).setOnClickListener {
            presenter.loadStats()
        }

        findViewById<Button>(R.id.btnLoadMorePosts).setOnClickListener {
            presenter.loadPosts()
        }

        findViewById<Button>(R.id.btnLoadRestrictions).setOnClickListener {
            presenter.loadRestrictionRequests()
        }

        findViewById<Button>(R.id.btnLoadUsers).setOnClickListener {
            presenter.loadUsers()
        }

        findViewById<Button>(R.id.btnCreateUser).setOnClickListener {
            showCreateUserDialog()
        }
    }

    private fun setupRecyclerViews() {
        // Posts RecyclerView
        postAdapter = AdminPostAdapter(mutableListOf()) { postId ->
            AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete") { _, _ ->
                    presenter.deletePost(postId)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        rvPosts.layoutManager = LinearLayoutManager(this)
        rvPosts.adapter = postAdapter

        // Users RecyclerView
        userAdapter = AdminUserAdapter(
            mutableListOf(),
            onDelete = { userId ->
                AlertDialog.Builder(this)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton("Delete") { _, _ ->
                        presenter.deleteUser(userId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onRestrict = { userId ->
                showRestrictUserDialog(userId)
            }
        )
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = userAdapter

        // Restrictions RecyclerView
        restrictionAdapter = AdminRestrictionAdapter(
            mutableListOf(),
            onApprove = { requestId ->
                presenter.approveRestrictionRequest(requestId)
            },
            onReject = { requestId ->
                showRejectDialog(requestId)
            }
        )
        rvRestrictions.layoutManager = LinearLayoutManager(this)
        rvRestrictions.adapter = restrictionAdapter
    }

    private fun showTab(tab: String) {
        // Hide all
        statsContent.visibility = android.view.View.GONE
        postsContent.visibility = android.view.View.GONE
        restrictionsContent.visibility = android.view.View.GONE
        usersContent.visibility = android.view.View.GONE

        // Reset button colors
        tabStats.alpha = 0.6f
        tabPosts.alpha = 0.6f
        tabRestrictions.alpha = 0.6f
        tabUsers.alpha = 0.6f

        // Show selected tab
        when (tab) {
            "stats" -> {
                statsContent.visibility = android.view.View.VISIBLE
                tabStats.alpha = 1f
            }
            "posts" -> {
                postsContent.visibility = android.view.View.VISIBLE
                tabPosts.alpha = 1f
            }
            "restrictions" -> {
                restrictionsContent.visibility = android.view.View.VISIBLE
                tabRestrictions.alpha = 1f
            }
            "users" -> {
                usersContent.visibility = android.view.View.VISIBLE
                tabUsers.alpha = 1f
            }
        }
    }

    override fun showStats(stats: AdminUsageStats) {
        tvTotalUsers.text = stats.totalUsers.toString()
        tvTotalPosts.text = stats.totalPosts.toString()
        tvTotalComments.text = stats.totalComments.toString()
        tvTotalReports.text = stats.totalReports.toString()
        tvActiveUsers.text = stats.activeUsers.toString()
        tvRestrictedUsers.text = stats.restrictedUsers.toString()
    }

    override fun showPosts(posts: List<AdminPost>) {
        postAdapter.updatePosts(posts)
    }

    override fun showUsers(users: List<AdminUser>) {
        userAdapter.updateUsers(users)
    }

    override fun showRestrictionRequests(requests: List<AdminRestrictionRequest>) {
        restrictionAdapter.updateRequests(requests)
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onLogoutCompleted() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showCreateUserDialog() {
        val username = EditText(this).apply { hint = "Username" }
        val email = EditText(this).apply { hint = "Email" }
        val password = EditText(this).apply { hint = "Password" }
        val displayName = EditText(this).apply { hint = "Display name" }
        val role = EditText(this).apply { hint = "Role (ROLE_USER / ROLE_MODERATOR / ROLE_ADMIN)"; setText("ROLE_USER") }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(username)
            addView(email)
            addView(password)
            addView(displayName)
            addView(role)
        }

        AlertDialog.Builder(this)
            .setTitle("Create User")
            .setView(layout)
            .setPositiveButton("Create") { _, _ ->
                // Validate inputs
                val usernameError = username.validateUsername()
                val emailError = email.validateEmail()
                val passwordError = password.validatePassword()
                val displayNameError = displayName.validateDisplayName()
                val roleError = role.text.toString().trim().validateRoleName()

                if (usernameError != null || emailError != null || passwordError != null || 
                    displayNameError != null || roleError != null) {
                    showError(usernameError ?: emailError ?: passwordError ?: displayNameError ?: roleError ?: "Validation error")
                    return@setPositiveButton
                }

                presenter.createUser(
                    username.text.toString().trim(),
                    email.text.toString().trim(),
                    password.text.toString().trim(),
                    displayName.text.toString().trim(),
                    role.text.toString().trim().normalizeRoleName()
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRestrictUserDialog(userId: Long) {
        val reason = EditText(this).apply { hint = "Reason" }
        val duration = EditText(this).apply { hint = "Duration days"; setText("7") }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(reason)
            addView(duration)
        }

        AlertDialog.Builder(this)
            .setTitle("Restrict User")
            .setView(layout)
            .setPositiveButton("Restrict") { _, _ ->
                val reasonError = reason.validateRestrictionReason()
                val durationError = duration.validateDurationDays()

                if (reasonError != null || durationError != null) {
                    showError(reasonError ?: durationError ?: "Validation error")
                    return@setPositiveButton
                }

                val parsedDuration = duration.text.toString().trim().toIntOrNull() ?: 7
                presenter.restrictUser(userId, reason.text.toString().trim(), parsedDuration)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRejectDialog(requestId: Long) {
        val reason = EditText(this).apply { hint = "Rejection reason (optional)" }

        AlertDialog.Builder(this)
            .setTitle("Reject Restriction Request")
            .setView(reason)
            .setPositiveButton("Reject") { _, _ ->
                presenter.rejectRestrictionRequest(requestId, reason.text.toString().trim())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        presenter.detach()
        presenter.destroy()
        super.onDestroy()
    }
}