# Identity Service

**Key Feed** 시스템의 사용자 인증 및 사용자 설정을 담당하는 마이크로서비스입니다.

## 기술 스택

- **Java 17**
- **Spring Boot 3.5**
- **Spring Security** - JWT 기반 인증
- **Spring Data JPA** - 데이터 액세스
- **Spring Cloud Netflix Eureka** - 서비스 디스커버리
- **Spring Cloud OpenFeign** - 서비스 간 통신
- **MySQL** - 데이터베이스
- **Jasypt** - 속성 암호화
- **Thymeleaf** - 이메일 템플릿

## 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway                            │
│              (JWT 검증 → X-User-Id 헤더 추가)                  │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Identity Service                         │
│                       (Port: 8081)                          │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────────────┐ │
│  │  Auth   │  │ Keyword │  │ Source  │  │    Bookmark     │ │
│  │ Domain  │  │ Domain  │  │ Domain  │  │     Domain      │ │
│  └─────────┘  └─────────┘  └─────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 도메인 구조

| 도메인 | 설명 |
|--------|------|
| **Auth** | 회원가입, 로그인, JWT 토큰 관리, 이메일 인증, 비밀번호 관리 |
| **Keyword** | 사용자 키워드 관리, 알림 토글 |
| **Source** | RSS/피드 소스 구독 관리 |
| **Bookmark** | 북마크 및 폴더 관리 |

## 빌드 및 실행

### 필수 환경변수

| 변수명 | 설명 |
|--------|------|
| `jwt_key` | JWT 서명용 HMAC512 시크릿 |
| `jasypt_key` | application.yml의 `ENC()` 값 복호화 키 |

### 빌드

```bash
# 빌드 (테스트 생략)
./gradlew clean build -x test

# 전체 테스트 실행
./gradlew test
```

### 실행

