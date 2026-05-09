import { useState } from "react";
import { LogoMark } from "../../../shared/components/LogoMark";
import { parseError, api } from "../../../shared/services/apiService";
import "../styles/Auth.css";

function LoginPage({ onLogin, onGoSignUp }) {
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
        username,
        email: username,
        password,
      });

      if (ok) {
        const token = data?.token || data?.data?.token;
        const accountUsername = data?.username || data?.data?.username || localStorage.getItem("ww_username") || username;
        const userEmail = data?.email || data?.data?.email || localStorage.getItem("ww_user_email") || "";
        const role = data?.role || data?.data?.role || "USER";
        const userId = data?.id || data?.data?.id;
        
        if (token) {
          localStorage.setItem("ww_token", token);
          if (userEmail) {
            localStorage.setItem("ww_user_email", userEmail);
          }
          if (userId != null) {
            localStorage.setItem("ww_user_id", String(userId));
          }
          localStorage.setItem("ww_username", accountUsername);
          localStorage.setItem("ww_user_role", role);
          localStorage.removeItem("ww_profile_picture");
        }

        setSuccess("✓ Login successful! Redirecting to dashboard...");
        setTimeout(() => {
          onLogin(accountUsername, token, role);
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
        <div className="logo-circle"><LogoMark /></div>
        <h1 className="auth-title">WhisperWall</h1>
        <p className="auth-subtitle">Share your thoughts anonymously</p>

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

export default LoginPage;
