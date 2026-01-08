# KeyFeed Client Service

The client service renders the KeyFeed login experience with routing support and centralized API helpers. The UI mirrors the latest Figma reference, including the gradient hero, social login buttons, email/password form, and responsive layout for mobile-sized viewports while staying friendly on larger screens.

## Getting started

```bash
npm install
npm run dev
```

Run `npm run build` before shipping and `npm run lint` to keep the codebase clean.

## Environment variables

All runtime configuration should be stored in `.env.local`.

```
VITE_API_BASE=https://api.example.com
```

`VITE_API_BASE` is optional in development. When it is not provided the client defaults to `http://localhost:8000/api`, which matches the provided KeyFeed auth backend.

## Project structure

- `src/features/auth/` – login screen UI (`LoginPage.tsx`) with scoped styles (`LoginPage.css`).
- `src/routes/` – app level routing (currently redirects `/` to `/login` and handles unknown routes).
- `src/services/` – API client wrapper (`apiClient.ts`) and auth specific helpers (`authApi.ts`).

`src/main.tsx` boots the React app, `src/App.tsx` wires the router shell, and global styles live in `App.css` / `index.css`.

## Routes

- `/login` – renders the login flow.
- `/signup` – renders the signup experience with email-based registration and agreements.
- `/` – redirects to `/login`.
- Any unknown path – shows a lightweight “not found” message with a link back to login.

## Design reference

The login screen implements the visual language from the [KeyFeed Figma file](https://www.figma.com/design/6m9N0rXf0tr5vWtAbIeyCM/KeyFeed?node-id=1-2). Update `src/features/auth/LoginPage.css` if the design specs change.
