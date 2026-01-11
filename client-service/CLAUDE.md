# CLAUDE.md 

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

```bash
npm install          # Install dependencies (Node 18+)
npm run dev          # Start Vite dev server with HMR
npm run build        # TypeScript check + production build
npm run lint         # Run ESLint
npm run preview      # Preview production build locally
```

## Environment Configuration

Create `.env.local` for runtime configuration:
```
VITE_API_BASE=https://api.example.com
```

Default API base URL is `http://localhost:8000/api` when not specified.

## Architecture Overview

KeyFeed Client Service is a React 19 + TypeScript + Vite frontend for a microservices-based content aggregation platform. It communicates with backend services via a centralized API gateway.

### Feature-Based Structure

```
src/
├── features/              # Domain-driven feature modules
│   ├── auth/             # Authentication (login, signup, context)
│   ├── home/             # Main feed display with infinite scroll
│   ├── bookmarks/        # Bookmark management
│   ├── notifications/    # Real-time notifications via SSE
│   └── profile/          # User profile & content source management
├── services/             # API clients & storage utilities
│   ├── apiClient.ts      # Core HTTP client (auth, refresh, large integers)
│   ├── authStorage.ts    # Token persistence with cross-tab sync
│   ├── authApi.ts        # Authentication endpoints
│   ├── feedApi.ts        # Feed content endpoints
│   ├── bookmarkApi.ts    # Bookmark endpoints
│   ├── notificationApi.ts # Notifications (REST + SSE)
│   └── sourceApi.ts      # Content source management
├── components/           # Shared UI components
├── routes/              # Routing configuration
└── types/               # TypeScript definitions
```

Each feature is self-contained with components, styles, and barrel exports (`index.ts`).

### Key Architectural Patterns

**Authentication Flow:**
- JWT tokens stored in localStorage/sessionStorage
- Automatic token refresh on 401 responses in `apiClient.ts`
- Cross-tab auth state sync via storage events
- Context-based auth state (`AuthContext.tsx`)

**API Client (`apiClient.ts`):**
- Automatic Bearer token injection
- Large integer support (converts >16 digit numbers to strings)
- Centralized error handling with message extraction
- Content-Type auto-detection for FormData

**Real-Time Notifications:**
- Server-Sent Events (SSE) via `EventSourcePolyfill`
- Last-Event-ID tracking in localStorage for reconnection
- Event deduplication and enrichment

**Data Fetching:**
- Cursor-based pagination for feeds and notifications
- Pattern: `await apiClient.request<ResponseType>(path, options)`
- No global state library (React Context + local useState)

**Routing:**
- Protected route wrapper checks `isAuthenticated` from context
- Routes: `/login`, `/signup`, `/home`, `/bookmarks`, `/notifications`, `/profile`, `/profile/sources`
- Root `/` redirects based on auth state

## Code Style Guidelines

- **Indentation:** 2 spaces
- **Components:** PascalCase function components with hooks
- **Hooks:** Prefix with `use`
- **Imports:** Feature-relative with barrel files
- **CSS:** Co-located with components (e.g., `LoginPage.css` next to `LoginPage.tsx`)
- **Commits:** Conventional Commits format (`feat:`, `fix:`, `refactor:`)

## Known Issues & Technical Debt

### ESLint Errors (5 total)
1. `src/features/auth/AuthContext.tsx:76` - Fast refresh warning (extract `useAuth` hook)
2. `src/features/notifications/NotificationPage.tsx:22` - Unused `nextCursorId` variable
3. `src/features/profile/SourceManagementPage.tsx:34` - Unsafe `return` in `finally` block
4. `src/features/profile/SourceManagementPage.tsx:144` - Unused `error` variable
5. `src/services/apiClient.ts:195` - Unnecessary escape `\}` in regex

### Code Quality Issues
- **Duplicate functions:** `formatRelativePublishedAt` and `formatAbsoluteDate` duplicated in `MainPage.tsx` and `NotificationPage.tsx` - should be extracted to `src/utils/dateUtils.ts`
- **Magic numbers:** `FEED_PAGE_SIZE`, `NEW_CONTENT_WINDOW_MS` hardcoded in components - should move to `src/constants/config.ts`
- **Complex components:** `NotificationPage.tsx` (294 lines) and `MainPage.tsx` (311 lines) - consider extracting custom hooks (`useNotifications`, `useFeed`)
- **No memoization:** Missing `React.memo`, `useMemo`, `useCallback` optimizations
- **Inconsistent error handling:** Mix of toast messages and console.error

### Missing Infrastructure
- No test suite (consider Vitest + React Testing Library)
- No runtime type validation (consider Zod)
- No code splitting (consider React.lazy for routes)
- No image optimization

## Integration with Microservices

This client service is part of a larger monorepo with these backend services:
- **gateway** - API gateway routing
- **identity-service** - User authentication
- **feed-service** - Content aggregation
- **notification-service** - Real-time notifications
- **match-service** - Content recommendation
- **crawl-service** - Web scraping
- **elastic-search** - Search engine

Current branch: `feat#38` (bookmark page features)
Main branch for PRs: `dev`

## Design Reference

Login/signup screens implement the [KeyFeed Figma design](https://www.figma.com/design/6m9N0rXf0tr5vWtAbIeyCM/KeyFeed?node-id=1-2). Update `src/features/auth/LoginPage.css` when design specs change.
