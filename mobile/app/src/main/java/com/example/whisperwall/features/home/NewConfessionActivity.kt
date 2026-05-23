package com.example.whisperwall.features.home

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import com.example.whisperwall.R
import com.example.whisperwall.core.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NewConfessionActivity : Activity() {
    private val appContainer by lazy { AppContainer.from(this) }
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var etContent: EditText
    private lateinit var tvCharacterCount: TextView
    private lateinit var tvError: TextView
    private lateinit var btnSubmit: Button
    private val categoryButtons = mutableListOf<Button>()
    private var selectedCategory = "Other"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_confession)

        etContent = findViewById(R.id.etContent)
        tvCharacterCount = findViewById(R.id.tvCharacterCount)
        tvError = findViewById(R.id.tvError)
        btnSubmit = findViewById(R.id.btnSubmit)

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
        buildCategoryButtons()
        wireCharacterCount()

        btnSubmit.setOnClickListener { submitConfession() }
    }

    private fun buildCategoryButtons() {
        val grid = findViewById<GridLayout>(R.id.categoryGrid)
        val categories = listOf("Mental Health", "Relationships", "Personal", "School", "Personal Growth", "Other")
        categories.forEach { category ->
            val button = Button(this).apply {
                text = category
                isAllCaps = false
                textSize = 14f
                setTextColor(getColor(android.R.color.black))
                background = getDrawable(R.drawable.bg_option_button)
                minHeight = 0
                minimumHeight = 0
                setPadding(8, 0, 8, 0)
                isSelected = category == selectedCategory
                setOnClickListener {
                    selectedCategory = category
                    categoryButtons.forEach { it.isSelected = it.text == selectedCategory }
                }
            }
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = resources.getDimensionPixelSize(R.dimen.category_button_height)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(0, 0, resources.getDimensionPixelSize(R.dimen.category_button_gap), resources.getDimensionPixelSize(R.dimen.category_button_gap))
            }
            grid.addView(button, params)
            categoryButtons.add(button)
        }
    }

    private fun wireCharacterCount() {
        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvCharacterCount.text = "${s?.length ?: 0}/500 characters"
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun submitConfession() {
        val content = etContent.text.toString().trim()
        val error = content.validateConfessionText()
        if (error != null) {
            showError(error)
            return
        }

        showError(null)
        btnSubmit.isEnabled = false
        btnSubmit.text = "..."
        activityScope.launch {
            val result = appContainer.confessionRepository.postConfession(
                content = content,
                category = selectedCategory,
                currentUsername = appContainer.sessionManager.username
            )
            btnSubmit.isEnabled = true
            btnSubmit.text = "Send"
            result.onSuccess {
                setResult(RESULT_OK)
                finish()
            }.onFailure {
                showError(it.message ?: "Unable to post confession.")
            }
        }
    }

    private fun showError(message: String?) {
        tvError.text = message.orEmpty()
        tvError.visibility = if (message.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }
}
