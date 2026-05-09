import React, { useEffect, useState } from "react";
import "../styles/Profile.css";
import { handleImageUploadWithCompression } from "../../../shared/utils/imageUtils";

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
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 15000); // 15 second timeout
      
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: "GET",
        mode: "cors",
        credentials: "omit",
        signal: controller.signal,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("ww_token") || ""}`,
        },
      });

      clearTimeout(timeoutId);
      const data = await parseResponseData(response);
      return { ok: response.ok, status: response.status, data };
    } catch (error) {
      if (error.name === 'AbortError') {
        return { ok: false, status: 0, data: { message: "Request timeout. The server took too long to respond." } };
      }
      return { ok: false, status: 0, data: { message: "Network error while loading profile." } };
    }
  },
  put: async (endpoint, body) => {
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 30000); // 30 second timeout for larger payloads
      
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: "PUT",
        mode: "cors",
        credentials: "omit",
        signal: controller.signal,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("ww_token") || ""}`,
        },
        body: JSON.stringify(body),
      });

      clearTimeout(timeoutId);
      const data = await parseResponseData(response);
      return { ok: response.ok, status: response.status, data };
    } catch (error) {
      if (error.name === 'AbortError') {
        return { ok: false, status: 0, data: { message: "Request timeout. The server took too long to respond. Try uploading smaller profile pictures." } };
      }
      return { ok: false, status: 0, data: { message: "Network error while saving profile." } };
    }
  },
};

const getErrorMessage = (payload, fallback) => {
  if (typeof payload?.message === "string") return payload.message;
  if (payload?.error?.message) return payload.error.message;
  return fallback;
};

const ProfileAvatar = ({ image, fallback }) => {
  if (image) {
    return <img className="profile-avatar" src={image} alt="Profile" />;
  }

  return <div className="profile-avatar profile-avatar-fallback">{(fallback || "U").slice(0, 1).toUpperCase()}</div>;
};

