import { useState, useEffect } from "react";
import LoginPage from "../features/auth/pages/LoginPage";
import SignUpPage from "../features/auth/pages/SignUpPage";
import HomePage from "../features/home/pages/HomePage";
import AdminDashboard from "../features/admin/pages/AdminDashboard";
import ModeratorDashboard from "../features/moderation/pages/ModerationDashboard";
import ProfilePage from "../features/profile/pages/ProfilePage";
import { api } from "../shared/services/apiService";
import "./App.css";

export default function App() {
  const [screen, setScreen] = useState("login");
  const [username, setUsername] = useState("");
  const [userId, setUserId] = useState(localStorage.getItem("ww_user_id") || "");
  const [userEmail, setUserEmail] = useState(localStorage.getItem("ww_user_email") || "");
  const [profilePicture, setProfilePicture] = useState(localStorage.getItem("ww_profile_picture") || "");
  const [token, setToken] = useState(localStorage.getItem("ww_token") || "");
  const [role, setRole] = useState(localStorage.getItem("ww_user_role") || "USER");

  const handleLogin = (u, t, r) => {
    setUsername(u);
    setToken(t);
    setRole(r || "USER");
    setUserId(localStorage.getItem("ww_user_id") || "");
    setUserEmail(localStorage.getItem("ww_user_email") || "");
    setProfilePicture(localStorage.getItem("ww_profile_picture") || "");
    
    const normalizedRole = (r || "USER").toUpperCase();
    if (normalizedRole === "ROLE_ADMIN" || normalizedRole === "ADMIN") {
      setScreen("admin");
    } else if (normalizedRole === "ROLE_MODERATOR" || normalizedRole === "MODERATOR") {
      setScreen("moderator");
    } else {
      setScreen("home");
    }
  };

  const handleSignUp = (u, t, r) => {
    setUsername(u);
    setToken(t);
    setRole(r || "USER");
    setUserId(localStorage.getItem("ww_user_id") || "");
    setUserEmail(localStorage.getItem("ww_user_email") || "");
    setProfilePicture(localStorage.getItem("ww_profile_picture") || "");
    
    const normalizedRole = (r || "USER").toUpperCase();
    if (normalizedRole === "ROLE_ADMIN" || normalizedRole === "ADMIN") {
      setScreen("admin");
    } else if (normalizedRole === "ROLE_MODERATOR" || normalizedRole === "MODERATOR") {
      setScreen("moderator");
    } else {
      setScreen("home");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("ww_token");
    localStorage.removeItem("ww_username");
    localStorage.removeItem("ww_user_email");
    localStorage.removeItem("ww_user_role");
    localStorage.removeItem("ww_user_id");
    localStorage.removeItem("ww_profile_picture");
    setUsername("");
    setUserId("");
    setUserEmail("");
    setProfilePicture("");
    setToken("");
    setRole("USER");
    setScreen("login");
  };

  const handleProfileUpdated = (nextUsername, nextEmail, nextProfilePicture) => {
    setUsername(nextUsername);
    setUserEmail(nextEmail || "");
    setProfilePicture(nextProfilePicture || "");
  };

  // Check if user is already logged in
  useEffect(() => {
    const savedToken = localStorage.getItem("ww_token");
    const savedUsername = localStorage.getItem("ww_username");
    const savedEmail = localStorage.getItem("ww_user_email");
    const savedRole = localStorage.getItem("ww_user_role");
    const savedUserId = localStorage.getItem("ww_user_id");
    
    if (savedToken) {
      setToken(savedToken);
      setUsername(savedUsername || savedEmail);
      setUserId(savedUserId || "");
      setUserEmail(savedEmail || "");
      setProfilePicture(localStorage.getItem("ww_profile_picture") || "");
      setRole(savedRole || "USER");
    }
  }, []);

  useEffect(() => {
    if (!token) return;

    let cancelled = false;

    const syncCurrentUser = async () => {
      const { ok, status, data } = await api.get("/users/me");

      if (!ok) {
        if (status === 401 && !cancelled) {
          localStorage.removeItem("ww_token");
          localStorage.removeItem("ww_username");
          localStorage.removeItem("ww_user_email");
          localStorage.removeItem("ww_user_role");
          localStorage.removeItem("ww_user_id");
          localStorage.removeItem("ww_profile_picture");
          setUsername("");
          setUserId("");
          setUserEmail("");
          setProfilePicture("");
          setToken("");
          setRole("USER");
          setScreen("login");
        }
        return;
      }

      if (cancelled) return;

      const nextUsername = data?.username || "";
      const nextEmail = data?.email || "";
      const nextPicture = data?.profilePicture || "";
      const nextUserId = data?.id != null ? String(data.id) : "";

      if (nextUsername) {
        localStorage.setItem("ww_username", nextUsername);
        setUsername(nextUsername);
      }
      localStorage.setItem("ww_user_email", nextEmail);
      localStorage.setItem("ww_profile_picture", nextPicture);
      if (nextUserId) {
        localStorage.setItem("ww_user_id", nextUserId);
      }

      setUserId(nextUserId);
      setUserEmail(nextEmail);
      setProfilePicture(nextPicture);

      const normalizedRole = (localStorage.getItem("ww_user_role") || "USER").toUpperCase();
      if (normalizedRole === "ROLE_ADMIN" || normalizedRole === "ADMIN") {
        setScreen("admin");
      } else if (normalizedRole === "ROLE_MODERATOR" || normalizedRole === "MODERATOR") {
        setScreen("moderator");
      } else {
        setScreen("home");
      }
    };

    syncCurrentUser();

    return () => {
      cancelled = true;
    };
  }, [token]);

  return (
    <>
      {screen === "login" && (
        <LoginPage
          onLogin={handleLogin}
          onGoSignUp={() => setScreen("signup")}
        />
      )}
      {screen === "signup" && (
        <SignUpPage
          onSignUp={handleSignUp}
          onGoLogin={() => setScreen("login")}
        />
      )}
      {screen === "home" && (
        <HomePage
          username={username}
          token={token}
          role={role}
          userId={userId}
          profilePicture={profilePicture}
          onLogout={handleLogout}
          onGoProfile={() => setScreen("profile")}
        />
      )}
      {screen === "admin" && (
        <AdminDashboard
          username={username}
          onLogout={handleLogout}
        />
      )}
      {screen === "moderator" && (
        <ModeratorDashboard
          username={username}
          onLogout={handleLogout}
        />
      )}
      {screen === "profile" && (
        <ProfilePage
          currentUsername={username}
          currentEmail={userEmail}
          onBack={() => setScreen(role === "MODERATOR" ? "moderator" : "home")}
          onLogout={handleLogout}
          onProfileUpdated={handleProfileUpdated}
        />
      )}
    </>
  );
}
