import { useState, useEffect } from "react";
import { api, parseError } from "../../../shared/services/apiService";
import { mapConfessionToCard, mergeConfessionLists } from "../../../shared/utils/formatUtils";
import { LogoMark } from "../../../shared/components/LogoMark";
import { IconFlag, IconPlus } from "../../../shared/components/Icons";
import { ReactionsPage } from "../../interactions/pages/ReactionsPage";
import { CommentsPage } from "../../interactions/pages/CommentsPage";
import "../styles/Home.css";

function HomePage({ username, token, role, userId, profilePicture, onLogout, onGoProfile }) {
  const [confessions, setConfessions] = useState([]);
  const [likedIds, setLikedIds] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);
  const [reportingConfessionId, setReportingConfessionId] = useState(null);
  const [reportReason, setReportReason] = useState("");
  const [reportComment, setReportComment] = useState("");
  const [draft, setDraft] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("Personal");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [postingBlocked, setPostingBlocked] = useState(false);
  const [reportError, setReportError] = useState("");
  const [reportSuccess, setReportSuccess] = useState("");
  const [expandedCommentId, setExpandedCommentId] = useState(null);
  const [commentsMap, setCommentsMap] = useState({});
  const [commentDraft, setCommentDraft] = useState({});
  const [commentLoading, setCommentLoading] = useState({});

  const confessionCategories = ["Mental Health", "Relationships", "Personal", "Academic", "Other"];

  const handleLogout = () => {
    localStorage.removeItem("ww_token");
    localStorage.removeItem("ww_user_email");
    localStorage.removeItem("ww_user_role");
    localStorage.removeItem("ww_username");
    localStorage.removeItem("ww_profile_picture");
    onLogout();
  };

  useEffect(() => {
    const loadConfessions = async () => {
      const [publicResult, mineResult] = await Promise.all([
        api.get("/confessions/public?page=0&size=50"),
        userId ? api.get(`/confessions/user/${userId}?page=0&size=50`) : Promise.resolve({ ok: true, status: 200, data: { content: [] } }),
      ]);

      const publicContent = publicResult.ok && Array.isArray(publicResult.data?.content)
        ? publicResult.data.content
        : [];
      const ownContent = mineResult.ok && Array.isArray(mineResult.data?.content)
        ? mineResult.data.content
        : [];

      const merged = mergeConfessionLists(ownContent, publicContent);

      // Load reactions BEFORE mapping to cards so likes count is included
      if (merged.length > 0) {
        const liked = await ReactionsPage.loadReactions(merged, userId, username);
        setLikedIds(liked);
      } else {
        setLikedIds([]);
      }

      // Now map to cards with likes count populated
      setConfessions(merged.map((item) => mapConfessionToCard(item, username)));

      if (!publicResult.ok && !mineResult.ok) {
        if (publicResult.status !== 401 && mineResult.status !== 401) {
          setError(parseError(publicResult.data || mineResult.data, publicResult.status || mineResult.status));
        }
        return;
      }

      setError("");
    };

    loadConfessions();
  }, [username, userId]);

  const toggleLike = async (id) => {
    const wasLiked = likedIds.includes(id);
    ReactionsPage.toggleLike(id, wasLiked, setLikedIds, setConfessions, setError);
  };

  const postConfession = async () => {
    if (!draft.trim()) {
      setError("Confession cannot be empty.");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const { ok, status, data } = await api.post("/confessions", {
        content: draft.trim(),
        category: selectedCategory,
      });

      if (ok) {
        const newConfession = mapConfessionToCard(
          {
            ...data,
            content: data?.content || draft.trim(),
            category: data?.category || selectedCategory,
            username,
            reactionCount: data?.reactionCount || 0,
            createdAt: data?.createdAt || new Date().toISOString(),
          },
          username
        );
        setConfessions((prev) => [newConfession, ...prev]);
        setDraft("");
        setShowModal(false);
        setPostingBlocked(false);
      } else {
        if (status === 403 || data?.error?.code === "CONF-001") {
          setPostingBlocked(true);
          setError("Your account is temporarily restricted from posting. Please contact support.");
        } else {
          setError(parseError(data, status));
        }
      }
    } catch (err) {
      setError("Unable to post confession. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteConfession = async (id) => {
    if (!window.confirm("Are you sure you want to delete this confession?")) return;

    try {
      const { ok } = await api.delete(`/confessions/${id}`);
      if (ok) {
        setConfessions((prev) => prev.filter((c) => c.id !== id));
      } else {
        setError("Failed to delete confession.");
      }
    } catch (err) {
      setError("Unable to delete confession. Please try again.");
    }
  };

  const submitReport = async () => {
    if (!reportingConfessionId) {
      setReportError("No confession selected for reporting.");
      return;
    }

    if (!reportReason.trim()) {
      setReportError("Please select or provide a reason.");
      return;
    }

    if (reportReason === "Other" && !reportComment.trim()) {
      setReportError("Please provide details for your report.");
      return;
    }

    setLoading(true);
    setReportError("");

    try {
      const { ok, status, data } = await api.post(`/reports/confession/${reportingConfessionId}`, {
        reason: reportReason,
        description: reportReason === "Other" ? reportComment : "",
      });

      if (ok) {
        setReportSuccess("✓ Report submitted successfully.");
        setTimeout(() => {
          setShowReportModal(false);
          setReportingConfessionId(null);
          setReportReason("");
          setReportComment("");
          setReportSuccess("");
        }, 2000);
      } else {
        if (status === 409 || data?.error?.code === "REPORT-001") {
          setReportError("You have already reported this confession.");
        } else {
          setReportError(parseError(data, status));
        }
      }
    } catch (err) {
      setReportError("Unable to submit report. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const loadComments = async (confessionId) => {
    CommentsPage.loadComments(confessionId, setExpandedCommentId, setCommentsMap, commentsMap);
  };

  const deleteComment = async (commentId, confessionId) => {
    CommentsPage.deleteComment(commentId, confessionId, setCommentsMap);
  };

  const submitComment = async (confessionId) => {
    CommentsPage.submitComment(
      confessionId,
      commentDraft[confessionId],
      setCommentDraft,
      setCommentLoading,
      setCommentsMap
    );
  };

  return (
    <div className="home-root">
      <div className="home-wrap">
        <div className="navbar">
          <div className="nav-logo"><LogoMark /></div>
          <span className="nav-title">WhisperWall</span>
          <button className="nav-user" onClick={onGoProfile}>
            {profilePicture ? (
              <img src={profilePicture} alt="Profile" className="nav-user-avatar" />
            ) : (
              <span className="nav-user-avatar nav-user-avatar-fallback">{(username || "U").slice(0, 1).toUpperCase()}</span>
            )}
            <span>{username}</span>
          </button>
          <button className="nav-logout-btn" onClick={handleLogout}>Logout</button>
        </div>

        <div className="home-main-grid">
          <section className="home-feed-panel">
            <div className="welcome-banner">
              <h3>Welcome back, {username}! 👋</h3>
              <p>Share your thoughts anonymously and support others in the community.</p>
            </div>

            <div className="feed-section">
              {error && <div className="error-box" style={{margin: "0 0 16px 0"}}>⚠ {error}</div>}
              {postingBlocked && <div className="error-box" style={{margin: "0 0 16px 0"}}>🔒 You are temporarily blocked from posting. Contact support for more info.</div>}
              
              <p className="section-label">Recent Confessions</p>

              {confessions.length === 0 ? (
                <div style={{textAlign: "center", padding: "40px 20px", color: "var(--text-muted)"}}>
                  <p style={{fontSize: "16px", marginBottom: "8px"}}>No confessions yet</p>
                  <p style={{fontSize: "14px"}}>Be the first to share your anonymous thought!</p>
                </div>
              ) : confessions.map((c) => (
                <div className="confession-card" key={c.id}>
                  <div className="card-header">
                    <span className="category-tag">{c.category}</span>
                    {c.isOwn && <span className="own-tag">Your Post</span>}
                  </div>
                  <p className="confession-text">{c.text}</p>
                  <div className="card-footer">
                    <ReactionsPage.LikeButton
                      confessionId={c.id}
                      liked={likedIds.includes(c.id)}
                      likeCount={c.likes}
                      onToggleLike={toggleLike}
                    />
                    <div className="footer-divider" />
                    <CommentsPage.CommentButton
                      confessionId={c.id}
                      commentCount={commentsMap[c.id] ? commentsMap[c.id].length : ""}
                      onLoadComments={loadComments}
                    />
                    <div className="footer-divider" />
                    {c.isOwn
                      ? <button className="footer-btn" onClick={() => handleDeleteConfession(c.id)}>🗑 Delete</button>
                      : <button className="footer-btn" onClick={() => {
                          setReportingConfessionId(c.id);
                          setShowReportModal(true);
                        }}><IconFlag /> Report</button>}
                    <span className="footer-time">{c.time}</span>
                  </div>

                  {expandedCommentId === c.id && (
                    <CommentsPage.CommentSection
                      confessionId={c.id}
                      comments={commentsMap[c.id]}
                      userId={userId}
                      commentDraft={commentDraft[c.id]}
                      commentLoading={commentLoading[c.id]}
                      onChangeCommentDraft={(cid, text) =>
                        setCommentDraft((prev) => ({ ...prev, [cid]: text }))
                      }
                      onSubmitComment={submitComment}
                      onDeleteComment={deleteComment}
                    />
                  )}
                </div>
              ))}
            </div>
          </section>

          <aside className="home-side-panel">
            <div className="side-card">
              <p className="side-kicker">Platform Overview</p>
              <h4>Community Space</h4>
              <p className="side-copy">Post anonymously, support others, and keep the feed respectful.</p>
              <div className="side-metric-row">
                <div className="side-metric">
                  <span>{confessions.length}</span>
                  <small>Visible posts</small>
                </div>
                <div className="side-metric">
                  <span>{role}</span>
                  <small>Current role</small>
                </div>
              </div>
              <button className="side-create-btn" onClick={() => setShowModal(true)}>
                <IconPlus />
                New Confession
              </button>
            </div>
          </aside>
        </div>

        <div className="fab-bar">
          <button className="fab" onClick={() => setShowModal(true)}>
            <IconPlus />
          </button>
        </div>

        {showModal && (
          <div className="modal-overlay" onClick={() => setShowModal(false)}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
              <p className="modal-title">Share Your Confession</p>
              <p className="modal-subtitle">Your identity will remain completely anonymous</p>
              
              <label className="field-label" style={{marginBottom: "8px"}}>Category</label>
              <select 
                value={selectedCategory} 
                onChange={(e) => setSelectedCategory(e.target.value)}
                style={{
                  width: "100%",
                  height: "40px",
                  background: "#F7F5FF",
                  border: "1.5px solid var(--border)",
                  borderRadius: "10px",
                  padding: "0 12px",
                  fontSize: "14px",
                  fontFamily: "'DM Sans', sans-serif",
                  color: "var(--text)",
                  marginBottom: "14px",
                  cursor: "pointer"
                }}
              >
                {confessionCategories.map((cat) => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>

              <textarea
                placeholder="What's on your mind? (Max 500 characters)"
                value={draft}
                onChange={(e) => setDraft(e.target.value.slice(0, 500))}
                autoFocus
              />
              <p className="char-count">{draft.length}/500 characters</p>
              {error && <div className="error-box" style={{marginBottom: "14px"}}>⚠ {error}</div>}
              <div className="modal-footer">
                <button className="btn-cancel" onClick={() => {
                  setShowModal(false);
                  setError("");
                  setDraft("");
                }}>Cancel</button>
                <button className="btn-post" onClick={postConfession} disabled={loading}>
                  {loading ? "Posting..." : "Post Anonymously"}
                </button>
              </div>
            </div>
          </div>
        )}

        {showReportModal && (
          <div className="modal-overlay" onClick={() => setShowReportModal(false)}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
              <p className="modal-title">Report Confession</p>
              <p className="modal-subtitle">Help us keep the community safe</p>
              
              <label className="field-label">Reason for Report</label>
              <select 
                value={reportReason} 
                onChange={(e) => setReportReason(e.target.value)}
                style={{
                  width: "100%",
                  height: "40px",
                  background: "#F7F5FF",
                  border: "1.5px solid var(--border)",
                  borderRadius: "10px",
                  padding: "0 12px",
                  fontSize: "14px",
                  fontFamily: "'DM Sans', sans-serif",
                  color: "var(--text)",
                  marginBottom: "14px",
                  cursor: "pointer"
                }}
              >
                <option value="">Select a reason...</option>
                <option value="Inappropriate content">Inappropriate content</option>
                <option value="Harassment">Harassment or bullying</option>
                <option value="Spam">Spam</option>
                <option value="Hate speech">Hate speech</option>
                <option value="Self-harm">Self-harm content</option>
                <option value="Other">Other</option>
              </select>

              {reportReason === "Other" && (
                <>
                  <label className="field-label">Please describe the issue</label>
                  <textarea
                    placeholder="Provide details about why you're reporting this..."
                    value={reportComment}
                    onChange={(e) => setReportComment(e.target.value.slice(0, 500))}
                    style={{
                      width: "100%",
                      minHeight: "100px",
                      background: "#F7F5FF",
                      border: "1.5px solid var(--border)",
                      borderRadius: "10px",
                      padding: "10px 12px",
                      fontSize: "14px",
                      fontFamily: "'DM Sans', sans-serif",
                      color: "var(--text)",
                      marginBottom: "8px",
                      resize: "vertical",
                      boxSizing: "border-box"
                    }}
                  />
                  <p className="char-count" style={{fontSize: "12px", marginBottom: "14px"}}>{reportComment.length}/500 characters</p>
                </>
              )}
              
              {reportSuccess && <div className="success-box" style={{marginBottom: "14px"}}>✓ {reportSuccess}</div>}
              {reportError && <div className="error-box" style={{marginBottom: "14px"}}>⚠ {reportError}</div>}
              
              <div className="modal-footer">
                <button className="btn-cancel" onClick={() => {
                  setShowReportModal(false);
                  setReportError("");
                  setReportSuccess("");
                  setReportReason("");
                  setReportComment("");
                }}>Cancel</button>
                <button className="btn-post" onClick={submitReport} disabled={loading || !reportReason.trim()}>
                  {loading ? "Submitting..." : "Submit Report"}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default HomePage;
