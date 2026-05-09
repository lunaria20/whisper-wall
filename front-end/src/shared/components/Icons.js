// Shared Icon Components

export const IconHeart = ({ filled }) => (
  <svg viewBox="0 0 24 24" width="18" height="18" fill={filled ? "#E53935" : "none"} stroke={filled ? "#E53935" : "#aaa"} strokeWidth="2">
    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
  </svg>
);

export const IconFlag = () => (
  <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#aaa" strokeWidth="2">
    <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z" /><line x1="4" y1="22" x2="4" y2="15" />
  </svg>
);

export const IconPlus = () => (
  <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M19 13H13v6h-2v-6H5v-2h6V5h2v6h6v2z" /></svg>
);

export const IconComment = () => (
  <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#aaa" strokeWidth="2">
    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
  </svg>
);
