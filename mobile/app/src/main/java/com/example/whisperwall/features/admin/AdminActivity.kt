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
import com.example.whisperwall.R
import com.example.whisperwall.core.di.AppContainer
import com.example.whisperwall.features.login.LoginActivity
import com.example.whisperwall.features.profile.ProfileActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AdminActivity : Activity() {
    private val appContainer by lazy { AppContainer.from(this) }
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var tvStats: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_feature)

        tvStats = findViewById(R.id.tvAdminStats)

        findViewById<Button>(R.id.btnAdminProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnAdminLogout).setOnClickListener {
            appContainer.sessionManager.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnRefreshStats).setOnClickListener {
            loadStats()
        }

        findViewById<Button>(R.id.btnCreateUser).setOnClickListener {
            showCreateUserDialog()
        }

        findViewById<Button>(R.id.btnDeleteUser).setOnClickListener {
            showDeleteUserDialog()
        }

        findViewById<Button>(R.id.btnRestrictUser).setOnClickListener {
            showRestrictUserDialog()
        }

        findViewById<Button>(R.id.btnDeletePost).setOnClickListener {
            showDeletePostDialog()
        }

        loadStats()
    }

    private fun loadStats() {
        activityScope.launch {
            val result = appContainer.adminRepository.getUsageStats()
            result.onSuccess { stats ->
                tvStats.text = buildString {
                    appendLine("Users: ${stats.totalUsers}")
                    appendLine("Posts: ${stats.totalPosts}")
                    appendLine("Comments: ${stats.totalComments}")
                    appendLine("Reports: ${stats.totalReports}")
                    appendLine("Active users: ${stats.activeUsers}")
                    appendLine("Restricted users: ${stats.restrictedUsers}")
                    appendLine("Admins: ${stats.adminUsers}")
                    appendLine("Moderators: ${stats.moderatorUsers}")
                }
            }.onFailure {
                tvStats.text = it.message ?: "Unable to load statistics."
            }
        }
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
                activityScope.launch {
                    val result = appContainer.adminRepository.createUser(
                        username.text.toString().trim(),
                        email.text.toString().trim(),
                        password.text.toString().trim(),
                        displayName.text.toString().trim(),
                        role.text.toString().trim().ifBlank { "ROLE_USER" }
                    )
                    Toast.makeText(this@AdminActivity, result.fold({ "User created" }, { it.message ?: "Unable to create user" }), Toast.LENGTH_SHORT).show()
                    loadStats()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteUserDialog() {
        val userId = EditText(this).apply { hint = "User ID" }
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setView(userId)
            .setPositiveButton("Delete") { _, _ ->
                val parsed = userId.text.toString().trim().toLongOrNull()
                if (parsed == null) {
                    Toast.makeText(this, "Enter a valid user ID", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                activityScope.launch {
                    val result = appContainer.adminRepository.deleteUser(parsed)
                    Toast.makeText(this@AdminActivity, result.fold({ "User deleted" }, { it.message ?: "Unable to delete user" }), Toast.LENGTH_SHORT).show()
                    loadStats()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRestrictUserDialog() {
        val userId = EditText(this).apply { hint = "User ID" }
        val reason = EditText(this).apply { hint = "Reason" }
        val duration = EditText(this).apply { hint = "Duration days"; setText("7") }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(userId)
            addView(reason)
            addView(duration)
        }

        AlertDialog.Builder(this)
            .setTitle("Restrict User")
            .setView(layout)
            .setPositiveButton("Restrict") { _, _ ->
                val parsedUserId = userId.text.toString().trim().toLongOrNull()
                val parsedDuration = duration.text.toString().trim().toIntOrNull() ?: 7
                if (parsedUserId == null || reason.text.toString().trim().isBlank()) {
                    Toast.makeText(this, "Enter user ID and reason", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                activityScope.launch {
                    val result = appContainer.adminRepository.restrictUser(parsedUserId, reason.text.toString().trim(), parsedDuration)
                    Toast.makeText(this@AdminActivity, result.fold({ "User restricted" }, { it.message ?: "Unable to restrict user" }), Toast.LENGTH_SHORT).show()
                    loadStats()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeletePostDialog() {
        val postId = EditText(this).apply { hint = "Post ID" }
        AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setView(postId)
            .setPositiveButton("Delete") { _, _ ->
                val parsed = postId.text.toString().trim().toLongOrNull()
                if (parsed == null) {
                    Toast.makeText(this, "Enter a valid post ID", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                activityScope.launch {
                    val result = appContainer.adminRepository.deletePost(parsed)
                    Toast.makeText(this@AdminActivity, result.fold({ "Post deleted" }, { it.message ?: "Unable to delete post" }), Toast.LENGTH_SHORT).show()
                    loadStats()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }
}