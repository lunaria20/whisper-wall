package com.example.whisperwall.features.home

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.example.whisperwall.R
import com.example.whisperwall.core.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ReportConfessionActivity : Activity() {
    private val appContainer by lazy { AppContainer.from(this) }
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var etDetails: EditText
    private lateinit var tvError: TextView
    private lateinit var btnSubmit: Button
    private val reasonButtons = mutableListOf<Button>()
    private var selectedReason = "Harassment or bullying"
    private var confessionId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_confession)

        confessionId = intent.getLongExtra(EXTRA_CONFESSION_ID, 0L)
        val content = intent.getStringExtra(EXTRA_CONFESSION_CONTENT).orEmpty()

        etDetails = findViewById(R.id.etDetails)
        tvError = findViewById(R.id.tvError)
        btnSubmit = findViewById(R.id.btnSubmitReport)

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvConfessionPreview).text = "\"${content.take(120)}${if (content.length > 120) "..." else ""}\""
        buildReasonButtons()

        btnSubmit.setOnClickListener { submitReport() }
    }

    private fun buildReasonButtons() {
        val list = findViewById<LinearLayout>(R.id.reasonList)
        val reasons = listOf(
            "Harassment or bullying",
            "Hate speech",
            "Violence or threats",
            "Spam or misleading",
            "Inappropriate content",
            "Other"
        )
        reasons.forEach { reason ->
            val button = Button(this).apply {
                text = reason
                isAllCaps = false
                textSize = 15f
                gravity = android.view.Gravity.CENTER_VERTICAL
                setTextColor(getColor(android.R.color.black))
                background = getDrawable(R.drawable.bg_option_button)
                minHeight = 0
                minimumHeight = 0
                setPadding(18, 0, 18, 0)
                isSelected = reason == selectedReason
                setOnClickListener {
                    selectedReason = reason
                    reasonButtons.forEach { it.isSelected = it.text == selectedReason }
                }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.report_reason_height)
            ).apply {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.report_reason_gap)
            }
            list.addView(button, params)
            reasonButtons.add(button)
        }
    }

    private fun submitReport() {
        if (confessionId <= 0L) {
            showError("Unable to identify this confession.")
            return
        }

        showError(null)
        btnSubmit.isEnabled = false
        btnSubmit.text = "Submitting..."
        activityScope.launch {
            val result = appContainer.reportRepository.submitReport(
                confessionId = confessionId,
                reason = selectedReason.sanitizeReportReason(),
                details = etDetails.text.toString().trim()
            )
            btnSubmit.isEnabled = true
            btnSubmit.text = "Submit Report"
            result.onSuccess {
                setResult(RESULT_OK)
                finish()
            }.onFailure {
                showError(it.message ?: "Unable to submit report.")
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

    companion object {
        const val EXTRA_CONFESSION_ID = "extra_confession_id"
        const val EXTRA_CONFESSION_CONTENT = "extra_confession_content"
    }
}
