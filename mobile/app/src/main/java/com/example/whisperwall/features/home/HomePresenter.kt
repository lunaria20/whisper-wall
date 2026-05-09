package com.example.whisperwall.features.home

import com.example.whisperwall.core.repository.ConfessionRepository
import com.example.whisperwall.core.repository.ReportRepository
import com.example.whisperwall.core.session.SessionManager
import com.example.whisperwall.features.common.TimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class HomePresenter(
    private val confessionRepository: ConfessionRepository,
    private val reportRepository: ReportRepository,
    private val sessionManager: SessionManager
) : HomeContract.Presenter {

    private var view: HomeContract.View? = null
    private var items: List<HomeContract.ConfessionItem> = emptyList()
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun attach(view: HomeContract.View) {
        this.view = view
        view.showWelcome(sessionManager.username.ifBlank { "User" })
    }

    override fun detach() {
        this.view = null
    }

    override fun loadFeed() {
        presenterScope.launch {
            val feedResult = confessionRepository.loadFeed(sessionManager.username, sessionManager.userId)
            feedResult.onSuccess { confessions ->
                val likedIds = confessionRepository.getLikedConfessionIds(confessions, sessionManager.userId, sessionManager.username)
                items = confessions.map { confession ->
                    HomeContract.ConfessionItem(
                        id = confession.id,
                        category = confession.category,
                        content = confession.content,
                        likes = confession.reactionCount,
                        time = TimeFormatter.relativeTime(confession.createdAt),
                        isOwn = confession.username == sessionManager.username,
                        likedByMe = likedIds.contains(confession.id)
                    )
                }
                view?.showFeed(items)
                view?.showError(null)
            }.onFailure {
                view?.showError(it.message ?: "Unable to load confessions.")
            }
        }
    }

    override fun onToggleLike(item: HomeContract.ConfessionItem) {
        val original = items
        items = items.map {
            if (it.id == item.id) {
                val nowLiked = !it.likedByMe
                it.copy(
                    likedByMe = nowLiked,
                    likes = if (nowLiked) it.likes + 1 else (it.likes - 1).coerceAtLeast(0)
                )
            } else it
        }
        view?.showFeed(items)

        presenterScope.launch {
            val result = confessionRepository.toggleLike(item.id, item.likedByMe)
            result.onFailure {
                items = original
                view?.showFeed(items)
                view?.showError(it.message ?: "Unable to update reaction.")
            }
        }
    }

    override fun onDeleteConfession(itemId: Long) {
        presenterScope.launch {
            val result = confessionRepository.deleteConfession(itemId)
            result.onSuccess {
                items = items.filterNot { it.id == itemId }
                view?.showFeed(items)
            }.onFailure {
                view?.showError(it.message ?: "Failed to delete confession.")
            }
        }
    }

    override fun onPostConfession(content: String, category: String) {
        presenterScope.launch {
            val result = confessionRepository.postConfession(content, category, sessionManager.username)
            result.onSuccess { confession ->
                val newItem = HomeContract.ConfessionItem(
                    id = confession.id,
                    category = confession.category,
                    content = confession.content,
                    likes = confession.reactionCount,
                    time = TimeFormatter.relativeTime(confession.createdAt),
                    isOwn = confession.username == sessionManager.username,
                    likedByMe = false
                )
                items = listOf(newItem) + items.filterNot { it.id == newItem.id }
                view?.showFeed(items)
                view?.showMessage("Confession posted")
            }.onFailure {
                view?.showError(it.message ?: "Unable to post confession.")
            }
        }
    }

    override fun onReportConfession(confessionId: Long, reason: String) {
        presenterScope.launch {
            val result = reportRepository.submitReport(confessionId, reason)
            result.onSuccess {
                view?.showMessage("Report submitted")
            }.onFailure {
                view?.showError(it.message ?: "Unable to submit report.")
            }
        }
    }

    override fun onLogout() {
        sessionManager.clear()
    }

    fun destroy() {
        presenterScope.cancel()
    }
}
