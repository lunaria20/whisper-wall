package com.example.whisperwall.features.moderator

import com.example.whisperwall.core.repository.ModeratorRepository
import com.example.whisperwall.core.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ModeratorPresenter(
    private val moderatorRepository: ModeratorRepository,
    private val sessionManager: SessionManager
) : ModeratorContract.Presenter {

    private var view: ModeratorContract.View? = null
    private var currentStatus = "PENDING"
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun attach(view: ModeratorContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
    }

    override fun selectStatus(status: String) {
        currentStatus = status.normalizeReportStatus()
        presenterScope.launch {
            val result = moderatorRepository.getReports(currentStatus)
            result.onSuccess {
                view?.showReports(it)
                view?.showStatus(null)
            }.onFailure {
                view?.showReports(emptyList())
                view?.showStatus(it.message ?: "Unable to load reports.")
            }
        }
    }

    override fun dismissReport(reportId: Long) {
        presenterScope.launch {
            val result = moderatorRepository.dismiss(reportId)
            result.onSuccess {
                view?.showMessage("Report dismissed")
                selectStatus(currentStatus)
            }.onFailure {
                view?.showStatus(it.message ?: "Failed to dismiss report.")
            }
        }
    }

    override fun removeConfession(reportId: Long) {
        presenterScope.launch {
            val result = moderatorRepository.removeConfession(reportId)
            result.onSuccess {
                view?.showMessage("Confession removed")
                selectStatus(currentStatus)
            }.onFailure {
                view?.showStatus(it.message ?: "Failed to remove confession.")
            }
        }
    }

    override fun sendRestrictionRequest(userId: Long, reason: String, durationDays: Int) {
        presenterScope.launch {
            val result = moderatorRepository.sendRestrictionRequest(userId, reason, durationDays)
            result.onSuccess {
                view?.showMessage("Restriction request sent")
            }.onFailure {
                view?.showStatus(it.message ?: "Failed to send restriction request.")
            }
        }
    }

    override fun logout() {
        sessionManager.clear()
        view?.onLogoutCompleted()
    }

    fun destroy() {
        presenterScope.cancel()
    }
}
