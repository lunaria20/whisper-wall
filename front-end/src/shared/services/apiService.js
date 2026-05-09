// Use local backend for development, or update to your actual API URL
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

export const api = {
  get: async (endpoint) => {
    try {
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: "GET",
        mode: "cors",
        credentials: "omit",
        headers: { 
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("ww_token") || ""}`,
          "Referrer-Policy": "strict-origin-when-cross-origin"
        },
      });
      const data = await parseResponseData(response);
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
        credentials: "omit",
        headers: { 
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("ww_token") || ""}`,
          "Referrer-Policy": "strict-origin-when-cross-origin"
        },
        body: JSON.stringify(body),
      });
      const data = await parseResponseData(response);
      return { ok: response.ok, status: response.status, data };
    } catch (err) {
      console.error("API POST Error:", err);
      return { ok: false, status: 0, data: { error: { message: "Network error. Please check API configuration." } } };
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
          "Referrer-Policy": "strict-origin-when-cross-origin"
        },
        body: JSON.stringify(body),
      });
      const data = await parseResponseData(response);
      return { ok: response.ok, status: response.status, data };
    } catch (err) {
      console.error("API PUT Error:", err);
      return { ok: false, status: 0, data: { error: { message: "Network error. Please check API configuration." } } };
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
          "Referrer-Policy": "strict-origin-when-cross-origin"
        },
      });
      const data = await parseResponseData(response);
      return { ok: response.ok, status: response.status, data };
    } catch (err) {
      console.error("API DELETE Error:", err);
      return { ok: false, status: 0, data: { error: { message: "Network error. Please check API configuration." } } };
    }
  },
};

export const parseError = (data, status) => {
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
