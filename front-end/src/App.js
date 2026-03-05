import { useState } from "react";
import React from "react";

// Use local backend for development, or update to your actual API URL
const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8080/api/v1";

const api = {
  get: async (endpoint) => {
    try {
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: "GET",
        mode: "cors",
        credentials: "include",
        headers: { 
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("ww_token") || ""}`,
          "Referrer-Policy": "strict-origin-when-cross-origin"
        },
      });
      const data = await response.json();
      return { ok: response.ok, status: response.status, data };
    } catch (err) {
      console.error("API GET Error:", err);
      return { ok: false, status: 0, data: { error: { message: "Network error. Please check API configuration." } } };
    }
  },
  post: async (endpoint, body) => {
    try {
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: "POST",
        mode: "cors",
        credentials: "include",
        headers: { 
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("ww_token") || ""}`,
          "Referrer-Policy": "strict-origin-when-cross-origin"
        },
        body: JSON.stringify(body),
      });
      const data = await response.json();
      return { ok: response.ok, status: response.status, data };
    } catch (err) {
      console.error("API POST Error:", err);
      return { ok: false, status: 0, data: { error: { message: "Network error. Please check API configuration." } } };
    }
  },
  delete: async (endpoint) => {
    try {
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: "DELETE",
        mode: "cors",
        credentials: "include",
        headers: { 
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("ww_token") || ""}`,
          "Referrer-Policy": "strict-origin-when-cross-origin"
        },
      });
      const data = await response.json();
      return { ok: response.ok, status: response.status, data };
    } catch (err) {
      console.error("API DELETE Error:", err);
      return { ok: false, status: 0, data: { error: { message: "Network error. Please check API configuration." } } };
    }
  },
};

const SAMPLE_CONFESSIONS = [
  {
    id: 1,
    category: "Mental Health",
    text: "I've been feeling really stressed about school lately, but I don't know who to talk to about it.",
    likes: 24,
    time: "2h ago",
  },
  {
    id: 2,
    category: "Personal",
    text: "I secretly love singing in the shower and pretending I'm performing at a concert!",
    likes: 42,
    time: "5h ago",
    isOwn: true,
  },
  {
    id: 3,
    category: "Relationships",
    text: "I wish I had the courage to tell my friend how much they mean to me.",
    likes: 67,
    time: "1d ago",
  },
];

const IconChat = () => (
  <svg viewBox="0 0 24 24"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H5.17L4 17.17V4h16v12z" /></svg>
);

const IconHeart = ({ filled }) => (
  <svg viewBox="0 0 24 24" fill={filled ? "#E53935" : "none"} stroke={filled ? "#E53935" : "#aaa"} strokeWidth="2">
    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
  </svg>
);

const IconFlag = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="#aaa" strokeWidth="2">
    <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z" /><line x1="4" y1="22" x2="4" y2="15" />
  </svg>
);

const IconPlus = () => (
  <svg viewBox="0 0 24 24"><path d="M19 13H13v6h-2v-6H5v-2h6V5h2v6h6v2z" /></svg>
);

const parseError = (data, status) => {
  if (data?.error?.message) return data.error.message;
  if (data?.error?.details && typeof data.error.details === "string") return data.error.details;
  if (data?.message) return data.message;

  switch (status) {
    case 400:
      return "Invalid input. Please check your details.";
    case 401:
      return "Invalid credentials. Please try again.";
    case 403:
      return "Access denied.";
    case 404:
      return "Service not found. Check your API URL.";
    case 409:
      return "Username or email already exists.";
    case 500:
      return "Server error. Please try again later.";
    default:
      return "Something went wrong. Please try again.";
  }
};

function LoginScreen({ onLogin, onGoSignUp }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    if (!username.trim() || !password.trim()) {
      setError("Please enter both username and password.");
      return;
    }

    setLoading(true);
    setError("");
    setSuccess("");

    try {
      const { ok, status, data } = await api.post("/auth/login", {
        email: username,
        password,
      });

      if (ok) {
        const token = data?.token || data?.data?.token;
        const userEmail = data?.email || data?.data?.email || username;
        const role = data?.role || data?.data?.role || "USER";
        
        if (token) {
          localStorage.setItem("ww_token", token);
          localStorage.setItem("ww_user_email", userEmail);
          localStorage.setItem("ww_user_role", role);
        }

        setSuccess("✓ Login successful! Redirecting to dashboard...");
        setTimeout(() => {
          onLogin(userEmail, token, role);
        }, 500);
      } else {
        setError(parseError(data, status));
        setPassword("");
      }
    } catch (err) {
      setError("Unable to connect to the server. Please check your connection.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="screen">
      <div className="auth-wrap">
        <div className="logo-circle"><IconChat /></div>
        <h1 className="auth-title">WhisperWall</h1>
        <p className="auth-subtitle">Share your thoughts anonymously</p>
        <div className="api-badge"><span className="api-dot" />API Connected</div>

        <div className="card">
          <label className="field-label">Username / Email</label>
          <input
            className="field-input"
            placeholder="Enter your username or email"
            value={username}
            disabled={loading}
            onChange={(e) => setUsername(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleLogin()}
          />

          <label className="field-label">Password</label>
          <input
            className="field-input"
            type="password"
            placeholder="••••••••"
            value={password}
            disabled={loading}
            onChange={(e) => setPassword(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleLogin()}
            style={{ marginBottom: "8px" }}
          />

          {success && <div className="success-box">✓ {success}</div>}
          {error && <div className="error-box">⚠ {error}</div>}

          <button className="btn-primary" onClick={handleLogin} disabled={loading}>
            {loading ? <><span className="spinner" /> Logging in...</> : "Log In"}
          </button>
        </div>

        <p className="auth-footer">
          Don't have an account?{" "}
          <span onClick={onGoSignUp}>Sign Up</span>
        </p>
      </div>
    </div>
  );
}

function SignUpScreen({ onSignUp, onGoLogin }) {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  const validateForm = () => {
    const errors = {};
    
    if (!username.trim()) {
      errors.username = "Username is required";
    } else if (username.length < 3) {
      errors.username = "Username must be at least 3 characters";
    } else if (username.length > 20) {
      errors.username = "Username must not exceed 20 characters";
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email.trim()) {
      errors.email = "Email is required";
    } else if (!emailRegex.test(email)) {
      errors.email = "Please enter a valid email address";
    }

    if (!password) {
      errors.password = "Password is required";
    } else if (password.length < 8) {
      errors.password = "Password must be at least 8 characters";
    } else if (!/[A-Z]/.test(password)) {
      errors.password = "Password must include at least one uppercase letter";
    } else if (!/[0-9]/.test(password)) {
      errors.password = "Password must include at least one number";
    }

    if (!confirm) {
      errors.confirm = "Confirm password is required";
    } else if (password !== confirm) {
      errors.confirm = "Passwords do not match";
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleCreate = async () => {
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setError("");
    setSuccess("");

    try {
      const { ok, status, data } = await api.post("/auth/register", {
        username,
        email,
        password,
      });

      if (ok) {
        const token = data?.token || data?.data?.token;
        const role = data?.role || data?.data?.role || "USER";
        
        if (token) {
          localStorage.setItem("ww_token", token);
          localStorage.setItem("ww_user_email", email);
          localStorage.setItem("ww_user_role", role);
          localStorage.setItem("ww_username", username);
        }

        setSuccess("✓ Account created successfully! Redirecting to dashboard...");
        setTimeout(() => {
          onSignUp(email, token, role);
        }, 500);
      } else {
        if (status === 409) {
          setError("Email already registered. Please use a different email or login.");
        } else {
          setError(parseError(data, status));
        }
      }
    } catch (err) {
      setError("Unable to connect to the server. Please check your connection.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="screen">
      <div className="auth-wrap">
        <div className="logo-circle"><IconChat /></div>
        <h1 className="auth-title">Join WhisperWall</h1>
        <p className="auth-subtitle">Create your anonymous account</p>
        <div className="api-badge"><span className="api-dot" />API Connected</div>

        <div className="card">
          <label className="field-label">Username</label>
          <input
            className="field-input"
            placeholder="Choose a username (3-20 characters)"
            value={username}
            disabled={loading}
            onChange={(e) => {
              setUsername(e.target.value);
              if (fieldErrors.username) setFieldErrors({...fieldErrors, username: ""});
            }}
          />
          {fieldErrors.username && <div className="error-box" style={{marginBottom: "12px"}}>⚠ {fieldErrors.username}</div>}

          <label className="field-label">Email</label>
          <input
            className="field-input"
            type="email"
            placeholder="your@email.com"
            value={email}
            disabled={loading}
            onChange={(e) => {
              setEmail(e.target.value);
              if (fieldErrors.email) setFieldErrors({...fieldErrors, email: ""});
            }}
          />
          {fieldErrors.email && <div className="error-box" style={{marginBottom: "12px"}}>⚠ {fieldErrors.email}</div>}

          <label className="field-label">Password</label>
          <input
            className="field-input"
            type="password"
            placeholder="At least 8 characters (with number & uppercase)"
            value={password}
            disabled={loading}
            onChange={(e) => {
              setPassword(e.target.value);
              if (fieldErrors.password) setFieldErrors({...fieldErrors, password: ""});
            }}
          />
          {fieldErrors.password && <div className="error-box" style={{marginBottom: "12px"}}>⚠ {fieldErrors.password}</div>}

          <label className="field-label">Confirm Password</label>
          <input
            className="field-input"
            type="password"
            placeholder="Re-enter password"
            value={confirm}
            disabled={loading}
            onChange={(e) => {
              setConfirm(e.target.value);
              if (fieldErrors.confirm) setFieldErrors({...fieldErrors, confirm: ""});
            }}
            onKeyDown={(e) => e.key === "Enter" && handleCreate()}
            style={{ marginBottom: "8px" }}
          />
          {fieldErrors.confirm && <div className="error-box" style={{marginBottom: "12px"}}>⚠ {fieldErrors.confirm}</div>}

          {success && <div className="success-box">✓ {success}</div>}
          {error && <div className="error-box">⚠ {error}</div>}

          <button className="btn-primary" onClick={handleCreate} disabled={loading}>
            {loading ? <><span className="spinner" /> Creating account...</> : "Create Account"}
          </button>
        </div>

        <p className="anon-note">Your identity will remain anonymous when posting confessions</p>

        <p className="auth-footer" style={{ marginTop: "12px" }}>
          Already have an account?{" "}
          <span onClick={onGoLogin}>Log In</span>
        </p>
      </div>
    </div>
  );
}

function HomeScreen({ username, token, role, onLogout }) {
  const [confessions, setConfessions] = useState([]);
  const [likedIds, setLikedIds] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);
  const [reportingConfessionId, setReportingConfessionId] = useState(null);
  const [reportReason, setReportReason] = useState("");
  const [draft, setDraft] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("Personal");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [postingBlocked, setPostingBlocked] = useState(false);
  const [reportError, setReportError] = useState("");
  const [reportSuccess, setReportSuccess] = useState("");

  const confessionCategories = ["Mental Health", "Relationships", "Personal", "Academic", "Other"];

  const handleLogout = () => {
    localStorage.removeItem("ww_token");
    localStorage.removeItem("ww_user_email");
    localStorage.removeItem("ww_user_role");
    localStorage.removeItem("ww_username");
    onLogout();
  };

  React.useEffect(() => {
    setConfessions(SAMPLE_CONFESSIONS);
  }, []);

  const toggleLike = async (id) => {
    const wasLiked = likedIds.includes(id);
    setLikedIds((prev) => (wasLiked ? prev.filter((x) => x !== id) : [...prev, id]));
    setConfessions((prev) => prev.map((c) => (c.id === id ? { ...c, likes: wasLiked ? c.likes - 1 : c.likes + 1 } : c)));

    try {
      await api.post(`/confessions/${id}/react`, { reactionType: "LIKE" });
    } catch (err) {
      console.error("Error submitting reaction:", err);
    }
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
        const newConfession = {
          id: data?.id || Date.now(),
          category: selectedCategory,
          text: draft.trim(),
          likes: 0,
          time: "Just now",
          isOwn: true,
        };
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
    if (!reportReason.trim()) {
      setReportError("Please select or provide a reason.");
      return;
    }

    setLoading(true);
    setReportError("");

    try {
      const { ok, status, data } = await api.post("/reports", {
        confessionId: reportingConfessionId,
        reason: reportReason,
      });

      if (ok) {
        setReportSuccess("✓ Report submitted successfully.");
        setTimeout(() => {
          setShowReportModal(false);
          setReportingConfessionId(null);
          setReportReason("");
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

  return (
    <div className="home-root">
      <div className="home-wrap">
        <div className="navbar">
          <div className="nav-logo"><IconChat /></div>
          <span className="nav-title">WhisperWall</span>
          <span className="nav-user">{username}</span>
          <button className="nav-logout-btn" onClick={handleLogout}>Logout</button>
        </div>

        <div className="welcome-banner">
          <h3>Welcome back, {username}! 👋</h3>
          <p>Share your thoughts anonymously and support others in the community.</p>
        </div>

        <div className="feed-section">
          {error && <div className="error-box" style={{margin: "0 16px 16px 16px"}}>⚠ {error}</div>}
          {postingBlocked && <div className="error-box" style={{margin: "0 16px 16px 16px"}}>🔒 You are temporarily blocked from posting. Contact support for more info.</div>}
          
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
                <button
                  className={`footer-btn${likedIds.includes(c.id) ? " liked" : ""}`}
                  onClick={() => toggleLike(c.id)}
                >
                  <IconHeart filled={likedIds.includes(c.id)} />
                  {c.likes}
                </button>
                <div className="footer-divider" />
                {c.isOwn
                  ? <button className="footer-btn" onClick={() => handleDeleteConfession(c.id)}>🗑 Delete</button>
                  : <button className="footer-btn" onClick={() => {
                      setReportingConfessionId(c.id);
                      setShowReportModal(true);
                    }}><IconFlag /> Report</button>}
                <span className="footer-time">{c.time}</span>
              </div>
            </div>
          ))}
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
              
              {reportSuccess && <div className="success-box" style={{marginBottom: "14px"}}>✓ {reportSuccess}</div>}
              {reportError && <div className="error-box" style={{marginBottom: "14px"}}>⚠ {reportError}</div>}
              
              <div className="modal-footer">
                <button className="btn-cancel" onClick={() => {
                  setShowReportModal(false);
                  setReportError("");
                  setReportSuccess("");
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

export default function App() {
  const [screen, setScreen] = useState("login");
  const [username, setUsername] = useState("");
  const [token, setToken] = useState(localStorage.getItem("ww_token") || "");
  const [role, setRole] = useState(localStorage.getItem("ww_user_role") || "USER");

  const handleLogin = (u, t, r) => {
    setUsername(u);
    setToken(t);
    setRole(r || "USER");
    setScreen("home");
  };

  const handleSignUp = (u, t, r) => {
    setUsername(u);
    setToken(t);
    setRole(r || "USER");
    setScreen("home");
  };

  const handleLogout = () => {
    setUsername("");
    setToken("");
    setRole("USER");
    setScreen("login");
  };

  // Check if user is already logged in
  React.useEffect(() => {
    const savedToken = localStorage.getItem("ww_token");
    const savedEmail = localStorage.getItem("ww_user_email");
    const savedRole = localStorage.getItem("ww_user_role");
    
    if (savedToken && savedEmail) {
      setToken(savedToken);
      setUsername(savedEmail);
      setRole(savedRole || "USER");
      setScreen("home");
    }
  }, []);

  return (
    <>
      {screen === "login" && (
        <LoginScreen
          onLogin={handleLogin}
          onGoSignUp={() => setScreen("signup")}
        />
      )}
      {screen === "signup" && (
        <SignUpScreen
          onSignUp={handleSignUp}
          onGoLogin={() => setScreen("login")}
        />
      )}
      {screen === "home" && (
        <HomeScreen 
          username={username}
          token={token}
          role={role}
          onLogout={handleLogout}
        />
      )}
    </>
  );
}
