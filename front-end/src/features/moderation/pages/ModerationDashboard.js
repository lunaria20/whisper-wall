import React, { useCallback, useMemo, useRef, useState } from "react";
import "../styles/Moderation.css";

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
          Authorization: `Bearer ${localStorage.getItem("ww_token") || ""}`,
        },
      });

      const data = await parseResponseData(response);
      return { ok: response.ok, status: response.status, data };
    } catch (error) {
      return { ok: false, status: 0, data: { message: "Network error while loading reports." } };
    }
  },
  post: async (endpoint, body) => {
    try {
      const fullUrl = `${API_BASE}${endpoint}`;
      const token = localStorage.getItem("ww_token") || "";
      console.log(`[API POST] URL: ${fullUrl}`);
      console.log(`[API POST] Token present: ${!!token} (length: ${token.length})`);
      console.log(`[API POST] Body:`, body);

      const response = await fetch(fullUrl, {
        method: "POST",
        mode: "cors",
        credentials: "omit",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: body ? JSON.stringify(body) : undefined,
      });

      console.log(`[API POST] Response Status: ${response.status}`);
      const data = await parseResponseData(response);
      return { ok: response.ok, status: response.status, data };
    } catch (error) {
      console.error(`[API POST Error]`, error);
      return { ok: false, status: 0, data: { message: "Network error while applying moderation action." } };
    }
  },
};

const formatRelativeAge = (dateValue) => {
  if (!dateValue) return "just now";

  const createdAt = new Date(dateValue);
  if (Number.isNaN(createdAt.getTime())) return "just now";

  const diffMs = Date.now() - createdAt.getTime();
  const diffMinutes = Math.floor(diffMs / 60000);
  if (diffMinutes < 1) return "just now";
  if (diffMinutes < 60) return `${diffMinutes}m ago`;

  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours}h ago`;

  const diffDays = Math.floor(diffHours / 24);
  return `${diffDays}d ago`;
};

const inferSeverity = (reason = "") => {
  const normalized = reason.toLowerCase();
  if (normalized.includes("violence") || normalized.includes("threat") || normalized.includes("harassment")) {
    return "critical";
  }
  if (normalized.includes("hate") || normalized.includes("privacy")) {
    return "high";
  }
  if (normalized.includes("spam")) {
    return "medium";
  }
  return "low";
};

const mapReport = (item) => ({
  id: item?.id,
  status: (() => {
    const normalized = (item?.status || "PENDING").toLowerCase();
    if (normalized === "resolved") return "reviewed";
    return normalized;
  })(),
  severity: inferSeverity(item?.reason || ""),
  category: item?.confessionCategory || "Other",
  confessionId: item?.confessionId,
  content: item?.confessionContent || "(No confession content available)",
  reason: item?.reason || "Unspecified",
  details: item?.description || "No additional details provided.",
  reporter: item?.reportedByUsername ? `#${String(item.reportedByUsername).toUpperCase()}` : "#UNKNOWN",
  confessionOwnerUsername: item?.confessionOwnerUsername || "",
  reportedAt: item?.createdAt ? new Date(item.createdAt).toLocaleString() : "Unknown",
  ageText: formatRelativeAge(item?.createdAt),
});

const SeverityBadge = ({ severity }) => (
  <span className={`mod-severity mod-severity-${severity}`}>
    {severity.toUpperCase()}
  </span>
);

const StatusBadge = ({ status }) => (
  <span className={`mod-status mod-status-${status}`}>
    {status.toUpperCase()}
  </span>
);

const Modal = ({ title, subtitle, children, onClose }) => (
  <div className="mod-modal-overlay" onClick={onClose}>
    <div className="mod-modal" onClick={(e) => e.stopPropagation()}>
      <div className="mod-modal-head">
        <div>
          <h3>{title}</h3>
          {subtitle ? <p>{subtitle}</p> : null}
        </div>
        <button className="mod-icon-btn" onClick={onClose} aria-label="Close dialog">
          ×
        </button>
      </div>
      {children}
    </div>
  </div>
);

