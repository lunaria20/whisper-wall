import { useState } from "react";
import { LogoMark } from "../../../shared/components/LogoMark";
import { parseError, api } from "../../../shared/services/apiService";
import "../styles/Auth.css";

function SignUpPage({ onSignUp, onGoLogin }) {
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
        const userId = data?.id || data?.data?.id;
        
        if (token) {
          localStorage.setItem("ww_token", token);
          localStorage.setItem("ww_user_email", email);
          localStorage.setItem("ww_user_role", role);
          localStorage.setItem("ww_username", username);
          if (userId != null) {
            localStorage.setItem("ww_user_id", String(userId));
          }
          localStorage.removeItem("ww_profile_picture");
        }

        setSuccess("✓ Account created successfully! Redirecting to dashboard...");
        setTimeout(() => {
          onSignUp(username, token, role);
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
        <div className="logo-circle"><LogoMark /></div>
        <h1 className="auth-title">Join WhisperWall</h1>
        <p className="auth-subtitle">Create your anonymous account</p>

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

export default SignUpPage;
