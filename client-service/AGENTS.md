# Repository Guidelines

## Project Structure & Module Organization
- React + TypeScript source code lives in `src/`; `src/main.tsx` boots Vite, while `src/App.tsx` defines the global shell and loads styles from `App.css` and `index.css`.
- Group features under `src/features/<domain>` with their components, hooks, and styles (e.g., `src/features/auth/AuthForm.tsx`). Export public APIs through a local `index.ts` to keep imports shallow.
- Imported media belongs in `src/assets/`; static files that need no bundling belong in `public/`. Never commit generated `dist/` artifacts.

## Build, Test, and Development Commands
- `npm install` — install dependencies (Node 18 or newer).
- `npm run dev` — launch the Vite dev server with React Fast Refresh.
- `npm run build` — execute `tsc -b` followed by `vite build` to emit production assets into `dist/`.
- `npm run preview` — serve the latest build locally for sanity checks.
- `npm run lint` — run ESLint on the workspace; resolve or justify every warning.

## Coding Style & Naming Conventions
- Use 2-space indentation, favor `const`, and author React 19 function components with hooks. Derive state with hooks before considering context or external stores.
- Components are PascalCase, hooks start with `use`, and CSS filenames mirror their component (`Navbar.css`).
- Prefer feature-relative imports and lean on barrel files to avoid brittle `../../..` paths.
- Scope styles with module files or CSS variables; skip global overrides unless adjusting theme tokens.

## Testing Guidelines
- Use Vitest plus React Testing Library when introducing tests. Name files `<Component>.test.tsx`, co-locate them with the component, and cover success, error, and accessibility flows.
- Target at least 80% coverage on new or modified surface area. Document intentional gaps in the PR description.
- Run `npx vitest run` for CI coverage or `npx vitest --watch` while iterating locally.

## Commit & Pull Request Guidelines
- Follow Conventional Commits (`feat:`, `fix:`, `refactor:`, etc.) and append issue references like `#12` when applicable. Keep each commit focused and lint-clean.
- Pull requests must describe the change, list verification steps (`npm run build`, screenshots for UI adjustments), and link related issues or tickets.
- Rebase on `main`, request review promptly, and wait for automated checks (when available) before merging.

## Security & Configuration Tips
- Store secrets in `.env.local`; only expose values prefixed with `VITE_`. Example: `VITE_API_BASE=https://api.example.com`.
- Document configuration changes in the README and include safe default placeholders for new variables.