export default function ProfilePage({ currentUsername, onBack, onLogout, onProfileUpdated }) {
  const [username, setUsername] = useState(currentUsername || "");
  const [email, setEmail] = useState("");
  const [bio, setBio] = useState("");
  const [profilePicture, setProfilePicture] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [changingPassword, setChangingPassword] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [passwordSuccess, setPasswordSuccess] = useState("");

  useEffect(() => {
    const loadProfile = async () => {
      setLoading(true);
      setError("");

      const { ok, status, data } = await api.get("/users/me");
      if (!ok) {
        if (status === 401) {
          // Token expired, clear it and logout
          localStorage.removeItem("ww_token");
          localStorage.removeItem("ww_username");
          localStorage.removeItem("ww_user_email");
          setError("Session expired. Please log in again.");
          onLogout(); // Trigger logout to go back to login screen
        } else {
          setError(getErrorMessage(data, "Unable to load profile."));
        }
        setLoading(false);
        return;
      }

      setUsername(data?.username || "");
      setEmail(data?.email || "");
      setBio(data?.bio || "");
      setProfilePicture(data?.profilePicture || "");
      setLoading(false);
    };

    loadProfile();
  }, [onLogout]);

  const handleImageUpload = async (event) => {
    const selected = event.target.files?.[0];
    if (!selected) return;

    if (!selected.type.startsWith("image/")) {
      setError("Please choose an image file.");
      return;
    }

    // File size check before compression
    if (selected.size > 10 * 1024 * 1024) {
      setError("Image is too large. Please use an image smaller than 10MB.");
      return;
    }

    try {
      setError("");
      // Use the new image upload utility with compression and Supabase
      const result = await handleImageUploadWithCompression(selected, username);
      setProfilePicture(result.data);
      
      // Show feedback about storage method
      if (result.type === "url") {
        console.log("Profile picture uploaded to cloud storage");
      } else {
        console.log("Profile picture stored locally as base64");
      }
    } catch (readError) {
      setError("Unable to process the selected image. Please try a smaller file.");
    }
  };

  const handleSave = async () => {
    if (!email.trim()) {
      setError("Email is required.");
      return;
    }

    setSaving(true);
    setError("");
    setSuccess("");

    const payload = {
      username: username.trim(),
      email: email.trim(),
      bio: bio.trim(),
      profilePicture,
    };

    const { ok, status, data } = await api.put("/users/me", payload);

    if (!ok) {
      if (status === 401) {
        // Token expired, clear it and redirect to login
        localStorage.removeItem("ww_token");
        localStorage.removeItem("ww_username");
        localStorage.removeItem("ww_user_email");
        setError("Your session has expired. Please log in again.");
        onLogout(); // Trigger logout to go back to login screen
      } else {
        setError(getErrorMessage(data, status === 409 ? "Username or email is already in use." : "Failed to update profile."));
      }
      setSaving(false);
      return;
    }

    const nextUsername = data?.username || payload.username;
    const nextEmail = data?.email || payload.email;
    const nextPicture = data?.profilePicture || payload.profilePicture;

    localStorage.setItem("ww_username", nextUsername);
    localStorage.setItem("ww_user_email", nextEmail);
    localStorage.setItem("ww_profile_picture", nextPicture || "");

    onProfileUpdated(nextUsername, nextEmail, nextPicture || "");
    setSuccess("Profile updated successfully.");
    setSaving(false);
  };

  const handleChangePassword = async () => {
    if (!currentPassword || !newPassword || !confirmPassword) {
      setPasswordError("Please fill out all password fields.");
      setPasswordSuccess("");
      return;
    }

    if (newPassword.length < 8) {
      setPasswordError("New password must be at least 8 characters.");
      setPasswordSuccess("");
      return;
    }

    if (newPassword !== confirmPassword) {
      setPasswordError("New password and confirmation do not match.");
      setPasswordSuccess("");
      return;
    }

    setChangingPassword(true);
    setPasswordError("");
    setPasswordSuccess("");

    const { ok, status, data } = await api.put("/users/me/password", {
      currentPassword,
      newPassword,
    });

    if (!ok) {
      if (status === 401) {
        localStorage.removeItem("ww_token");
        localStorage.removeItem("ww_username");
        localStorage.removeItem("ww_user_email");
        setPasswordError("Your session has expired. Please log in again.");
        onLogout();
      } else {
        setPasswordError(getErrorMessage(data, "Failed to change password."));
      }
      setChangingPassword(false);
      return;
    }

    setCurrentPassword("");
    setNewPassword("");
    setConfirmPassword("");
    setPasswordSuccess("Password updated successfully.");
    setChangingPassword(false);
  };

  return (
    <div className="profile-root">
      <div className="profile-card">
        <div className="profile-head">
          <button className="profile-back" onClick={onBack}>Back</button>
          <h1>Your Profile</h1>
          <button className="profile-logout" onClick={onLogout}>Logout</button>
        </div>

        {loading ? (
          <div className="profile-loading">Loading profile...</div>
        ) : (
          <>
            <div className="profile-hero">
              <ProfileAvatar image={profilePicture} fallback={username} />
              <div>
                <p className="profile-hint">Profile Picture</p>
                <label className="profile-upload-btn">
                  Upload Photo
                  <input type="file" accept="image/*" onChange={handleImageUpload} />
                </label>
              </div>
            </div>

            <div className="profile-form">
              <label>Username</label>
              <input value={username} disabled placeholder="Your username" />

              <label>Email</label>
              <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@email.com" />

              <label>Bio</label>
              <textarea
                value={bio}
                onChange={(e) => setBio(e.target.value.slice(0, 1000))}
                placeholder="Tell us something about you..."
              />
              <small>{bio.length}/1000</small>
            </div>

            <div className="profile-password-section">
              <h2>Change Password</h2>
              <div className="profile-form">
                <label>Current Password</label>
                <input
                  type="password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  placeholder="Enter current password"
                />

                <label>New Password</label>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Enter new password"
                />

                <label>Confirm New Password</label>
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Confirm new password"
                />
              </div>

              {passwordError ? <div className="profile-error">{passwordError}</div> : null}
              {passwordSuccess ? <div className="profile-success">{passwordSuccess}</div> : null}

              <button className="profile-password-save" onClick={handleChangePassword} disabled={changingPassword}>
                {changingPassword ? "Updating Password..." : "Update Password"}
              </button>
            </div>

            {error ? <div className="profile-error">{error}</div> : null}
            {success ? <div className="profile-success">{success}</div> : null}

            <button className="profile-save" onClick={handleSave} disabled={saving}>
              {saving ? "Saving..." : "Save Profile"}
            </button>
          </>
        )}
      </div>
    </div>
  );
}