export default function ModeratorDashboard({ username, onLogout }) {
  const [reports, setReports] = useState([]);
  const [stats, setStats] = useState({ pending: 0, reviewed: 0, dismissed: 0 });
  const [responseRate, setResponseRate] = useState(100);
  const [notifications, setNotifications] = useState([]);
  const [activeTab, setActiveTab] = useState("pending");
  const [severityFilter, setSeverityFilter] = useState("all");
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState("");
  const [selectedReport, setSelectedReport] = useState(null);
  const [removeTarget, setRemoveTarget] = useState(null);
  const [dismissTarget, setDismissTarget] = useState(null);
  const [restrictTarget, setRestrictTarget] = useState(null);
  const [actionNote, setActionNote] = useState("");
  const [restrictReason, setRestrictReason] = useState("");
  const [restrictDuration, setRestrictDuration] = useState("7");
  const [restrictLoading, setRestrictLoading] = useState(false);
  const [flashMessage, setFlashMessage] = useState("");
  const previousPendingRef = useRef(null);

  const addNotification = useCallback((message, level = "info") => {
    const entry = {
      id: `${Date.now()}-${Math.random()}`,
      level,
      message,
      time: new Date().toLocaleTimeString(),
    };

    setNotifications((prev) => [entry, ...prev].slice(0, 6));
  }, []);

  const updateDerivedMetrics = useCallback((nextStats) => {
    const total = nextStats.pending + nextStats.reviewed + nextStats.dismissed;
    const handled = nextStats.reviewed + nextStats.dismissed;
    setResponseRate(total === 0 ? 100 : Math.round((handled / total) * 100));
  }, []);

  React.useEffect(() => {
    let isMounted = true;

    const fetchReports = async (showLoading = true) => {
      if (showLoading) {
        setLoading(true);
      }
      setLoadError("");

      const refreshStats = async () => {
        const [pendingResult, reviewedResult, dismissedResult] = await Promise.all([
          api.get("/admin/reports?status=PENDING&page=0&size=1"),
          api.get("/admin/reports?status=REVIEWED&page=0&size=1"),
          api.get("/admin/reports?status=DISMISSED&page=0&size=1"),
        ]);

        const nextStats = {
          pending: pendingResult.data?.totalElements || 0,
          reviewed: reviewedResult.data?.totalElements || 0,
          dismissed: dismissedResult.data?.totalElements || 0,
        };

        if (isMounted) {
          setStats(nextStats);
          updateDerivedMetrics(nextStats);

          const previousPending = previousPendingRef.current;
          if (previousPending !== null && nextStats.pending > previousPending) {
            addNotification("New reported posts arrived and need review.", "warning");
          }
          previousPendingRef.current = nextStats.pending;
        }
      };

      const status = activeTab === "reviewed" ? "RESOLVED" : activeTab.toUpperCase();
      const { ok, status: responseStatus, data } = await api.get(`/admin/reports?status=${status}&page=0&size=100`);

      if (!ok) {
        if (responseStatus === 403) {
          setLoadError("You are not allowed to access moderation reports.");
        } else if (responseStatus === 401) {
          setLoadError("Session expired. Please log in again.");
        } else {
          setLoadError(data?.message || "Unable to load reports right now.");
        }
        if (isMounted) {
          setReports([]);
        }
        await refreshStats();
        if (isMounted && showLoading) {
          setLoading(false);
        }
        return;
      }

      const rows = Array.isArray(data?.content) ? data.content : [];
      if (isMounted) {
        setReports(rows.map(mapReport));
      }
      await refreshStats();
      if (isMounted && showLoading) {
        setLoading(false);
      }
    };

    fetchReports(true);

    const poller = setInterval(() => {
      fetchReports(false);
    }, 10000);

    return () => {
      isMounted = false;
      clearInterval(poller);
    };
  }, [activeTab, addNotification, updateDerivedMetrics]);

  const pendingUrgency = useMemo(() => {
    const pendingReports = reports.filter((r) => r.status === "pending");
    return {
      critical: pendingReports.filter((r) => r.severity === "critical").length,
      high: pendingReports.filter((r) => r.severity === "high").length,
    };
  }, [reports]);

  const visibleReports = useMemo(() => {
    const normalized = query.trim().toLowerCase();

    return reports.filter((report) => {
      const severityMatch = severityFilter === "all" || report.severity === severityFilter;
      const textMatch =
        normalized.length === 0 ||
        `${report.id} ${report.content} ${report.reason} ${report.details}`.toLowerCase().includes(normalized);

      return severityMatch && textMatch;
    });
  }, [reports, severityFilter, query]);

  const confirmRemove = async () => {
    if (!removeTarget || !actionNote.trim()) return;

    const { ok, status } = await api.post(`/admin/reports/${removeTarget.id}/remove-confession`);
    if (!ok) {
      setFlashMessage(status === 403 ? "Action denied for your role." : "Failed to remove confession.");
      addNotification(`Failed to remove confession for report #${removeTarget.id}.`, "error");
      return;
    }

    setReports((prev) => prev.filter((r) => r.id !== removeTarget.id));
    setStats((prev) => {
      const next = {
        pending: Math.max(0, prev.pending - 1),
        reviewed: prev.reviewed + 1,
        dismissed: prev.dismissed,
      };
      updateDerivedMetrics(next);
      return next;
    });
    setFlashMessage(`Confession #${removeTarget.confessionId} removed and report marked reviewed.`);
    addNotification(`Report #${removeTarget.id} was resolved by removing confession #${removeTarget.confessionId}.`, "success");
    setRemoveTarget(null);
    setActionNote("");
  };

  const confirmDismiss = async () => {
    if (!dismissTarget || !actionNote.trim()) return;

    const { ok, status } = await api.post(`/admin/reports/${dismissTarget.id}/dismiss`);
    if (!ok) {
      setFlashMessage(status === 403 ? "Action denied for your role." : "Failed to dismiss report.");
      addNotification(`Failed to dismiss report #${dismissTarget.id}.`, "error");
      return;
    }

    setReports((prev) => prev.filter((r) => r.id !== dismissTarget.id));
    setStats((prev) => {
      const next = {
        pending: Math.max(0, prev.pending - 1),
        reviewed: prev.reviewed,
        dismissed: prev.dismissed + 1,
      };
      updateDerivedMetrics(next);
      return next;
    });
    setFlashMessage(`Report #${dismissTarget.id} dismissed.`);
    addNotification(`Report #${dismissTarget.id} was dismissed.`, "success");
    setDismissTarget(null);
    setActionNote("");
  };

  const applyRestriction = async () => {
    if (!restrictTarget || !restrictReason.trim()) {
      setFlashMessage("Please fill in all restriction fields.");
      return;
    }

    const durationDays = parseInt(restrictDuration, 10) || 7;
    const confessionId = Number(restrictTarget.confessionId);

    if (!Number.isFinite(confessionId) || confessionId <= 0) {
      setFlashMessage("The selected report does not include a valid confession ID.");
      addNotification(`Report #${restrictTarget.id} cannot be used for a restriction request.`, "error");
      return;
    }
    
    setRestrictLoading(true);
    setFlashMessage("Sending restriction request...");
    
    const { ok, status, data } = await api.post("/moderator/restriction-requests", {
      confessionId,
      reason: restrictReason.trim(),
      requestedDurationDays: durationDays,
    });

    if (!ok) {
      if (status === 403) {
        setFlashMessage("Action denied for your role. You need moderator privileges.");
      } else if (status === 401) {
        setFlashMessage("Session expired. Please log in again.");
      } else if (status === 404) {
        setFlashMessage("API endpoint not found. Please contact support.");
      } else {
        setFlashMessage(data?.message || "Failed to send restriction request.");
      }
      addNotification(`Failed to send restriction request for report #${restrictTarget.id}.`, "error");
      setRestrictLoading(false);
      return;
    }

    setFlashMessage(`✓ Restriction request sent to admin for confession #${confessionId}`);
    addNotification(`Restriction request submitted for confession #${confessionId}. Duration: ${durationDays} days`, "success");
    
    setRestrictTarget(null);
    setRestrictReason("");
    setRestrictDuration("7");
    setRestrictLoading(false);
  };

  const openRestrict = (report) => {
    if (!report?.confessionId) {
      addNotification(`Report #${report.id} does not include a confession ID.`, "error");
      setFlashMessage(`Report #${report.id} cannot be restricted without a confession ID.`);
      return;
    }

    setRestrictReason(report.reason || "");
    setRestrictDuration("7");
    setRestrictLoading(false);
    setRestrictTarget(report);
    addNotification(`Started reviewing report #${report.id} for confession restriction.`, "info");
  };

  const openRemove = (report) => {
    setActionNote("");
    setRemoveTarget(report);
    addNotification(`Started reviewing report #${report.id} for confession removal.`, "info");
  };

  const openDismiss = (report) => {
    setActionNote("");
    setDismissTarget(report);
    addNotification(`Started reviewing report #${report.id} for dismissal.`, "info");
  };

  return (
    <div className="mod-root">
      <div className="mod-topbar">
        <div className="mod-title-wrap">
          <h1>Moderator Dashboard</h1>
          <p>Content Moderation and Safety</p>
        </div>
        <div className="mod-user-wrap">
          <span className="mod-online">Online</span>
          <button className="mod-logout" onClick={onLogout}>Logout</button>
        </div>
      </div>

      {flashMessage ? <div className="mod-flash">{flashMessage}</div> : null}
      {loadError ? <div className="mod-flash mod-flash-error">{loadError}</div> : null}

      {notifications.length > 0 ? (
        <section className="mod-live-strip">
          <div className="mod-live-notifications">
            {notifications.map((item) => (
              <p key={item.id} className={`mod-live-item mod-live-${item.level}`}>
                <span>{item.message}</span>
                <small>{item.time}</small>
              </p>
            ))}
          </div>
        </section>
      ) : null}

      <div className="mod-stats-grid">
        <article className="mod-stat mod-stat-pending">
          <h4>Pending Reports</h4>
          <span className="mod-value">{stats.pending}</span>
          <small>Requires immediate attention</small>
        </article>
        <article className="mod-stat mod-stat-reviewed">
          <h4>Actions Taken</h4>
          <span className="mod-value">{stats.reviewed}</span>
          <small>Confessions removed</small>
        </article>
        <article className="mod-stat mod-stat-dismissed">
          <h4>Dismissed</h4>
          <span className="mod-value">{stats.dismissed}</span>
          <small>No violation found</small>
        </article>
        <article className="mod-stat mod-stat-rate">
          <h4>Response Rate</h4>
          <span className="mod-value">{responseRate}%</span>
          <small>Live handled vs total reports</small>
        </article>
      </div>

      <section className="mod-toolbar">
        <input
          className="mod-search"
          type="text"
          placeholder="Search by report ID, content, or reason..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <select
          className="mod-filter"
          value={severityFilter}
          onChange={(e) => setSeverityFilter(e.target.value)}
        >
          <option value="all">All Severities</option>
          <option value="critical">Critical</option>
          <option value="high">High</option>
          <option value="medium">Medium</option>
          <option value="low">Low</option>
        </select>
      </section>

      <section className="mod-tabs">
        <button className={activeTab === "pending" ? "active" : ""} onClick={() => setActiveTab("pending")}>
          Pending <span>{stats.pending}</span>
        </button>
        <button className={activeTab === "reviewed" ? "active" : ""} onClick={() => setActiveTab("reviewed")}>
          Reviewed
        </button>
        <button className={activeTab === "dismissed" ? "active" : ""} onClick={() => setActiveTab("dismissed")}>
          Dismissed
        </button>
      </section>

      {activeTab === "pending" ? (
        <div className="mod-priority-box">
          <h3>Priority Review Required</h3>
          <p>
            {pendingUrgency.critical} critical and {pendingUrgency.high} high severity reports need immediate attention.
            Review time-sensitive reports first.
          </p>
        </div>
      ) : null}

      <section className="mod-report-list">
        {loading ? (
          <div className="mod-empty">Loading reports...</div>
        ) : visibleReports.length === 0 ? (
          <div className="mod-empty">No reports match your current filters.</div>
        ) : (
          visibleReports.map((report) => (
            <article className="mod-report-card" key={report.id}>
              <header className="mod-report-head">
                <div className="mod-report-head-left">
                  <h4>Report #{report.id}</h4>
                  <SeverityBadge severity={report.severity} />
                  <span className="mod-chip">{report.category}</span>
                </div>
                <button className="mod-details-btn" onClick={() => setSelectedReport(report)}>
                  Details
                </button>
              </header>

              <div className="mod-report-meta">
                <span>Reported {report.ageText}</span>
                <span>{report.reportedAt}</span>
                <span>By User {report.reporter}</span>
                <StatusBadge status={report.status} />
              </div>

              <div className="mod-content-box">
                <p className="mod-label">Confession Content:</p>
                <p>{report.content}</p>
              </div>

              <div className="mod-details-box">
                <p><strong>Reason:</strong> {report.reason}</p>
                <p><strong>Details:</strong> {report.details}</p>
              </div>

              {report.status === "pending" ? (
                <div className="mod-actions-row">
                  <button className="mod-btn danger" onClick={() => openRemove(report)}>Remove Confession</button>
                  <button className="mod-btn muted" onClick={() => openDismiss(report)}>Dismiss Report</button>
                  <button className="mod-btn warning" onClick={() => openRestrict(report)}>Restrict User</button>
                </div>
              ) : null}
            </article>
          ))
        )}
      </section>

      {selectedReport ? (
        <Modal title={`Report Details - #${selectedReport.id}`} onClose={() => setSelectedReport(null)}>
          <div className="mod-detail-grid">
            <div>
              <p className="mod-label">Status</p>
              <StatusBadge status={selectedReport.status} />
            </div>
            <div>
              <p className="mod-label">Severity</p>
              <SeverityBadge severity={selectedReport.severity} />
            </div>
          </div>
          <div className="mod-content-box">
            <p className="mod-label">Confession Content</p>
            <p>{selectedReport.content}</p>
          </div>
          <div className="mod-detail-grid two">
            <p><strong>Confession ID:</strong> #{selectedReport.confessionId}</p>
            <p><strong>Category:</strong> {selectedReport.category}</p>
            <p><strong>Report Reason:</strong> {selectedReport.reason}</p>
            <p><strong>Reported By:</strong> User {selectedReport.reporter}</p>
            <p><strong>Reported At:</strong> {selectedReport.reportedAt}</p>
          </div>
          <div className="mod-modal-actions">
            <button className="mod-btn neutral" onClick={() => setSelectedReport(null)}>Close</button>
          </div>
        </Modal>
      ) : null}

      {removeTarget ? (
        <Modal
          title="Remove Confession"
          subtitle="This will permanently remove the confession from the public feed and mark the report as reviewed."
          onClose={() => {
            setRemoveTarget(null);
            setActionNote("");
          }}
        >
          <div className="mod-content-box">
            <p className="mod-label">Reported Content:</p>
            <p>{removeTarget.content}</p>
          </div>
          <div className="mod-details-box">
            <p><strong>Reason:</strong> {removeTarget.reason}</p>
            <p><strong>Severity:</strong> {removeTarget.severity}</p>
            <p><strong>Details:</strong> {removeTarget.details}</p>
          </div>
          <label className="mod-label">Action Note (Required)</label>
          <textarea
            className="mod-textarea"
            placeholder="Describe the action taken and reasoning..."
            value={actionNote}
            onChange={(e) => setActionNote(e.target.value)}
          />
          <div className="mod-modal-actions">
            <button className="mod-btn neutral" onClick={() => setRemoveTarget(null)}>Cancel</button>
            <button className="mod-btn danger" disabled={!actionNote.trim()} onClick={confirmRemove}>
              Remove Confession
            </button>
          </div>
        </Modal>
      ) : null}

      {dismissTarget ? (
        <Modal
          title="Dismiss Report"
          subtitle="This will dismiss the report and keep the confession visible on the feed."
          onClose={() => {
            setDismissTarget(null);
            setActionNote("");
          }}
        >
          <div className="mod-content-box">
            <p className="mod-label">Reported Content:</p>
            <p>{dismissTarget.content}</p>
          </div>
          <div className="mod-details-box">
            <p><strong>Reason:</strong> {dismissTarget.reason}</p>
            <p><strong>Severity:</strong> {dismissTarget.severity}</p>
            <p><strong>Details:</strong> {dismissTarget.details}</p>
          </div>
          <label className="mod-label">Action Note (Required)</label>
          <textarea
            className="mod-textarea"
            placeholder="Describe the action taken and reasoning..."
            value={actionNote}
            onChange={(e) => setActionNote(e.target.value)}
          />
          <div className="mod-modal-actions">
            <button className="mod-btn neutral" onClick={() => setDismissTarget(null)}>Cancel</button>
            <button className="mod-btn muted" disabled={!actionNote.trim()} onClick={confirmDismiss}>
              Dismiss Report
            </button>
          </div>
        </Modal>
      ) : null}

      {restrictTarget ? (
        <Modal
          title="Request User Restriction"
          subtitle="Send a restriction request to the system admin. The admin will decide the duration."
          onClose={() => {
            setRestrictTarget(null);
            setRestrictReason("");
            setRestrictDuration("7");
            setRestrictLoading(false);
          }}
        >
          <div className="mod-details-box">
            <p><strong>Report ID:</strong> #{restrictTarget.id}</p>
            <p><strong>Confession ID:</strong> #{restrictTarget.confessionId}</p>
            <p><strong>Target user:</strong> {restrictTarget.confessionOwnerUsername || "Unknown"}</p>
          </div>

          <label className="mod-label">Restriction Duration</label>
          <select
            className="mod-select"
            value={restrictDuration}
            onChange={(e) => setRestrictDuration(e.target.value)}
          >
            <option value="1">1 day</option>
            <option value="3">3 days</option>
            <option value="7">7 days (1 week)</option>
            <option value="14">14 days (2 weeks)</option>
            <option value="30">30 days</option>
          </select>

          <label className="mod-label">Reason for Restriction (Required)</label>
          <textarea
            className="mod-textarea"
            placeholder="Explain why this user is being restricted..."
            value={restrictReason}
            onChange={(e) => setRestrictReason(e.target.value)}
          />

          <div className="mod-note-box">
            Note: This sends a request to the system admin. The admin will approve or reject the restriction.
          </div>

          <div className="mod-modal-actions">
            <button className="mod-btn neutral" onClick={() => setRestrictTarget(null)}>Cancel</button>
            <button className="mod-btn warning" disabled={!restrictReason.trim() || restrictLoading} onClick={applyRestriction}>
              {restrictLoading ? "Sending..." : "Send Request"}
            </button>
          </div>
        </Modal>
      ) : null}
    </div>
  );
}