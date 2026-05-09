# Sprintify Frontend

Next.js frontend for the Sprintify project management platform.

## Setup

```bash
npm install
npm run dev
```

Visit `http://localhost:3000`

## Features

### Authentication
- Login with email and password
- Sign up new account
- JWT token-based authentication
- Automatic redirect based on user role

### User Dashboard
- View pending project invites
- Accept or decline invites
- Create new projects
- View activity logs

### Admin Dashboard
- View all users
- Ban users (specify days)
- Delete users
- View system logs

## API Integration

The frontend communicates with the backend via API Gateway:
- Base URL: `http://localhost:8080`
- Routes: `/identity-service/*`, `/project-service/*`, `/log-analysis-service/*`

## Project Structure

```
src/
  app/
    login/          # Login page
    signup/         # Sign up page
    user/           # User dashboard
    admin/          # Admin dashboard
  lib/
    api.ts          # API client
    auth-store.ts   # Auth state management
    hooks.ts        # Custom hooks
```

## Dependencies

- **Next.js 14** - React framework
- **Axios** - HTTP client
- **Zustand** - State management
- **React Hot Toast** - Notifications
- **Tailwind CSS** - Styling
- **Lucide React** - Icons
