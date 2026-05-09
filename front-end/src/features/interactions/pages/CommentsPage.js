import { api } from "../../../shared/services/apiService";
import { IconComment } from "../../../shared/components/Icons";

/**
 * CommentsPage - Handles comments on confessions
 * Manages comment display, posting, and deletion
 */
export const CommentsPage = {
  /**
   * Load comments for a confession
   * @param {Number} confessionId - ID of the confession
   * @param {Function} setExpandedCommentId - State setter for expanded comment section
   * @param {Function} setCommentsMap - State setter for comments data
   * @param {Object} commentsMap - Current comments map
   */
  loadComments: async (confessionId, setExpandedCommentId, setCommentsMap, commentsMap) => {
    if (Object.keys(commentsMap).includes(String(confessionId))) {
      // If already loaded, just toggle visibility
      setExpandedCommentId((prev) => (prev === confessionId ? null : confessionId));
      return;
    }

    setExpandedCommentId(confessionId);
    const result = await api.get(`/comments/confession/${confessionId}?page=0&size=20`);
    const comments =
      result.ok && Array.isArray(result.data?.content) ? result.data.content : [];
    setCommentsMap((prev) => ({ ...prev, [confessionId]: comments }));
  },

  /**
   * Delete a comment
   * @param {Number} commentId - ID of the comment to delete
   * @param {Number} confessionId - ID of the parent confession
   * @param {Function} setCommentsMap - State setter for comments data
   */
  deleteComment: async (commentId, confessionId, setCommentsMap) => {
    if (!window.confirm("Delete this comment?")) return;
    const result = await api.delete(`/comments/${commentId}`);
    if (result.ok) {
      setCommentsMap((prev) => ({
        ...prev,
        [confessionId]: (prev[confessionId] || []).filter((cm) => cm.id !== commentId),
      }));
    }
  },

  /**
   * Submit a new comment
   * @param {Number} confessionId - ID of the confession
   * @param {String} commentText - Comment text to submit
   * @param {Function} setCommentDraft - State setter for comment drafts
   * @param {Function} setCommentLoading - State setter for loading state
   * @param {Function} setCommentsMap - State setter for comments data
   */
  submitComment: async (
    confessionId,
    commentText,
    setCommentDraft,
    setCommentLoading,
    setCommentsMap
  ) => {
    const text = (commentText || "").trim();
    if (!text) return;

    setCommentLoading((prev) => ({ ...prev, [confessionId]: true }));
    const result = await api.post(`/comments/confession/${confessionId}`, {
      content: text,
    });
    setCommentLoading((prev) => ({ ...prev, [confessionId]: false }));

    if (result.ok) {
      setCommentDraft((prev) => ({ ...prev, [confessionId]: "" }));
      // Refresh comments list
      const refreshed = await api.get(`/comments/confession/${confessionId}?page=0&size=20`);
      const comments =
        refreshed.ok && Array.isArray(refreshed.data?.content)
          ? refreshed.data.content
          : [];
      setCommentsMap((prev) => ({ ...prev, [confessionId]: comments }));
    }
  },

  /**
   * React component to render comments section
   */
  CommentButton: ({ confessionId, commentCount, onLoadComments }) => (
    <button className="footer-btn" onClick={() => onLoadComments(confessionId)}>
      <IconComment />
      {commentCount}
    </button>
  ),

  /**
   * React component to render comments list and input
   */
  CommentSection: ({
    confessionId,
    comments,
    userId,
    commentDraft,
    commentLoading,
    onChangeCommentDraft,
    onSubmitComment,
    onDeleteComment,
  }) => (
    <div
      style={{
        borderTop: "1px solid var(--border)",
        marginTop: "12px",
        paddingTop: "12px",
      }}
    >
      {(comments || []).length === 0 ? (
        <p
          style={{
            fontSize: "13px",
            color: "var(--text-muted)",
            marginBottom: "10px",
          }}
        >
          No comments yet. Be the first!
        </p>
      ) : (
        (comments || []).map((cm) => (
          <div
            key={cm.id}
            style={{
              marginBottom: "10px",
              padding: "8px 10px",
              background: "var(--bg, #F7F5FF)",
              borderRadius: "8px",
            }}
          >
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: "2px",
              }}
            >
              <p
                style={{
                  fontSize: "12px",
                  fontWeight: 600,
                  color: "var(--text-muted)",
                  margin: 0,
                }}
              >
                {cm.displayName || cm.username || "Anonymous"}
              </p>
              {String(cm.userId) === String(userId) && (
                <button
                  onClick={() => onDeleteComment(cm.id, confessionId)}
                  style={{
                    background: "none",
                    border: "none",
                    cursor: "pointer",
                    fontSize: "16px",
                    color: "#aaa",
                    padding: "2px 6px",
                    lineHeight: 1,
                  }}
                  title="Delete comment"
                >
                  🗑
                </button>
              )}
            </div>
            <p style={{ fontSize: "14px", margin: 0 }}>{cm.content}</p>
          </div>
        ))
      )}
      <div style={{ display: "flex", gap: "8px", marginTop: "8px" }}>
        <input
          style={{
            flex: 1,
            height: "36px",
            borderRadius: "8px",
            border: "1.5px solid var(--border)",
            padding: "0 10px",
            fontSize: "14px",
            fontFamily: "inherit",
          }}
          placeholder="Write a comment..."
          value={commentDraft || ""}
          onChange={(e) => onChangeCommentDraft(confessionId, e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && onSubmitComment(confessionId)}
        />
        <button
          onClick={() => onSubmitComment(confessionId)}
          disabled={commentLoading || !(commentDraft || "").trim()}
          style={{
            padding: "0 14px",
            borderRadius: "8px",
            border: "none",
            background: "var(--primary, #7C3AED)",
            color: "#fff",
            fontSize: "13px",
            cursor: "pointer",
            fontFamily: "inherit",
          }}
        >
          {commentLoading ? "..." : "Send"}
        </button>
      </div>
    </div>
  ),
};

export default CommentsPage;
