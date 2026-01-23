# CLAUDE.md

이 파일은 Claude Code(claude.ai/code)가 이 저장소의 코드를 작업할 때 참고하는 가이드입니다.

## 개발 명령어

```bash
npm install          # 의존성 설치 (Node 18+ 필요)
npm run dev          # HMR이 포함된 Vite 개발 서버 시작
npm run build        # TypeScript 검사 + 프로덕션 빌드
npm run lint         # ESLint 실행
npm run preview      # 프로덕션 빌드 로컬 미리보기
```

## 환경 설정

런타임 설정을 위해 `.env.local` 파일을 생성하세요:
```
VITE_API_BASE=https://api.example.com
```

지정하지 않으면 기본 API 베이스 URL은 `http://localhost:8000/api`입니다.

## 아키텍처 개요

KeyFeed Client Service는 마이크로서비스 기반 콘텐츠 집계 플랫폼을 위한 React 19 + TypeScript + Vite 프론트엔드입니다. 중앙화된 API 게이트웨이를 통해 백엔드 서비스와 통신합니다.

### 기능 기반 구조

```
src/
├── features/              # 도메인 주도 기능 모듈
│   ├── auth/             # 인증 (로그인, 회원가입, 컨텍스트)
│   ├── home/             # 무한 스크롤이 포함된 메인 피드 표시
│   ├── bookmarks/        # 북마크 관리
│   ├── notifications/    # SSE를 통한 실시간 알림
│   └── profile/          # 사용자 프로필 및 콘텐츠 소스 관리
├── services/             # API 클라이언트 및 스토리지 유틸리티
│   ├── apiClient.ts      # 핵심 HTTP 클라이언트 (인증, 갱신, 큰 정수 처리)
│   ├── authStorage.ts    # 탭 간 동기화가 포함된 토큰 저장
│   ├── authApi.ts        # 인증 엔드포인트
│   ├── feedApi.ts        # 피드 콘텐츠 엔드포인트
│   ├── bookmarkApi.ts    # 북마크 엔드포인트
│   ├── notificationApi.ts # 알림 (REST + SSE)
│   └── sourceApi.ts      # 콘텐츠 소스 관리
├── components/           # 공유 UI 컴포넌트
├── routes/              # 라우팅 설정
└── types/               # TypeScript 타입 정의
```

각 기능은 컴포넌트, 스타일, 배럴 익스포트(`index.ts`)를 포함한 독립적인 모듈입니다.

### 주요 아키텍처 패턴

**인증 흐름:**
- JWT 토큰은 localStorage/sessionStorage에 저장
- `apiClient.ts`에서 401 응답 시 자동 토큰 갱신
- 스토리지 이벤트를 통한 탭 간 인증 상태 동기화
- 컨텍스트 기반 인증 상태 관리 (`AuthContext.tsx`)

**API 클라이언트 (`apiClient.ts`):**
- 자동 Bearer 토큰 주입
- 큰 정수 지원 (16자리 이상의 숫자를 문자열로 변환)
- 메시지 추출을 포함한 중앙화된 에러 처리
- FormData에 대한 Content-Type 자동 감지

**실시간 알림:**
- `EventSourcePolyfill`을 통한 Server-Sent Events (SSE)
- 재연결을 위한 localStorage의 Last-Event-ID 추적
- 이벤트 중복 제거 및 보강

**데이터 페칭:**
- 피드와 알림에 대한 커서 기반 페이지네이션
- 패턴: `await apiClient.request<ResponseType>(path, options)`
- 전역 상태 라이브러리 미사용 (React Context + 로컬 useState)

**라우팅:**
- 보호된 라우트 래퍼가 컨텍스트의 `isAuthenticated` 확인
- 라우트: `/login`, `/signup`, `/home`, `/bookmarks`, `/notifications`, `/profile`, `/profile/sources`
- 루트 `/`는 인증 상태에 따라 리다이렉트

## 코드 스타일 가이드라인

- **들여쓰기:** 2칸 스페이스
- **컴포넌트:** PascalCase 함수형 컴포넌트 + 훅 사용
- **훅:** `use` 접두사 사용
- **임포트:** 배럴 파일을 통한 기능 상대 경로
- **CSS:** 컴포넌트와 함께 위치 (예: `LoginPage.tsx` 옆에 `LoginPage.css`)
- **커밋:** Conventional Commits 형식 (`feat:`, `fix:`, `refactor:`)

## 알려진 이슈 및 기술 부채

### ESLint 에러 (총 5개)
1. `src/features/auth/AuthContext.tsx:76` - Fast refresh 경고 (`useAuth` 훅 추출 필요)
2. `src/features/notifications/NotificationPage.tsx:22` - 사용하지 않는 `nextCursorId` 변수
3. `src/features/profile/SourceManagementPage.tsx:34` - `finally` 블록에서의 안전하지 않은 `return`
4. `src/features/profile/SourceManagementPage.tsx:144` - 사용하지 않는 `error` 변수
5. `src/services/apiClient.ts:195` - 정규식에서 불필요한 이스케이프 `\}`

### 누락된 인프라
- 테스트 스위트 미구현 (Vitest + React Testing Library 고려)
- 런타임 타입 검증 미구현 (Zod 고려)
- 코드 스플리팅 미구현 (라우트에 React.lazy 고려)
- 이미지 최적화 미구현

## 마이크로서비스 통합

이 클라이언트 서비스는 다음 백엔드 서비스들과 함께 모노레포의 일부입니다:
- **gateway** - API 게이트웨이 라우팅
- **identity-service** - 사용자 인증
- **feed-service** - 콘텐츠 집계
- **notification-service** - 실시간 알림
- **match-service** - 콘텐츠 추천
- **crawl-service** - 웹 크롤링
- **elastic-search** - 검색 엔진

현재 브랜치: `feat#132`
PR용 메인 브랜치: `dev`

## 디자인 참조

로그인/회원가입 화면은 [KeyFeed Figma 디자인](https://www.figma.com/design/6m9N0rXf0tr5vWtAbIeyCM/KeyFeed?node-id=1-2)을 구현합니다. 디자인 스펙이 변경되면 `src/features/auth/LoginPage.css`를 업데이트하세요.
