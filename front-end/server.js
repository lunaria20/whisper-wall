/**
 * Simple Express server to serve the React static build on Render.
 * 
 * This is a static file server ONLY - it does NOT process API requests.
 * All API calls from the React frontend go directly to the backend:
 * https://whisper-wall-backend.onrender.com/api
 * 
 * Architecture:
 * Browser → React App (this server) → Backend API (Render Spring Boot)
 *                                  → Supabase (optional, for image storage)
 */

const express = require('express');
const path = require('path');
const app = express();

const PORT = process.env.PORT || 3000;

// Serve static files from the React build directory
app.use(express.static(path.join(__dirname, 'build')));

// Handle client-side routing - redirect all non-file requests to index.html
// This allows React Router to work on page refresh/direct URLs
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'build', 'index.html'));
});

app.listen(PORT, () => {
  console.log(`✓ Frontend static server running on port ${PORT}`);
  console.log(`✓ Backend API at https://whisper-wall-backend.onrender.com/api`);
});
