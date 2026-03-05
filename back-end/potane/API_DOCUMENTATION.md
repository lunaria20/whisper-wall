# WhisperWall Backend API Documentation

## Overview
This is a Spring Boot REST API backend for the WhisperWall anonymous confession platform. It provides all necessary endpoints for user authentication, managing confessions, comments, reactions, and reporting.

## Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- Node.js (for the frontend, optional for backend)

## Installation & Setup

### 1. Database Setup
```sql
CREATE DATABASE whisperwall;
USE whisperwall;
```

### 2. Update Application Properties
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/whisperwall?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
```

### 3. Build the Project
```bash
cd back-end/potane
mvn clean install
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8080/api`

## API Endpoints

### Authentication (`/api/auth`)
- **POST /auth/register** - Register a new user
  ```json
  {
    "username": "user1",
    "email": "user1@example.com",
    "password": "password123",
    "displayName": "User One"
  }
  ```
  
- **POST /auth/login** - Login user
  ```json
  {
    "username": "user1",
    "password": "password123"
  }
  ```

### Users (`/api/users`)
- **GET /users/{id}** - Get user by ID
- **GET /users/username/{username}** - Get user by username
- **POST /users/{userId}/block/{blockedUserId}** - Block a user
- **POST /users/{userId}/unblock/{blockedUserId}** - Unblock a user

### Confessions (`/api/confessions`)
- **POST /confessions** - Create a new confession (Requires authentication)
  ```json
  {
    "content": "I am feeling lonely today...",
    "category": "feelings",
    "mood": "sad"
  }
  ```
  
- **GET /confessions/{id}** - Get confession by ID
- **GET /confessions/public** - Get all public confessions (Paginated)
  ```
  ?page=0&size=10&sortBy=createdAt
  ```
  
- **GET /confessions/user/{userId}** - Get user's confessions
- **PUT /confessions/{id}** - Update confession (Requires authentication & ownership)
- **DELETE /confessions/{id}** - Delete confession (Requires authentication & ownership)

### Comments (`/api/comments`)
- **POST /comments/confession/{confessionId}** - Create comment (Requires authentication)
  ```json
  {
    "content": "I understand how you feel..."
  }
  ```
  
- **GET /comments/confession/{confessionId}** - Get confession's comments
- **GET /comments/user/{userId}** - Get user's comments
- **PUT /comments/{id}** - Update comment (Requires authentication & ownership)
- **DELETE /comments/{id}** - Delete comment (Requires authentication & ownership)

### Reactions (`/api/reactions`)
- **POST /reactions/confession/{confessionId}** - Add reaction (Requires authentication)
  ```json
  {
    "reactionType": "heart"
  }
  ```
  
- **GET /reactions/confession/{confessionId}** - Get confession reactions
- **DELETE /reactions/confession/{confessionId}** - Remove reaction (Requires authentication)

### Reports (`/api/reports`)
- **POST /reports/confession/{confessionId}** - Report confession (Requires authentication)
  ```json
  {
    "reason": "Inappropriate content",
    "description": "This confession contains offensive language"
  }
  ```
  
- **GET /reports/confession/{confessionId}** - Get confession reports

### Admin (`/api/admin`)
- **GET /admin/reports/pending** - Get pending reports (Admin only)
- **POST /admin/reports/{reportId}/resolve** - Resolve report (Admin only)
- **POST /admin/reports/{reportId}/dismiss** - Dismiss report (Admin only)

## Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

The token is received from the login/register endpoints.

## Database Schema
The application uses JPA/Hibernate with auto-DDL configuration. Default is `create-drop` which creates the schema on startup and drops it on shutdown.

For production, change in `application.properties`:
```properties
spring.jpa.hibernate.ddl-auto=validate
```

### Tables:
- `users` - User accounts
- `roles` - User roles (USER, ADMIN, MODERATOR)
- `confessions` - Anonymous confessions
- `comments` - Comments on confessions
- `reactions` - Reactions/emotions to confessions
- `reports` - Content reports
- `user_roles` - User-Role mapping
- `blocked_users` - User blocking relationships

## Default Roles
The application automatically creates these roles on startup:
- **USER** - Standard user
- **ADMIN** - Administrator with moderation capabilities
- **MODERATOR** - Content moderator

## Security Features
- JWT token-based authentication
- BCrypt password encryption
- CORS configuration for frontend integration
- Role-based access control
- Request validation with Jakarta Validation API

## Error Handling
The API returns appropriate HTTP status codes:
- `200 OK` - Request succeeded
- `201 Created` - Resource created successfully
- `204 No Content` - Request succeeded with no response body
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Access denied
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists
- `500 Internal Server Error` - Server error

## API Documentation
Once the application is running, you can access the Swagger UI (OpenAPI documentation) at:
```
http://localhost:8080/api/swagger-ui.html
```

## Frontend Integration
The frontend should call these endpoints:

1. **Register/Login**: `POST /api/auth/register` or `POST /api/auth/login`
2. **Post Confession**: `POST /api/confessions`
3. **Get Confessions**: `GET /api/confessions/public`
4. **Add Comment**: `POST /api/comments/confession/{confessionId}`
5. **Add Reaction**: `POST /api/reactions/confession/{confessionId}`
6. **Report Content**: `POST /api/reports/confession/{confessionId}`

## Environment Variables (Optional)
You can override properties with environment variables:
```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://your-host:3306/whisperwall
export SPRING_DATASOURCE_USERNAME=your_user
export SPRING_DATASOURCE_PASSWORD=your_password
export APP_JWT_SECRET=your-long-secret-key
```

## Troubleshooting

### Database Connection Issues
- Ensure MySQL is running
- Check database credentials in `application.properties`
- Verify the database exists

### Port Already in Use
Change the port in `application.properties`:
```properties
server.port=8081
```

### JWT Errors
- Ensure token is included in Authorization header
- Check token format: `Bearer <token>`
- Verify token hasn't expired

## Development Notes
- Database is created/dropped on each startup (development mode)
- Default JWT expiration: 24 hours (86400000 ms)
- Confessions are stored with creator information for moderation
- Comments and reactions have approval workflow
- Reports are tracked with status (PENDING, REVIEWED, RESOLVED, DISMISSED)

## Next Steps
1. Start the backend server
2. Connect your React frontend to the backend
3. Test API endpoints using Postman or Swagger UI
4. Deploy to production with proper configuration

## Support
For issues or questions, check the application logs for detailed error messages.
