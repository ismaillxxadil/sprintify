# Run the New Frontend (Client)

This project’s new frontend is in the `client` folder and uses Next.js.

## 1) Prerequisites

- Node.js 18+ (recommended: latest LTS)
- npm
- API Gateway running (default: `http://localhost:8080`)

## 2) Go to the client folder

```bash
cd /home/runner/work/sprintify/sprintify/client
```

## 3) Install dependencies

```bash
npm install
```

## 4) Configure environment (optional)

By default, the client uses:

- `NEXT_PUBLIC_API_BASE_URL=/api`
- `API_GATEWAY_URL=http://localhost:8080`

You can set these before running:

```bash
export NEXT_PUBLIC_API_BASE_URL=/api
export API_GATEWAY_URL=http://localhost:8080
```

## 5) Start development server

```bash
npm run dev
```

Open: `http://localhost:3000`

## 6) Validate frontend

```bash
npm run lint
npm run build
```

## Notes

- Browser requests use same-origin `/api` paths.
- Next.js rewrites proxy `/api/*` to the API Gateway using `API_GATEWAY_URL`.
- This setup helps prevent CORS and frontend config issues.
