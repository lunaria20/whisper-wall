package com.example.whisperwall.features.admin

import com.example.whisperwall.core.repository.AdminRepository
import com.example.whisperwall.core.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AdminPresenter(
    private val adminRepository: AdminRepository,
    private val sessionManager: SessionManager
) : AdminContract.Presenter {

    private var view: AdminContract.View? = null
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun attach(view: AdminContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
    }

    override fun loadStats() {
        presenterScope.launch {
            val result = adminRepository.getUsageStats()
            result.onSuccess { stats ->
                view?.showStats(stats)
            }.onFailure {
                view?.showError(it.message ?: "Unable to load statistics.")
            }
        }
    }

    override fun loadPosts(page: Int, size: Int) {
        presenterScope.launch {
            val result = adminRepository.getAllPosts(page, size)
            result.onSuccess { posts ->
                view?.showPosts(posts)
            }.onFailure {
                view?.showError(it.message ?: "Unable to load posts.")
            }
        }
    }

    override fun loadUsers(page: Int, size: Int) {
        presenterScope.launch {
            val result = adminRepository.getAllUsers(page, size)
            result.onSuccess { users ->
                view?.showUsers(users)
            }.onFailure {
                view?.showError(it.message ?: "Unable to load users.")
            }
        }
    }

    override fun loadRestrictionRequests(page: Int, size: Int) {
        presenterScope.launch {
            val result = adminRepository.getPendingRestrictionRequests(page, size)
            result.onSuccess { requests ->
                view?.showRestrictionRequests(requests)
            }.onFailure {
                view?.showError(it.message ?: "Unable to load restriction requests.")
            }
        }
    }

    override fun deletePost(postId: Long) {
        presenterScope.launch {
            val result = adminRepository.deletePost(postId)
            result.onSuccess {
                view?.showMessage("Post deleted successfully")
                loadPosts()
            }.onFailure {
                view?.showError(it.message ?: "Unable to delete post.")
            }
        }
    }

    override fun deleteUser(userId: Long) {
        presenterScope.launch {
            val result = adminRepository.deleteUser(userId)
            result.onSuccess {
                view?.showMessage("User deleted successfully")
                loadUsers()
            }.onFailure {
                view?.showError(it.message ?: "Unable to delete user.")
            }
        }
    }

    override fun createUser(username: String, email: String, password: String, displayName: String, roleName: String) {
        presenterScope.launch {
            val result = adminRepository.createUser(username, email, password, displayName, roleName)
            result.onSuccess {
                view?.showMessage("User created successfully")
                loadUsers()
            }.onFailure {
                view?.showError(it.message ?: "Unable to create user.")
            }
        }
    }

    override fun restrictUser(userId: Long, reason: String, durationDays: Int) {
        presenterScope.launch {
            val result = adminRepository.restrictUser(userId, reason, durationDays)
            result.onSuccess {
                view?.showMessage("User restricted successfully")
                loadUsers()
            }.onFailure {
                view?.showError(it.message ?: "Unable to restrict user.")
            }
        }
    }

    override fun unrestrictUser(userId: Long) {
        presenterScope.launch {
            val result = adminRepository.unrestrictUser(userId)
            result.onSuccess {
                view?.showMessage("User unrestricted successfully")
                loadUsers()
            }.onFailure {
                view?.showError(it.message ?: "Unable to unrestrict user.")
            }
        }
    }

    override fun approveRestrictionRequest(requestId: Long) {
        presenterScope.launch {
            val result = adminRepository.approveRestrictionRequest(requestId)
            result.onSuccess {
                view?.showMessage("Restriction request approved")
                loadRestrictionRequests()
            }.onFailure {
                view?.showError(it.message ?: "Unable to approve restriction.")
            }
        }
    }

    override fun rejectRestrictionRequest(requestId: Long, reason: String) {
        presenterScope.launch {
            val result = adminRepository.rejectRestrictionRequest(requestId, reason)
            result.onSuccess {
                view?.showMessage("Restriction request rejected")
                loadRestrictionRequests()
            }.onFailure {
                view?.showError(it.message ?: "Unable to reject restriction.")
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
