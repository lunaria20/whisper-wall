# Supabase Storage Setup Guide

This guide helps you set up Supabase Cloud Storage for profile pictures to improve upload performance.

## Why Supabase Storage?

- **Faster uploads**: Images are stored in cloud storage instead of the database
- **Better performance**: Reduces payload size from base64 encoding (33% overhead)
- **Scalability**: Dedicated storage infrastructure instead of database bloat
- **Automatic fallback**: If Supabase is not configured, the app falls back to base64 encoding

## Setup Steps

### 1. Create Supabase Project

If you don't have a Supabase project yet:
- Go to [https://app.supabase.com](https://app.supabase.com)
- Create a new project (or use your existing one)
- Note the Project URL and Anon Key

### 2. Create Storage Bucket

1. In Supabase dashboard, go to **Storage** (left sidebar)
2. Click **Create a new bucket**
3. Name: `profile-pictures`
4. Make it **Public** (toggle "Public bucket")
5. Click **Create bucket**

### 3. Set Up Bucket Policies

Allow authenticated users to upload:

1. Click on `profile-pictures` bucket
2. Go to **Policies** tab
3. Click **New Policy** > **For full customization**
4. Use this policy:

```javascript
// Allow users to upload files
{
  "identities": ["authenticated"],
  "object": "*",
  "level": "bucket",
  "operations": ["select", "insert", "delete"],
  "using": "true",
  "withCheck": "true"
}
```

Alternatively, use these simpler individual policies:

**Upload Policy:**
- Policy: `ALLOW INSERT`
- Target roles: `authenticated`

**Delete Policy:**
- Policy: `ALLOW DELETE`
- Target roles: `authenticated`

**Read Policy (Public):**
- Policy: `ALLOW SELECT`
- Target roles: `anon, authenticated`

### 4. Configure Frontend Environment Variables

1. Open `front-end/.env.local` (create if doesn't exist)
2. Add these variables:

```env
REACT_APP_SUPABASE_URL=https://your-project.supabase.co
REACT_APP_SUPABASE_ANON_KEY=your-anon-key-here
```

3. Get your values from Supabase:
   - Go to **Settings** > **API**
   - Copy **Project URL** and **Anon Key**

4. Restart your React app: `npm start`

### 5. Test It Out

1. Run the app: `npm start`
2. Go to Profile page
3. Upload a new profile picture
4. Check browser console for messages:
   - `"Profile picture uploaded to cloud storage"` = Success (URL stored)
   - `"Profile picture stored locally as base64"` = Fallback (Supabase not configured)

## Troubleshooting

### Images not uploading to Supabase?

**Check 1: Environment variables loaded**
- Restart `npm start` after adding `.env.local`
- Verify in browser console

**Check 2: Bucket exists and is public**
- Go to Storage > profile-pictures
- Make sure bucket toggle is **PUBLIC**

**Check 3: Policies are set**
- Check Storage > profile-pictures > Policies
- Ensure `INSERT` and `SELECT` policies exist

**Check 4: CORS Configuration**
If you get CORS errors:
1. Go to Settings > API > CORS
2. Add your frontend URL to allowed origins
3. Example: `http://localhost:3000`

### Images display as broken links?

The storage URL format should be:
```
https://your-project.supabase.co/storage/v1/object/public/profile-pictures/username/uuid.jpg
```

If images show broken links, the bucket might be set to **PRIVATE**. Make sure it's **PUBLIC**.

## Fallback Behavior

If Supabase is not configured:
- Images are automatically stored as **base64 in the database** (original behavior)
- No setup required, everything works out of the box
- Performance is slower due to 33% base64 overhead

## File Compression

All images are automatically compressed:
- **Max size**: 500KB after compression
- **Max resolution**: 800x800px
- **Quality**: 70%

This happens before uploading to Supabase or converting to base64.

## Image Limits

- **Max upload size**: 10MB (before compression)
- **Max compressed size**: 500KB (after compression)
- **Supported formats**: JPEG, PNG, WebP, GIF

## Database Schema

The `profilePicture` field in the `users` table can store:
- **Base64 data URL**: `data:image/jpeg;base64,/9j/4AAQSkZJRg...` (fallback)
- **Supabase public URL**: `https://your-project.supabase.co/storage/v1/object/public/profile-pictures/username/uuid.jpg`

The frontend automatically detects the format and displays correctly.

## Performance Comparison

| Method | Upload Time | Size | Database Impact |
|--------|------------|------|------------------|
| Base64 (old) | ~5-10s for 2MB image | 33% larger | Bloats database |
| Compressed Base64 | ~1-2s for 500KB image | Reduced | Still in database |
| Supabase Storage | ~0.5-2s for 500KB image | Minimal | Only URL stored |

## Additional Resources

- [Supabase Storage Docs](https://supabase.com/docs/guides/storage)
- [Supabase CORS Setup](https://supabase.com/docs/guides/auth#cors-configuration)
