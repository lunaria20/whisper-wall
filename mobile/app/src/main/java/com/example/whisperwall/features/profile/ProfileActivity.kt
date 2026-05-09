package com.example.whisperwall.features.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.app.Activity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.whisperwall.R
import com.example.whisperwall.core.di.AppContainer
import com.example.whisperwall.core.model.UserProfile
import com.example.whisperwall.core.storage.ProfileImageStorage
import com.example.whisperwall.core.storage.StorageMode
import com.example.whisperwall.features.admin.AdminActivity
import com.example.whisperwall.features.home.HomeActivity
import com.example.whisperwall.features.login.LoginActivity
import com.example.whisperwall.features.moderator.ModeratorActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ProfileActivity : Activity(), ProfileContract.View {
    private val appContainer by lazy { AppContainer.from(this) }
    private lateinit var presenter: ProfilePresenter

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etBio: EditText
    private lateinit var ivProfilePicture: ImageView
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnPickProfilePicture: Button
    private lateinit var tvStatus: TextView

    private var profilePictureValue: String = ""
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_feature)

        presenter = ProfilePresenter(appContainer.userRepository, appContainer.sessionManager)
        presenter.attach(this)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etBio = findViewById(R.id.etBio)
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        btnPickProfilePicture = findViewById(R.id.btnPickProfilePicture)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        tvStatus = findViewById(R.id.tvStatus)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            navigateToRoleHome()
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            presenter.logout()
            onLogoutCompleted()
        }

        btnPickProfilePicture.setOnClickListener {
            pickProfilePicture()
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val bio = etBio.text.toString()

            val usernameError = etUsername.validateUsername()
            val emailError = etEmail.validateEmail()
            if (usernameError != null) {
                showStatus(usernameError)
                return@setOnClickListener
            }
            if (emailError != null) {
                showStatus(emailError)
                return@setOnClickListener
            }
            presenter.saveProfile(username, email, bio, profilePictureValue)
        }

        findViewById<Button>(R.id.btnChangePassword).setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            when {
                currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() -> {
                    showStatus("Please fill out all password fields.")
                }
                etNewPassword.validateNewPassword() != null -> {
                    showStatus(etNewPassword.validateNewPassword() ?: "Invalid password")
                }
                newPassword != confirmPassword -> {
                    showStatus("New password and confirmation do not match.")
                }
                else -> {
                    presenter.changePassword(currentPassword, newPassword)
                }
            }
        }

        presenter.loadProfile()
    }

    override fun showProfile(profile: UserProfile) {
        etUsername.setText(profile.username)
        etEmail.setText(profile.email)
        etBio.setText(profile.bio)
        profilePictureValue = profile.profilePicture
        renderProfilePicture(profile.profilePicture)
    }

    override fun showStatus(message: String, success: Boolean) {
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = message
        tvStatus.setTextColor(if (success) 0xFF1B7A2A.toInt() else 0xFFB3261E.toInt())
        if (success) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            etCurrentPassword.setText("")
            etNewPassword.setText("")
            etConfirmPassword.setText("")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_IMAGE_REQUEST || resultCode != RESULT_OK) return

        val imageUri = data?.data ?: return
        activityScope.launch {
            try {
                showStatus("Uploading photo...", true)
                val result = ProfileImageStorage.prepareProfileImage(this@ProfileActivity, imageUri, etUsername.text.toString().trim())
                profilePictureValue = result.storedValue
                ivProfilePicture.setImageBitmap(result.previewBitmap)

                val message = if (result.storageMode == StorageMode.SUPABASE) {
                    "Photo uploaded to cloud storage."
                } else {
                    "Photo saved locally as base64."
                }
                showStatus(message, true)
            } catch (error: Exception) {
                showStatus(error.message ?: "Unable to process the selected image.")
            }
        }
    }

    override fun onSessionExpired() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onLogoutCompleted() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        presenter.detach()
        presenter.destroy()
        activityScope.cancel()
        super.onDestroy()
    }

    private fun navigateToRoleHome() {
        val role = appContainer.sessionManager.role
        val intent = if (role.equals("ADMIN", ignoreCase = true) || role.equals("ROLE_ADMIN", ignoreCase = true)) {
            Intent(this, AdminActivity::class.java)
        } else if (role.equals("MODERATOR", ignoreCase = true) || role.equals("ROLE_MODERATOR", ignoreCase = true)) {
            Intent(this, ModeratorActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun pickProfilePicture() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "Choose a profile photo"), PICK_IMAGE_REQUEST)
    }

    private fun renderProfilePicture(value: String) {
        if (value.isBlank()) {
            ivProfilePicture.setImageDrawable(null)
            ivProfilePicture.setBackgroundResource(R.drawable.bg_input_field)
            return
        }

        if (value.startsWith("data:image", ignoreCase = true)) {
            activityScope.launch {
                loadBase64Preview(value)
            }
            return
        }

        if (value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true)) {
            activityScope.launch {
                loadRemotePreview(value)
            }
            return
        }

        ivProfilePicture.setImageDrawable(null)
        ivProfilePicture.setBackgroundResource(R.drawable.bg_input_field)
    }

    private fun loadBase64Preview(value: String) {
        val base64Part = value.substringAfter("base64,", "")
        if (base64Part.isBlank()) {
            ivProfilePicture.setImageDrawable(null)
            ivProfilePicture.setBackgroundResource(R.drawable.bg_input_field)
            return
        }

        val bytes = android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (bitmap != null) {
            ivProfilePicture.setImageBitmap(bitmap)
        }
    }

    private suspend fun loadRemotePreview(value: String) {
        val bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                java.net.URL(value).openStream().use { input ->
                    android.graphics.BitmapFactory.decodeStream(input)
                }
            }.getOrNull()
        }

        if (bitmap != null) {
            ivProfilePicture.setImageBitmap(bitmap)
        } else {
            ivProfilePicture.setImageDrawable(null)
            ivProfilePicture.setBackgroundResource(R.drawable.bg_input_field)
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 2001
    }
}
