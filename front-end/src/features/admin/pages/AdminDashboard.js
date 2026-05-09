import React, { useState, useEffect } from "react";
import "../styles/Admin.css";

const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8080/api";

const parseResponseData = async (response) => {
  const contentType = response.headers.get("content-type") || "";
  const text = await response.text();

  if (!text) {
    return {};
  }

  if (contentType.includes("application/json")) {
    try {
      return JSON.parse(text);
    } catch (error) {
      return { message: text };
    }
  }

  return { message: text };
};

const api = {
  get: async (endpoint) => {
    try {
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: "GET",
        mode: "cors",
        credentials: "omit",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("ww_token") || ""}`,
        },
      });
      const data = await parseResponseData(response);
      return { ok: response.ok, status: response.status, data };
    } catch (err) {
      console.error("API GET Error:", err);
      return { ok: false, status: 0, data: {} };
    }
  },
  post: async (endpoint, body) => {
    try {
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: "POST",
        mode: "cors",
        credentials: "omit",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("ww_token") || ""}`,
        },
        body: JSON.stringify(body),
      });
      const data = await parseResponseData(response);
      return { ok: response.ok, status: response.status, data };
    } catch (err) {
      console.error("API POST Error:", err);
      return { ok: false, status: 0, data: {} };
    }
  },
  put: async (endpoint, body) => {
    try {
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: "PUT",
        mode: "cors",
        credentials: "omit",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("ww_token") || ""}`,
        },
        body: JSON.stringify(body),
      });
      const data = await parseResponseData(response);
      return { ok: response.ok, status: response.status, data };
    } catch (err) {
      console.error("API PUT Error:", err);
      return { ok: false, status: 0, data: {} };
    }
  },
  delete: async (endpoint) => {
    try {
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: "DELETE",
        mode: "cors",
        credentials: "omit",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("ww_token") || ""}`,
        },
      });
      return { ok: response.ok, status: response.status, data: {} };
    } catch (err) {
      console.error("API DELETE Error:", err);
      return { ok: false, status: 0, data: {} };
    }
  },
};

const IconLogout = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><polyline points="16 17 21 12 16 7" /><line x1="21" y1="12" x2="9" y2="12" />
  </svg>
);

