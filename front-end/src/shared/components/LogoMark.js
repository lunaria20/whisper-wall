import React, { useState } from "react";

export function LogoMark() {
  const [imageError, setImageError] = useState(false);

  if (imageError) {
    return (
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 200 200"
        width="100%"
        height="100%"
        fill="none"
      >
        <rect width="200" height="200" fill="#6C3CE1" rx="40" />
        <path
          d="M 60 80 Q 100 120 140 80"
          stroke="white"
          strokeWidth="12"
          strokeLinecap="round"
          fill="none"
        />
        <circle cx="75" cy="60" r="6" fill="white" />
        <circle cx="125" cy="60" r="6" fill="white" />
      </svg>
    );
  }

  return (
    <img
      src="/logo.png"
      alt="WhisperWall Logo"
      onError={() => setImageError(true)}
      width="100%"
      height="100%"
      style={{ display: "block" }}
    />
  );
}
