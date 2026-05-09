package com.example.whisperwall.features.moderator

import com.example.whisperwall.core.model.Report

interface ModeratorContract {
    interface View {
        fun showReports(reports: List<Report>)
        fun showStatus(message: String?)
        fun showMessage(message: String)
        fun onLogoutCompleted()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun selectStatus(status: String)
        fun dismissReport(reportId: Long)
        fun removeConfession(reportId: Long)
        fun sendRestrictionRequest(userId: Long, reason: String, durationDays: Int)
        fun logout()
    }
}