```bash
# 로컬 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

## 프로필

| 프로필 | 설명 |
|--------|------|
| `local` | MySQL localhost, 개발용 JWT 만료시간 연장 |
| `prod` | 외부 DB, 표준 JWT 만료시간 (기본) |

## 인증 흐름

### JWT 토큰
- **Access Token**: 10분 만료
- **Refresh Token**: 14일 만료 (HTTP-only 쿠키)

### 엔드포인트 접근 권한
- **공개**: `/api/auth/**` (회원가입, 로그인, 이메일 인증, 비밀번호 찾기)
- **인증 필요**: `/api/users/**`, `/api/keywords/**`, `/api/sources/**`, `/api/bookmarks/**`
- **내부 전용**: `/internal/**`, `/actuator/**`

---

# API 명세

## 공통 응답 형식

```json
{
  "status": "200",
  "message": "성공 메시지",
  "data": { ... }
}
```

---

## 1. 인증 API (`/api/auth`)

### 1.1 회원가입

**POST** `/api/auth/join`

```json
// Request
{
  "email": "user@example.com",
  "name": "홍길동",
  "password": "password123"
}

// Response (201 Created)
{
  "status": "201",
  "message": "저장에 성공하였습니다.",
  "data": null
}
```

### 1.2 로그인

**POST** `/api/auth/login`

```json
// Request
{
  "email": "user@example.com",
  "password": "password123"
}

// Response (200 OK)
// Set-Cookie: refreshToken=xxx; HttpOnly; SameSite=None; Path=/
{
  "status": "200",
  "message": "로그인에 성공하였습니다.",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "role": "USER",
    "accessToken": "eyJhbGciOiJIUzUxMi..."
  }
}
```

### 1.3 토큰 재발급

**POST** `/api/auth/refresh`

> Cookie에 `refreshToken` 필요

```json
// Response (200 OK)
// Set-Cookie: refreshToken=xxx; HttpOnly; SameSite=None; Path=/
{
  "status": "200",
  "message": "토큰발급에 성공했습니다.",
  "data": "eyJhbGciOiJIUzUxMi..."  // 새 accessToken
}
```

### 1.4 회원가입 이메일 인증 요청

**POST** `/api/auth/email-verification/request`

```json
// Request
{
  "email": "user@example.com"
}

// Response (201 Created)
{
  "status": "201",
  "message": "이메일 전송에 성공하였습니다.",
  "data": null
}
```

### 1.5 회원가입 이메일 인증 확인

**POST** `/api/auth/email-verification/confirm`

```json
// Request
{
  "email": "user@example.com",
  "code": "123456"
}

// Response (200 OK)
{
  "status": "200",
  "message": "이메일 인증이 완료되었습니다.",  // 또는 "인증번호가 일치하지 않습니다."
  "data": {
    "status": "VERIFIED",  // PENDING, VERIFIED, EXPIRED, LOCKED
    "attempts": 1,
    "retryAt": null,
    "expiresAt": "2026-02-05T16:15:00"
  }
}
```

### 1.6 비밀번호 찾기 - 이메일 요청

**POST** `/api/auth/password-reset/request`

```json
// Request
{
  "email": "user@example.com"
}

// Response (201 Created)
{
  "status": "201",
  "message": "비밀번호 재설정을 위한 인증 이메일이 발송되었습니다.",
  "data": null
}
```

| 에러 상황 | HTTP Status | message |
|----------|-------------|---------|
| 등록되지 않은 이메일 | 404 | 데이터가 존재하지 않습니다. |
| 잠금 상태 | 423 | 일정 시간 동안 많은 시도로 인해 인증이 제한되었습니다. |

### 1.7 비밀번호 찾기 - 인증코드 검증

**POST** `/api/auth/password-reset/verify`

```json
// Request
{
  "email": "user@example.com",
  "code": "123456"
}

// Response (200 OK)
{
  "status": "200",
  "message": "이메일 인증이 완료되었습니다.",
  "data": {
    "status": "VERIFIED",
    "attempts": 1,
    "retryAt": null,
    "expiresAt": "2026-02-05T16:15:00"
  }
}
```

| 에러 상황 | HTTP Status | message |
|----------|-------------|---------|
| 코드 만료 | 400 | 인증코드의 유효기간이 지났습니다. |
| 시도 횟수 초과 | 429 | 지정된 인증 횟수가 초과되었습니다. |
| 잠금 상태 | 423 | 일정 시간 동안 많은 시도로 인해 인증이 제한되었습니다. |

### 1.8 비밀번호 찾기 - 비밀번호 재설정

**POST** `/api/auth/password-reset/confirm`

```json
// Request
{
  "email": "user@example.com",
  "newPassword": "newPassword123!",
  "confirmPassword": "newPassword123!"
}

// Response (200 OK)
{
  "status": "200",
  "message": "비밀번호가 성공적으로 재설정되었습니다.",
  "data": null
}
```

| 에러 상황 | HTTP Status | message |
|----------|-------------|---------|
| 비밀번호 불일치 | 400 | 새 비밀번호가 일치하지 않습니다. |
| 이메일 인증 미완료 | 400 | 이메일 인증이 완료되지 않았습니다. |
| 기존 비밀번호와 동일 | 400 | 현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다. |

---

## 2. 사용자 API (`/api/users`)

> 인증 필요 (X-User-Id 헤더)

### 2.1 비밀번호 변경

**PATCH** `/api/users/password`

```json
// Request
{
  "currentPassword": "oldPassword123",
  "newPassword": "newPassword123!",
  "confirmPassword": "newPassword123!"
}

// Response (200 OK)
{
  "status": "200",
  "message": "비밀번호가 성공적으로 변경되었습니다.",
  "data": null
}
```

### 2.2 회원 탈퇴

**DELETE** `/api/users`

```json
// Request
{
  "password": "password123"
}

// Response (200 OK)
{
  "status": "200",
  "message": "회원 탈퇴가 완료되었습니다.",
  "data": null
}
```

---

## 3. 키워드 API (`/api/keywords`)

> 인증 필요 (X-User-Id 헤더)

### 3.1 키워드 목록 조회

**GET** `/api/keywords`

```json
// Response (200 OK)
{
  "status": "200",
  "message": "조회에 성공하였습니다.",
  "data": [
    {
      "id": 1,
      "name": "AI",
      "notificationEnabled": true
    }
  ]
}
```

### 3.2 키워드 추가

**POST** `/api/keywords`

```json
// Request
{
  "name": "AI"
}

// Response (201 Created)
{
  "status": "201",
  "message": "저장에 성공하였습니다.",
  "data": {
    "id": 1,
    "name": "AI",
    "notificationEnabled": true
  }
}
```

### 3.3 키워드 알림 토글

**PATCH** `/api/keywords/{keywordId}/toggle`

```json
// Response (200 OK)
{
  "status": "200",
  "message": "수정에 성공하였습니다.",
  "data": {
    "id": 1,
    "name": "AI",
    "notificationEnabled": false
  }
}
```

### 3.4 키워드 삭제

**DELETE** `/api/keywords/{keywordId}`

```json
// Response (200 OK)
{
  "status": "200",
  "message": "삭제에 성공하였습니다.",
  "data": null
}
```

---

## 4. 소스 API (`/api/sources`)

> 인증 필요 (X-User-Id 헤더)

### 4.1 내 소스 목록 조회

**GET** `/api/sources/my`

```json
// Response (200 OK)
{
  "status": "200",
  "message": "조회에 성공하였습니다.",
  "data": [
    {
      "userSourceId": 1,
      "sourceId": 10,
      "name": "TechCrunch",
      "url": "https://techcrunch.com/feed",
      "receiveFeed": true
    }
  ]
}
```

### 4.2 내 소스 검색

**GET** `/api/sources/my/search?keyword={keyword}`

```json
// Response (200 OK)
{
  "status": "200",
  "message": "조회에 성공하였습니다.",
  "data": [ ... ]
}
```

### 4.3 소스 추가

**POST** `/api/sources`

```json
// Request
{
  "url": "https://techcrunch.com/feed",
  "name": "TechCrunch"
}

// Response (200 OK)
{
  "status": "200",
  "message": "저장에 성공하였습니다.",
  "data": {
    "userSourceId": 1,
    "sourceId": 10,
    "name": "TechCrunch",
    "url": "https://techcrunch.com/feed",
    "receiveFeed": true
  }
}
```

### 4.4 소스 구독 해제

**DELETE** `/api/sources/my/{userSourceId}`

```json
// Response (200 OK)
{
  "status": "200",
  "message": "삭제에 성공하였습니다.",
  "data": null
}
```

### 4.5 피드 수신 토글

**PATCH** `/api/sources/my/{userSourceId}/receive-feed`

```json
// Response (200 OK)
{
  "status": "200",
  "message": "수정에 성공하였습니다.",
  "data": {
    "userSourceId": 1,
    "sourceId": 10,
    "name": "TechCrunch",
    "url": "https://techcrunch.com/feed",
    "receiveFeed": false
  }
}
```

### 4.6 추천 소스 목록 조회

**GET** `/api/sources/recommended?page={page}&size={size}`

```json
// Response (200 OK)
{
  "status": "200",
  "message": "조회에 성공하였습니다.",
  "data": [
    {
      "sourceId": 10,
      "name": "TechCrunch",
      "url": "https://techcrunch.com/feed",
      "subscribed": false
    }
  ]
}
```

---

## 5. 북마크 API (`/api/bookmarks`)

> 인증 필요 (X-User-Id 헤더)

### 5.1 북마크 폴더 목록 조회

**GET** `/api/bookmarks/folders`

```json
// Response (200 OK)
{
  "status": "200",
  "message": "조회에 성공하였습니다.",
  "data": [
    {
      "id": 1,
      "name": "개발",
      "bookmarkCount": 5
    }
  ]
}
```

### 5.2 북마크 폴더 생성

**POST** `/api/bookmarks/folders`

```json
// Request
{
  "name": "개발"
}

// Response (201 Created)
{
  "status": "201",
  "message": "저장에 성공하였습니다.",
  "data": 1  // folderId
}
```

### 5.3 북마크 폴더 수정

**PATCH** `/api/bookmarks/folders/{folderId}`

```json
// Request
{
  "name": "개발 자료"
}

// Response (200 OK)
{
  "status": "200",
  "message": "수정에 성공하였습니다.",
  "data": null
}
```

### 5.4 북마크 폴더 삭제

**DELETE** `/api/bookmarks/folders/{folderId}`

```json
// Response (200 OK)
{
  "status": "200",
  "message": "삭제에 성공하였습니다.",
  "data": null
}
```

### 5.5 북마크 목록 조회

**GET** `/api/bookmarks?folderId={folderId}&lastId={lastId}&size={size}`

> 커서 기반 페이지네이션

```json
// Response (200 OK)
{
  "status": "200",
  "message": "조회에 성공하였습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "contentId": "content-123",
        "title": "기사 제목",
        "url": "https://example.com/article",
        "folderId": 1
      }
    ],
    "lastId": 1,
    "hasNext": true
  }
}
```

### 5.6 북마크 추가

**POST** `/api/bookmarks`

```json
// Request
{
  "contentId": "content-123",
  "title": "기사 제목",
  "url": "https://example.com/article",
  "folderId": 1  // optional
}

// Response (201 Created)
{
  "status": "201",
  "message": "저장에 성공하였습니다.",
  "data": 1  // bookmarkId
}
```

### 5.7 북마크 삭제

**DELETE** `/api/bookmarks/{bookmarkId}`

```json
// Response (200 OK)
{
  "status": "200",
  "message": "삭제에 성공하였습니다.",
  "data": null
}
```

### 5.8 북마크 폴더 이동

**PATCH** `/api/bookmarks/{bookmarkId}/folder`

```json
// Request
{
  "folderId": 2
}

// Response (200 OK)
{
  "status": "200",
  "message": "수정에 성공하였습니다.",
  "data": null
}
```

### 5.9 북마크를 폴더에서 제거

**DELETE** `/api/bookmarks/{bookmarkId}/folder`

```json
// Response (200 OK)
{
  "status": "200",
  "message": "수정에 성공하였습니다.",
  "data": null
}
```

---

## 6. 내부 API (`/internal`)

> 서비스 간 통신용 (인증 없음)

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/internal/users/{userId}/keywords` | 사용자 키워드 조회 |
| POST | `/internal/keywords/match-users?sourceId={sourceId}` | 키워드 매칭 사용자 조회 |
| GET | `/internal/sources/user/{userId}` | 사용자 구독 소스 조회 |
| POST | `/internal/bookmarks/user/{userId}/check` | 북마크 여부 확인 |

---

## 제한 사항

| 항목 | 제한 |
|------|------|
| 사용자당 최대 키워드 | 20개 |
| 사용자당 최대 북마크 폴더 | 7개 |
| 이메일 인증 최대 시도 | 5회 |
| 이메일 인증 잠금 시간 | 15분 |
| 인증 코드 유효 시간 | 5분 |
