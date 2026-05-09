import { api } from "../../../shared/services/apiService";
import { IconHeart } from "../../../shared/components/Icons";

/**
 * ReactionsPage - Handles reactions (likes) on confessions
 * Manages liked state and provides toggle function for liking/unliking confessions
 */
export const ReactionsPage = {
  /**
   * Initialize reactions state and load liked confessions
   * @param {Array} confessions - List of confessions
   * @param {Number} userId - Current user ID
   * @param {String} username - Current username
   * @returns {Promise<Array>} Array of liked confession IDs
   */
  loadReactions: async (confessions, userId, username) => {
    if (confessions.length === 0) return [];

    const reactionResults = await Promise.all(
      confessions.map((item) => {
        const cleanId = item?.id;
        return cleanId != null
          ? api.get(`/reactions/confession/${cleanId}`)
          : Promise.resolve({ ok: false, data: [] });
      })
    );

    const liked = [];
    confessions.forEach((item, idx) => {
      const result = reactionResults[idx];
      const reactions = result.ok && Array.isArray(result.data) ? result.data : [];
      
      // Update the confession with reaction count
      item.likes = reactions.length;
      
      const currentUserLiked = reactions.some(
        (reaction) =>
          (userId && String(reaction?.userId) === String(userId)) ||
          (username && reaction?.username === username)
      );

      if (currentUserLiked) {
        liked.push(item.id);
      }
    });
    return liked;
  },

  /**
   * Toggle like/unlike for a confession
   * @param {Number} id - Confession ID
   * @param {Boolean} wasLiked - Whether confession was already liked
   * @param {Function} setLikedIds - State setter for liked IDs
   * @param {Function} setConfessions - State setter for confessions
   * @param {Function} setError - State setter for error messages
   */
  toggleLike: async (id, wasLiked, setLikedIds, setConfessions, setError) => {
    // Optimistic update
    setLikedIds((prev) => (wasLiked ? prev.filter((x) => x !== id) : [...prev, id]));
    setConfessions((prev) =>
      prev.map((c) =>
        c.id === id ? { ...c, likes: wasLiked ? c.likes - 1 : c.likes + 1 } : c
      )
    );

    try {
      const result = wasLiked
        ? await api.delete(`/reactions/confession/${id}`)
        : await api.post(`/reactions/confession/${id}`, { reactionType: "LIKE" });

      if (!result.ok) {
        // Rollback on error
        setLikedIds((prev) =>
          wasLiked ? [...prev, id] : prev.filter((x) => x !== id)
        );
        setConfessions((prev) =>
          prev.map((c) =>
            c.id === id ? { ...c, likes: wasLiked ? c.likes + 1 : c.likes - 1 } : c
          )
        );
        setError("Unable to update reaction. Please try again.");
      }
    } catch (err) {
      // Rollback on error
      setLikedIds((prev) =>
        wasLiked ? [...prev, id] : prev.filter((x) => x !== id)
      );
      setConfessions((prev) =>
        prev.map((c) =>
          c.id === id ? { ...c, likes: wasLiked ? c.likes + 1 : c.likes - 1 } : c
        )
      );
      setError("Unable to update reaction. Please try again.");
      console.error("Error submitting reaction:", err);
    }
  },

  /**
   * React component to render like button
   */
  LikeButton: ({ confessionId, liked, likeCount, onToggleLike }) => (
    <button
      className={`footer-btn${liked ? " liked" : ""}`}
      onClick={() => onToggleLike(confessionId)}
    >
      <IconHeart filled={liked} />
      {likeCount}
    </button>
  ),
};

export default ReactionsPage;
