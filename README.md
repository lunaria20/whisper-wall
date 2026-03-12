# 💜 WhisperWall
*A safe place for anonymous voices.*

WhisperWall is a **secure anonymous confession platform** where users can freely share thoughts and experiences without revealing their identity.

Instead of private messaging, confessions are posted to a **shared community feed** where others can read, react, and offer support.

The platform focuses on **responsible anonymity**, combining privacy with moderation tools and security controls.

---

# 🌸 Theme

WhisperWall follows a **Violet & Lavender aesthetic** to create a calm and welcoming environment.

| Color | Hex Code |
|------|------|
| Lavender | `#E6E6FA` |
| Soft Violet | `#C8A2C8` |
| Deep Violet | `#7F5AF0` |
| Dark Background | `#1E1B2E` |
| Accent | `#B57EDC` |

---

# 📌 Project Overview

WhisperWall allows users to:

- Share **anonymous confessions**
- Read posts from a **community feed**
- React to confessions to show support
- **Report harmful or inappropriate content**
- Maintain community safety through **moderator tools**

User identities are **never displayed publicly**, ensuring anonymity while still allowing moderation through authenticated accounts.

---

# 🎯 Objectives

- Provide **secure user registration and login**
- Allow **anonymous confession posting**
- Enable reactions to confessions
- Allow users to **report inappropriate posts**
- Provide moderator tools to **review reports and manage content**
- Protect user data using **role-based access control**
- Prevent misuse through **validation and abuse controls**

---

# 📦 Project Scope

## ✅ Included Features

- User registration, log in, log out  
- Anonymous confession posting  
- Delete own confession  
- Public confession feed  
- Like/support reactions  
- Report inappropriate content  
- Moderator dashboard  
- React web application  
- Kotlin Android application  
- REST API backend  
- PostgreSQL database  

## ❌ Excluded Features

- Private messaging
- AI automated moderation
- Email notifications
- Push notifications
- Payment systems
- Large-scale public deployment

WhisperWall is designed mainly for **small communities such as schools or campus environments**.

---

# 👥 Primary Users

## Users
- Create accounts
- Post anonymous confessions
- React to confessions
- Report harmful content

## Moderators
- Review reports
- Remove harmful posts
- Manage moderation actions

---

# 🧭 Core User Journeys

## 1️⃣ New User Posting a Confession

1. Open WhisperWall
2. Click **Sign Up**
3. Enter email and password
4. Account is created
5. User enters confession feed
6. Click **Post a Confession**
7. Submit anonymous confession
8. Confession appears in feed

---

## 2️⃣ Returning User Interaction

1. User logs in
2. Views confession feed
3. Reads posts
4. Scrolls through confessions
5. Likes/supports posts
6. Reports inappropriate content if necessary
7. Logs out

---

## 3️⃣ Reporting Content

1. User reads a confession
2. Clicks **Report**
3. Selects a reason
4. Submits report
5. Confirmation message appears

---

## 4️⃣ Moderator Reviewing Reports

1. Moderator logs in
2. Opens **Moderator Dashboard**
3. Views reported posts
4. Reviews report details
5. Removes post or dismisses report
6. Report status updates

---

## 5️⃣ Restricted User Attempting to Post

1. User attempts to submit confession
2. System detects restriction
3. Submission is blocked
4. User sees restriction message
5. User can still browse the feed

---

# 🧩 Feature List (MoSCoW)

## MUST HAVE

- Secure user authentication
- Anonymous confession posting
- Public confession feed
- Reactions (like/support)
- Delete own post
- Reporting system
- Moderator dashboard
- Role-based access control
- Secure database access restrictions
- Posting restrictions for rule violations

---

## SHOULD HAVE

- Responsive web and mobile design
- Pagination or infinite scroll feed
- Error messages and validation
- Basic abuse prevention (rate limiting)

---

## COULD HAVE

- Confession categories
- Trending confessions
- Light/Dark mode

---

## WON'T HAVE

- Private chat
- AI moderation
- Social media login
- Email notifications
- Advanced analytics
- Multi-language support

---

# ⚙️ System Architecture

```
Frontend
 ├── React Web Application
 └── Kotlin Android Application

Backend
 └── REST API Server

Database
 └── PostgreSQL
```

---

# 🔐 Security Features

- HTTPS secure communication
- Token-based authentication
- Password hashing
- Input validation against SQL injection and XSS
- Rate limiting for abuse prevention
- Role-based access control
- Anonymous public posts
- Moderator posting restrictions

---

# 🧠 Functional Features

## User Authentication

Users can register, log in, and log out securely.

**API Endpoints**

```
POST /auth/register
POST /auth/login
POST /auth/logout
```

---

## Anonymous Confession Posting

Users can submit confessions anonymously.

**Features**

- Text submission
- Optional category
- Delete own post
- Restricted users cannot post

**API**

```
POST /confessions
GET /confessions
```

---

## Confession Feed

Displays a chronological list of confessions.

Features include:
- Reaction counts
- Report button
- Scroll or pagination

---

## Reactions (Like / Support)

Users can react to confessions.

Rules:
- One reaction per user per post
- Users may toggle reactions

**API**

```
POST /confessions/{id}/react
```

---

## Reporting System

Users can report inappropriate confessions.

Fields:
- Report reason
- Optional comment

**API**

```
POST /reports
```

---

## Moderator Dashboard

Moderators can manage reports and remove harmful content.

**API**

```
GET /moderator/reports
PUT /moderator/reports/{id}
DELETE /moderator/confessions/{id}
```

---

# 🧪 Acceptance Criteria

### AC-1: User Registration
A new user can create an account and is redirected to the confession feed.

### AC-2: Anonymous Confession Posting
Logged-in users can post confessions without revealing identity.

### AC-3: Report Confession
Users can report confessions and receive confirmation.

### AC-4: Moderator Review
Moderators can remove inappropriate posts.

### AC-5: Posting Restriction
Restricted users cannot submit new confessions.

---

# 🚀 Performance Requirements

- API response time ≤ **3 seconds**
- Web page load ≤ **4 seconds**
- Android cold start ≤ **4 seconds**
- Supports **50 concurrent users**
- Database queries ≤ **1 second**

---

# 💻 Compatibility

**Web Browsers**
- Chrome
- Firefox
- Edge

**Android**
- Version 7.0+

**Operating Systems**
- Windows
- macOS
- Linux

**Screen Sizes**
- Mobile
- Tablet
- Desktop

---

# 🎨 Usability Goals

- New users can register and post within **5 minutes**
- Clear instructions on forms
- Consistent navigation
- Helpful error messages
- Mobile-friendly buttons
- Basic keyboard navigation

---

# 🌿 WhisperWall Philosophy

WhisperWall aims to create a **safe, respectful, and anonymous space** where individuals can express thoughts without fear of judgment.

Privacy, empathy, and community safety are the **core values** behind the platform.

---

💜 *Anonymous voices matter. Whisper safely.*