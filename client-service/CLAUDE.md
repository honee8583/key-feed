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
- **세미콜론:** 사용하지 않음
- **따옴표:** 싱글 쿼트(`'`) 사용
- **컴포넌트:** PascalCase 함수형 컴포넌트 + 훅 사용
- **훅:** `use` 접두사 사용
- **임포트:** 배럴 파일을 통한 기능 상대 경로
- **CSS:** 컴포넌트와 함께 위치 (예: `LoginPage.tsx` 옆에 `LoginPage.css`)
- **커밋:** Conventional Commits 형식 (`feat:`, `fix:`, `refactor:`)
  - 커밋 메시지는 **한국어**로 작성 (예: `feat: 로그인 기능 추가`)
  - 제목은 **명령조**로 작성 (예: `fix: 버그 수정` ✓, `fix: 버그를 수정했습니다` ✗)

### 공통 유틸리티 재사용

중복 코드 방지를 위해 기존 유틸리티를 재사용하세요:
- **날짜 포맷팅:** `src/utils/dateUtils.ts` (`formatRelativePublishedAt`, `formatAbsoluteDate`)
- **상수:** `src/constants/config.ts` (`FEED_PAGE_SIZE`, `NEW_CONTENT_WINDOW_MS`)

### 타이머/이벤트 리스너 cleanup 패턴

`setTimeout`, `setInterval`, 이벤트 리스너 사용 시 반드시 cleanup 처리:

```tsx
const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

useEffect(() => {
  return () => {
    if (timerRef.current) {
      clearTimeout(timerRef.current)
    }
  }
}, [])

// 사용
timerRef.current = setTimeout(() => { ... }, 1000)
```

## 알려진 이슈 및 기술 부채

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

## Figma MCP 연결 확인

- Figma URL을 전달받았을 때 **반드시 MCP 서버 연결 상태를 먼저 확인**
- **MCP 서버가 연결되지 않았다면**: "피그마 MCP 서버에 연결되어 있지 않습니다. 먼저 MCP 설정을 확인하고 연결해주세요."라고 응답하고 작업 중단
- 연결 확인 방법: MCP Servers 목록에서 Figma 항목이 `connected` 또는 `running` 상태인지 체크

## Figma 디자인 처리 규칙

- Figma MCP 서버의 URL(예: `https://mcp.figma.com/...`)을 **절대 아이콘/이미지 소스로 사용하지 말기**
- 디자인에서 아이콘 추출 시:
  - 실제 아이콘 SVG 파일을 프로젝트 `public/icons/` 또는 `src/assets/icons/`에 저장
  - import 경로: `import IconName from '@/assets/icons/icon-name.svg'`
  - CSS: `background-image: url('/icons/icon-name.svg')` 또는 `<img src="/icons/icon-name.svg" />`

## 빌드 및 검증 워크플로우

모든 작업 완료 후 반드시 빌드를 수행합니다:

1. `npm run lint` 실행 (필수)
2. `npm run build` 실행
3. 빌드 에러 발생 시: 에러 로그 표시 → 수정 → 재빌드
4. 빌드 성공 후: `npm run preview`로 로컬 확인
5. 성공 메시지: "빌드 성공! preview 서버: http://localhost:4173 확인하세요."
