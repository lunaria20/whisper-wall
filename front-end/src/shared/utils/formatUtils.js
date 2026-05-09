// Utility functions shared across the app

export const formatRelativeTime = (timestamp) => {
  const now = new Date();
  const date = new Date(timestamp);
  const seconds = Math.floor((now - date) / 1000);

  if (seconds < 60) return `${seconds}s ago`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}d ago`;
  const weeks = Math.floor(days / 7);
  if (weeks < 4) return `${weeks}w ago`;
  const months = Math.floor(days / 30);
  if (months < 12) return `${months}mo ago`;
  const years = Math.floor(months / 12);
  return `${years}y ago`;
};

export const sanitizeId = (rawId) => {
  // Converts composite IDs like "123:1" to just "123"
  if (!rawId) return "";
  return String(rawId).split(":")[0];
};

export const mapConfessionToCard = (confession, username) => ({
  id: sanitizeId(confession.id),
  text: confession.content || confession.text,
  category: confession.category,
  createdAt: confession.createdAt,
  likes: confession.reactionCount || 0,
  commentsCount: confession.commentsCount || 0,
  isOwn: username && (confession.username === username || confession.userId === username),
  time: formatRelativeTime(confession.createdAt),
});

export const mergeConfessionLists = (existing, newList) => {
  const map = new Map();
  existing.forEach((c) => map.set(sanitizeId(c.id), c));
  newList.forEach((c) => map.set(sanitizeId(c.id), c));
  return Array.from(map.values());
};
