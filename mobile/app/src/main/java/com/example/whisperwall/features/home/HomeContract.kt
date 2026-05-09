package com.example.whisperwall.features.home

interface HomeContract {
    data class ConfessionItem(
        val id: Long,
        val category: String,
        val content: String,
        val likes: Int,
        val time: String,
        val isOwn: Boolean,
        val likedByMe: Boolean
    )

    interface View {
        fun showWelcome(username: String)
        fun showFeed(items: List<ConfessionItem>)
        fun showError(message: String?)
        fun showMessage(message: String)
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun loadFeed()
        fun onToggleLike(item: ConfessionItem)
        fun onDeleteConfession(itemId: Long)
        fun onPostConfession(content: String, category: String)
        fun onReportConfession(confessionId: Long, reason: String)
        fun onLogout()
    }
}