function AdminDashboard({ username, onLogout }) {
  const [tab, setTab] = useState("stats");
  const [stats, setStats] = useState(null);
  const [posts, setPosts] = useState([]);
  const [restrictionRequests, setRestrictionRequests] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showCreateUserModal, setShowCreateUserModal] = useState(false);
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    displayName: "",
    roleName: "ROLE_USER",
  });

  // Load stats
  useEffect(() => {
    const loadStats = async () => {
      setLoading(true);
      setError("");
      const result = await api.get("/admin/stats/usage");
      if (result.ok) {
        setStats(result.data);
      } else {
        setError("Failed to load statistics");
      }
      setLoading(false);
    };

    if (tab === "stats") {
      loadStats();
    }
  }, [tab]);

  // Load posts
  useEffect(() => {
    const loadPosts = async () => {
      setLoading(true);
      setError("");
      const result = await api.get("/admin/posts?page=0&size=20");
      if (result.ok) {
        setPosts(result.data.content || []);
      } else {
        const statusMsg = result.status === 403
          ? "You do not have permission to manage posts."
          : `Failed to load posts (${result.status})`;
        setError(statusMsg);
        console.error("Admin posts load error:", result.status, result.data);
      }
      setLoading(false);
    };

    if (tab === "posts") {
      loadPosts();
    }
  }, [tab]);

  // Load restriction requests
  useEffect(() => {
    const loadRequests = async () => {
      setLoading(true);
      setError("");
      const result = await api.get("/admin/restriction-requests/pending?page=0&size=20");
      if (result.ok) {
        setRestrictionRequests(result.data.content || []);
      } else {
        setError("Failed to load restriction requests");
      }
      setLoading(false);
    };

    if (tab === "restrictions") {
      loadRequests();
    }
  }, [tab]);

  // Load users
  useEffect(() => {
    const loadUsers = async () => {
      setLoading(true);
      setError("");
      const result = await api.get("/admin/users?page=0&size=50");
      if (result.ok) {
        setUsers(result.data.content || []);
      } else {
        setError("Failed to load users");
      }
      setLoading(false);
    };

    if (tab === "users") {
      loadUsers();
    }
  }, [tab]);

  const handleDeletePost = async (postId) => {
    if (!window.confirm("Are you sure you want to delete this post?")) return;

    const result = await api.delete(`/admin/posts/${postId}`);
    if (result.ok) {
      setPosts(posts.filter(p => p.id !== postId));
      setError("");
    } else {
      setError("Failed to delete post");
    }
  };

  const handleCreateUser = async () => {
    if (!formData.username || !formData.email || !formData.password) {
      setError("All fields are required");
      return;
    }

    const result = await api.post("/admin/users", formData);
    if (result.ok) {
      setShowCreateUserModal(false);
      setFormData({ username: "", email: "", password: "", displayName: "", roleName: "ROLE_USER" });
      setError("");
      // reload users list
      const refreshed = await api.get("/admin/users?page=0&size=50");
      if (refreshed.ok) setUsers(refreshed.data.content || []);
    } else {
      setError(result.data?.message || "Failed to create user");
    }
  };

  const handleDeleteUser = async (userId) => {
    if (!window.confirm("Are you sure you want to delete this user?")) return;

    const result = await api.delete(`/admin/users/${userId}`);
    if (result.ok) {
      setUsers(users.filter(u => u.id !== userId));
      setError("");
    } else {
      setError(result.data?.message || "Failed to delete user");
    }
  };

  const handleApproveRestrictionRequest = async (requestId) => {
    const result = await api.post(`/admin/restriction-requests/${requestId}/approve`, {});
    if (result.ok) {
      setRestrictionRequests(restrictionRequests.filter(r => r.id !== requestId));
      setError("");
    } else {
      setError("Failed to approve restriction");
    }
  };

  const handleRejectRestrictionRequest = async (requestId) => {
    const result = await api.post(`/admin/restriction-requests/${requestId}/reject?reason=Rejected+by+admin`, {});
    if (result.ok) {
      setRestrictionRequests(restrictionRequests.filter(r => r.id !== requestId));
      setError("");
    } else {
      setError("Failed to reject restriction");
    }
  };

  return (
    <div className="admin-container">
      <div className="admin-header">
        <h1>🔧 Admin Dashboard</h1>
        <div className="admin-user-info">
          <span>{username}</span>
          <button className="btn-logout" onClick={onLogout}>
            <IconLogout /> Logout
          </button>
        </div>
      </div>

      <div className="admin-nav">
        <button className={`nav-btn ${tab === "stats" ? "active" : ""}`} onClick={() => setTab("stats")}>
          📊 Statistics
        </button>
        <button className={`nav-btn ${tab === "posts" ? "active" : ""}`} onClick={() => setTab("posts")}>
          📝 Manage Posts
        </button>
        <button className={`nav-btn ${tab === "restrictions" ? "active" : ""}`} onClick={() => setTab("restrictions")}>
          🚫 Restriction Requests
        </button>
        <button className={`nav-btn ${tab === "users" ? "active" : ""}`} onClick={() => setTab("users")}>
          👥 Manage Users
        </button>
      </div>

      <div className="admin-content">
        {loading && <div className="error-box">Loading...</div>}
        {error && <div className="error-box">⚠ {error}</div>}

        {/* ✅ FIX: removed Admin Users and Moderators stat cards */}
        {tab === "stats" && stats && (
          <div className="stats-grid">
            <div className="stat-card">
              <h3>Total Users</h3>
              <p className="stat-number">{stats.totalUsers}</p>
            </div>
            <div className="stat-card">
              <h3>Total Posts</h3>
              <p className="stat-number">{stats.totalPosts}</p>
            </div>
            <div className="stat-card">
              <h3>Total Comments</h3>
              <p className="stat-number">{stats.totalComments}</p>
            </div>
            <div className="stat-card">
              <h3>Total Reports</h3>
              <p className="stat-number">{stats.totalReports}</p>
            </div>
            <div className="stat-card">
              <h3>Active Users (30d)</h3>
              <p className="stat-number">{stats.activeUsers}</p>
            </div>
            <div className="stat-card">
              <h3>Restricted Users</h3>
              <p className="stat-number">{stats.restrictedUsers}</p>
            </div>
          </div>
        )}

        {tab === "posts" && (
          <div className="management-section">
            <h2>Manage Posts</h2>
            <div className="management-list">
              {posts.length === 0 ? (
                <p>No posts found</p>
              ) : (
                posts.map(post => (
                  <div key={post.id} className="management-item">
                    <div className="item-header">
                      <div className="item-info">
                        <h4>Post #{post.id}</h4>
                        <p className="item-content">{post.content?.substring(0, 100)}...</p>
                        <small>By: {post.username}</small>
                      </div>
                      <button className="btn-delete" onClick={() => handleDeletePost(post.id)}>
                        Delete
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}

        {tab === "restrictions" && (
          <div className="management-section">
            <h2>Pending Restriction Requests</h2>
            <div className="management-list">
              {restrictionRequests.length === 0 ? (
                <p>No pending requests</p>
              ) : (
                restrictionRequests.map(req => (
                  <div key={req.id} className="management-item">
                    <div className="item-header">
                      <div className="item-info">
                        <h4>Request to restrict: {req.userToRestrict?.username}</h4>
                        <p><strong>Reason:</strong> {req.reason}</p>
                        <p><strong>Duration:</strong> {req.requestedDurationDays} days</p>
                        <small>By moderator: {req.requestedByModerator?.username}</small>
                      </div>
                      <div className="btn-group">
                        <button className="btn-approve" onClick={() => handleApproveRestrictionRequest(req.id)}>
                          Approve
                        </button>
                        <button className="btn-reject" onClick={() => handleRejectRestrictionRequest(req.id)}>
                          Reject
                        </button>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}

        {tab === "users" && (
          <div className="management-section">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "20px" }}>
              <h2>Manage Users</h2>
              <button className="btn-primary" onClick={() => setShowCreateUserModal(true)}>
                + Create User
              </button>
            </div>
            <div className="management-list">
              {users.length === 0 ? (
                <p>No users found</p>
              ) : (
                users.map(user => (
                  <div key={user.id} className="management-item">
                    <div className="item-header">
                      <div className="item-info">
                        <h4>{user.displayName || user.username}</h4>
                        <p className="item-content">@{user.username}</p>
                        <small>Email: {user.email}</small>
                        <small style={{ marginLeft: "10px" }}>Role: {user.roleName}</small>
                      </div>
                      <button className="btn-delete" onClick={() => handleDeleteUser(user.id)}>
                        Delete
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </div>

      {showCreateUserModal && (
        <div className="modal-overlay" onClick={() => setShowCreateUserModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Create New User</h2>
            <label>Username</label>
            <input
              type="text"
              value={formData.username}
              onChange={(e) => setFormData({...formData, username: e.target.value})}
              placeholder="Username"
            />
            <label>Email</label>
            <input
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({...formData, email: e.target.value})}
              placeholder="Email"
            />
            <label>Password</label>
            <input
              type="password"
              value={formData.password}
              onChange={(e) => setFormData({...formData, password: e.target.value})}
              placeholder="Password"
            />
            <label>Display Name</label>
            <input
              type="text"
              value={formData.displayName}
              onChange={(e) => setFormData({...formData, displayName: e.target.value})}
              placeholder="Display Name"
            />
            <label>Role</label>
            <select value={formData.roleName} onChange={(e) => setFormData({...formData, roleName: e.target.value})}>
              <option value="ROLE_USER">User</option>
              <option value="ROLE_MODERATOR">Moderator</option>
              <option value="ROLE_ADMIN">Admin</option>
            </select>
            <div className="modal-footer">
              <button className="btn-cancel" onClick={() => setShowCreateUserModal(false)}>Cancel</button>
              <button className="btn-primary" onClick={handleCreateUser}>Create</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminDashboard;