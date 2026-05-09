# Whisper Wall Frontend - Render Deployment Guide

This guide walks through deploying the React frontend to Render and connecting it to your backend.

## Prerequisites

- Render account (free tier available at https://render.com)
- GitHub repository with your code pushed
- Backend already deployed to Render (e.g., https://whisper-wall-backend.onrender.com)

## Backend API URL

Your backend is deployed at:
```
https://whisper-wall-backend.onrender.com/api
```

## Deployment Steps

### Step 1: Push Code to GitHub

Ensure all changes are committed and pushed to your GitHub repository:

```bash
git add .
git commit -m "Add Render frontend deployment configuration"
git push origin main
```

### Step 2: Create Render Web Service

1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click **"New +"** → **"Web Service"**
3. Connect your GitHub repository:
   - Search for **lunaria20/whisper-wall**
   - Click **"Connect"**
4. Configure the web service:
   - **Name**: `whisper-wall-frontend` (or your preferred name)
   - **Runtime**: Node
   - **Build Command**: `npm install && npm run build`
   - **Start Command**: `node server.js`
   - **Plan**: Free tier is sufficient for testing

### Step 3: Set Environment Variables

In the Render dashboard for your web service, go to **"Environment"** and add:

```
REACT_APP_API_URL = https://whisper-wall-backend.onrender.com/api
REACT_APP_SUPABASE_URL = https://mqssfwzsisxwpdiujiwl.supabase.co
REACT_APP_SUPABASE_ANON_KEY = sb_publishable_KhZyeNKn6Kg2R9Mk8gx43Q_JcLeBYkg
```

**Note**: Environment variables prefixed with `REACT_APP_` are baked into the frontend build and visible in the browser.

### Step 4: Deploy

1. Click **"Create Web Service"**
2. Render automatically deploys from your GitHub repo
3. Watch the deployment logs - you should see:
   - Dependencies installing
   - React build completing
   - Server starting on port 3000 (or assigned port)

### Step 5: Verify Deployment

Once deployed:
1. Click the URL provided by Render (e.g., `https://whisper-wall-frontend.onrender.com`)
2. Test login with your seeded account:
   - Email: `admin2004@whisperwall.com`
   - Password: `Admin2004**`
3. Verify the app can fetch from the backend (check Network tab in browser DevTools)

## Troubleshooting

### "Port already in use" error
This shouldn't happen on Render, but if testing locally:
```bash
# Kill process on port 3000
npx kill-port 3000
# Then restart
node server.js
```

### Frontend can't reach backend
- Verify `REACT_APP_API_URL` in Render environment is correct
- Check browser DevTools Network tab for API calls
- Ensure backend service is running and accessible

### Build fails
- Check build logs in Render dashboard
- Ensure all dependencies are listed in `package.json`
- Run `npm install && npm run build` locally to test

### Static assets not loading
- The `server.js` file handles serving the build folder
- Ensure `build/` is generated during the build step

## Local Testing Before Deployment

To test the production setup locally:

```bash
# Build the React app
npm run build

# Install Express (if not already installed)
npm install express

# Run the server locally
node server.js
```

Then visit `http://localhost:3000` and test the app.

## Redeploy Changes

After pushing new code to GitHub:
1. Render automatically detects changes
2. Automatically re-runs build and deployment
3. No manual action needed

To force a redeploy without code changes:
1. Go to the web service in Render dashboard
2. Click **"Deploy"** → **"Deploy Latest Commit"**

## Environment Variables Reference

| Variable | Local | Production |
|----------|-------|------------|
| `REACT_APP_API_URL` | `http://localhost:8080/api` | `https://whisper-wall-backend.onrender.com/api` |
| `REACT_APP_SUPABASE_URL` | Your Supabase URL | Same |
| `REACT_APP_SUPABASE_ANON_KEY` | Your Anon Key | Same |

## Project Structure

- `public/` - Static HTML and assets
- `src/` - React source code
- `build/` - Generated production build (created by `npm run build`)
- `server.js` - Express server that serves the React build
- `render.yaml` - Render deployment configuration (optional, for direct use)
- `package.json` - Project dependencies and scripts

## Support

For deployment issues:
- Check Render logs in the dashboard
- Visit [Render Docs](https://render.com/docs)
- Review backend logs at https://whisper-wall-backend.onrender.com for API errors
