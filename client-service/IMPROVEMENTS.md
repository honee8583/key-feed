# 프로젝트 개선점 정리

## 📋 목차
1. [즉시 수정 필요 사항](#즉시-수정-필요-사항)
2. [코드 품질 개선](#코드-품질-개선)
3. [성능 최적화](#성능-최적화)
4. [아키텍처 개선](#아키텍처-개선)
5. [테스트 및 품질 보증](#테스트-및-품질-보증)
6. [사용자 경험 개선](#사용자-경험-개선)
7. [보안 및 안정성](#보안-및-안정성)
8. [개발자 경험 개선](#개발자-경험-개선)

---

## 즉시 수정 필요 사항

### 1. ESLint 오류 수정
현재 5개의 ESLint 오류가 존재합니다:

- **`src/features/auth/AuthContext.tsx:76`**: Fast refresh 경고
  - `useAuth` 훅을 별도 파일로 분리하여 해결
  
- **`src/features/notifications/NotificationPage.tsx:22`**: 사용되지 않는 변수
  - `nextCursorId` state 변수 제거 (ref만 사용 중)
  
- **`src/features/profile/SourceManagementPage.tsx:34`**: Unsafe finally 사용
  - `finally` 블록에서 `return` 제거하고 조건문으로 처리
  
- **`src/features/profile/SourceManagementPage.tsx:144`**: 사용되지 않는 변수
  - `error` 변수 제거 또는 사용
  
- **`src/services/apiClient.ts:195`**: 불필요한 이스케이프 문자
  - 정규식에서 `\}` → `}`로 수정

### 2. 타입 안정성 개선
- `NotificationDto`의 `id` 필드가 `number | string | null | undefined`로 불명확
- API 응답 타입 검증 부재 (런타임 검증 필요)
- 옵셔널 체이닝 남용으로 인한 잠재적 버그

---

## 코드 품질 개선

### 1. 코드 중복 제거
**문제점:**
- `formatRelativePublishedAt`와 `formatAbsoluteDate` 함수가 `MainPage.tsx`와 `NotificationPage.tsx`에 중복 정의됨

**개선 방안:**
```typescript
// src/utils/dateUtils.ts 생성
export function formatRelativePublishedAt(value: string): string { ... }
export function formatAbsoluteDate(date: Date): string { ... }
```

### 2. 매직 넘버 및 하드코딩 값 제거
**문제점:**
- `FEED_PAGE_SIZE = 10`, `NEW_CONTENT_WINDOW_MS = 1000 * 60 * 60 * 24` 등이 컴포넌트 내부에 하드코딩
- `DEFAULT_THUMBNAIL` URL이 컴포넌트 내부에 정의

**개선 방안:**
- `src/constants/config.ts` 파일 생성하여 상수 관리
- 환경 변수로 관리 가능한 값은 `.env`로 이동

### 3. 복잡한 컴포넌트 분리
**문제점:**
- `NotificationPage.tsx` (294줄): 너무 많은 책임을 가짐
  - 상태 관리, 데이터 fetching, SSE 구독, 렌더링이 모두 한 파일에
- `MainPage.tsx` (311줄): 유사한 문제

**개선 방안:**
- 커스텀 훅으로 로직 분리 (`useNotifications`, `useFeed`)
- 프레젠테이션 컴포넌트와 컨테이너 컴포넌트 분리

### 4. 에러 처리 일관성
**문제점:**
- 에러 메시지가 컴포넌트마다 다르게 처리됨
- 일부 에러는 `console.error`만 하고 사용자에게 표시하지 않음 (`NotificationPage.tsx:90`)

**개선 방안:**
- 전역 에러 핸들러 또는 에러 바운더리 도입
- 에러 메시지 표준화

---

## 성능 최적화

### 1. React 최적화 기법 미적용
**문제점:**
- `React.memo` 사용 없음 → 불필요한 리렌더링 발생 가능
- `useMemo`, `useCallback` 사용이 제한적
- `NotificationCard`, `HighlightCard` 등이 props 변경 시마다 리렌더링

**개선 방안:**
```typescript
// NotificationCard 최적화
export const NotificationCard = React.memo(({ item }: { item: NotificationItem }) => {
  // ...
}, (prev, next) => prev.item.id === next.item.id)

// HighlightCard 최적화
export const HighlightCard = React.memo(HighlightCardComponent)
```

### 2. 이미지 최적화
**문제점:**
- 이미지 lazy loading은 적용되어 있으나 (`loading="lazy"`)
- 이미지 최적화 도구 (WebP 변환, 리사이징) 미사용
- 썸네일 이미지가 없을 때 placeholder 사용하나 최적화되지 않음

**개선 방안:**
- Vite 플러그인으로 이미지 최적화
- WebP 포맷 지원
- 이미지 CDN 활용 검토

### 3. 번들 크기 최적화
**문제점:**
- 코드 스플리팅 미적용
- 모든 라우트가 한 번들에 포함

**개선 방안:**
```typescript
// AppRouter.tsx
const MainPage = lazy(() => import('../features/home/MainPage'))
const NotificationPage = lazy(() => import('../features/notifications/NotificationPage'))
```

### 4. API 요청 최적화
**문제점:**
- 중복 요청 방지 메커니즘 없음
- 요청 취소 로직이 일부만 구현됨 (`SourceManagementPage`에만 존재)

**개선 방안:**
- React Query 또는 SWR 도입으로 캐싱 및 중복 요청 방지
- AbortController를 통한 요청 취소 표준화

---

## 아키텍처 개선

### 1. 상태 관리
**문제점:**
- Context API만 사용 (인증 상태)
- 서버 상태와 클라이언트 상태가 혼재
- 전역 상태 관리 라이브러리 부재

**개선 방안:**
- React Query 도입으로 서버 상태 관리
- Zustand 또는 Jotai로 클라이언트 상태 관리 (필요 시)

### 2. API 클라이언트 구조
**문제점:**
- 각 API 모듈이 독립적으로 baseUrl 관리 (`notificationApi.ts`, `apiClient.ts`)
- 타입 정의가 각 파일에 분산

**개선 방안:**
- API 타입을 `src/types/api/`로 통합
- baseUrl을 단일 소스에서 관리
- API 응답 래퍼 타입 표준화

### 3. 유틸리티 함수 구조화
**문제점:**
- 유틸리티 함수가 컴포넌트 파일 내부에 정의
- 재사용 가능한 함수들이 분산

**개선 방안:**
```
src/
  utils/
    dateUtils.ts
    stringUtils.ts
    validationUtils.ts
```

### 4. 환경 변수 관리
**문제점:**
- 환경 변수 검증 로직 없음
- 타입 안전한 환경 변수 접근 부재

**개선 방안:**
```typescript
// src/config/env.ts
export const env = {
  apiBase: import.meta.env.VITE_API_BASE || 'http://localhost:8000/api',
} as const

// 런타임 검증 추가
```

---

## 테스트 및 품질 보증

### 1. 테스트 코드 부재
**문제점:**
- 단위 테스트, 통합 테스트, E2E 테스트 모두 없음
- `package.json`에 테스트 스크립트 없음

**개선 방안:**
- Vitest + React Testing Library 설정
- 핵심 기능부터 테스트 작성:
  - API 클라이언트
  - 인증 플로우
  - 날짜 포맷팅 유틸리티
  - 주요 컴포넌트

### 2. 타입 검증
**문제점:**
- 런타임 타입 검증 없음
- API 응답 검증 부재

**개선 방안:**
- Zod 또는 Yup으로 런타임 타입 검증
- API 응답 스키마 검증

### 3. 코드 커버리지
**문제점:**
- 커버리지 측정 도구 없음

**개선 방안:**
- Vitest 커버리지 리포트 설정
- CI/CD에서 커버리지 임계값 설정 (목표: 80%)

---

## 사용자 경험 개선

### 1. 로딩 상태
**문제점:**
- 로딩 상태가 컴포넌트마다 다르게 표시됨
- 스켈레톤 UI 없음

**개선 방안:**
- 공통 로딩 컴포넌트 생성
- 스켈레톤 UI 도입

### 2. 에러 처리 UX
**문제점:**
- 에러 발생 시 재시도 버튼이 일부만 존재
- 네트워크 오류와 서버 오류 구분 없음

**개선 방안:**
- 전역 에러 토스트/스낵바
- 재시도 로직 표준화
- 오프라인 상태 감지 및 표시

### 3. 접근성 (a11y)
**문제점:**
- 일부 버튼에 `aria-label` 없음
- 키보드 네비게이션 검증 부재
- 포커스 관리 미흡

**개선 방안:**
- 접근성 감사 도구 실행 (axe, Lighthouse)
- 키보드 네비게이션 테스트
- 스크린 리더 테스트

### 4. 반응형 디자인
**문제점:**
- 모바일 중심 설계이나 태블릿/데스크톱 대응 불명확

**개선 방안:**
- 반응형 브레이크포인트 명확화
- 다양한 화면 크기 테스트

---

## 보안 및 안정성

### 1. XSS 방지
**문제점:**
- 사용자 입력 검증 로직 부재
- HTML 이스케이프 처리 불명확

**개선 방안:**
- DOMPurify 같은 라이브러리 검토
- 입력값 검증 및 sanitization

### 2. 토큰 관리
**문제점:**
- 토큰 갱신 실패 시 처리 로직은 있으나 사용자 피드백 부족
- 토큰 만료 시간 관리 불명확

**개선 방안:**
- 토큰 만료 전 자동 갱신
- 만료 시 명확한 사용자 안내

### 3. 환경 변수 보안
**문제점:**
- 민감한 정보가 코드에 하드코딩될 가능성

**개선 방안:**
- 환경 변수 검증
- `.env.example` 파일 제공

### 4. 에러 정보 노출
**문제점:**
- 개발용 에러 메시지가 프로덕션에 노출될 가능성

**개선 방안:**
- 환경별 에러 메시지 분리
- 민감한 정보 필터링

---

## 개발자 경험 개선

### 1. 문서화
**문제점:**
- API 문서 부재
- 컴포넌트 사용 예시 없음
- 주석이 거의 없음

**개선 방안:**
- JSDoc 주석 추가
- Storybook 도입 검토
- API 문서 자동 생성 (OpenAPI/Swagger)

### 2. 개발 도구
**문제점:**
- Prettier 설정 없음
- Git hooks (pre-commit) 없음
- 린트 자동 수정 스크립트 없음

**개선 방안:**
```json
// package.json
{
  "scripts": {
    "lint:fix": "eslint . --fix",
    "format": "prettier --write \"src/**/*.{ts,tsx,css}\""
  }
}
```

### 3. 타입 정의
**문제점:**
- 타입 정의가 서비스 파일에 분산
- 공통 타입 재사용 부족

**개선 방안:**
```
src/
  types/
    api/
      auth.ts
      feed.ts
      notification.ts
    domain/
      user.ts
      content.ts
```

### 4. 디버깅 도구
**문제점:**
- 개발 환경에서의 디버깅 도구 부족

**개선 방안:**
- React DevTools 활용 가이드
- 네트워크 요청 로깅 개선
- 에러 추적 도구 (Sentry 등) 검토

---

## 우선순위별 개선 계획

### 🔴 높은 우선순위 (즉시)
1. ESLint 오류 수정
2. 코드 중복 제거 (날짜 포맷팅 함수)
3. 에러 처리 일관성 개선
4. 사용되지 않는 변수 제거

### 🟡 중간 우선순위 (단기)
1. React 최적화 (memo, useMemo, useCallback)
2. 테스트 환경 구축 및 핵심 기능 테스트
3. API 클라이언트 구조 개선
4. 타입 안정성 강화

### 🟢 낮은 우선순위 (중장기)
1. 상태 관리 라이브러리 도입
2. 코드 스플리팅
3. 이미지 최적화
4. Storybook 도입
5. E2E 테스트 구축

---

## 참고사항

- 현재 프로젝트는 React 19, TypeScript, Vite를 사용하는 모던 스택
- 기능 중심의 폴더 구조는 잘 유지되고 있음
- 기본적인 에러 처리와 로딩 상태 관리는 구현되어 있음
- 개선점은 대부분 코드 품질, 성능, 테스트 영역에 집중됨
