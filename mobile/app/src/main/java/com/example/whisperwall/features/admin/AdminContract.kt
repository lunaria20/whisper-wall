package com.example.whisperwall.features.admin

import com.example.whisperwall.core.repository.AdminPost
import com.example.whisperwall.core.repository.AdminUser
import com.example.whisperwall.core.repository.AdminRestrictionRequest
import com.example.whisperwall.core.repository.AdminUsageStats

interface AdminContract {
    interface View {
        fun showStats(stats: AdminUsageStats)
        fun showPosts(posts: List<AdminPost>)
        fun showUsers(users: List<AdminUser>)
        fun showRestrictionRequests(requests: List<AdminRestrictionRequest>)
        fun showMessage(message: String)
        fun showError(message: String)
        fun onLogoutCompleted()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun loadStats()
        fun loadPosts(page: Int = 0, size: Int = 20)
        fun loadUsers(page: Int = 0, size: Int = 50)
        fun loadRestrictionRequests(page: Int = 0, size: Int = 20)
        fun deletePost(postId: Long)
        fun deleteUser(userId: Long)
        fun createUser(username: String, email: String, password: String, displayName: String, roleName: String)
        fun restrictUser(userId: Long, reason: String, durationDays: Int)
        fun unrestrictUser(userId: Long)
        fun approveRestrictionRequest(requestId: Long)
        fun rejectRestrictionRequest(requestId: Long, reason: String = "")
        fun logout()
    }
}
