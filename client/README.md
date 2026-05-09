# Sprintify Frontend

Minimal functionality-first Next.js client for Sprintify.

## Setup

```bash
npm install
npm run dev
```

Optional environment variable:

```bash
NEXT_PUBLIC_API_BASE_URL=/api
API_GATEWAY_URL=http://localhost:8080
```

- `NEXT_PUBLIC_API_BASE_URL` defaults to `/api` (same-origin in browser).
- `API_GATEWAY_URL` is used by Next.js rewrites to proxy `/api/*` to the API Gateway.
- This prevents browser CORS issues because frontend requests stay same-origin.
