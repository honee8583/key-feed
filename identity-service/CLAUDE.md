# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소에서 작업할 때 참고하는 가이드입니다.

## 빌드 및 테스트 명령어

```bash
# 빌드 (테스트 생략)
./gradlew clean build -x test

# 전체 테스트 실행
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "com.leedahun.identityservice.domain.auth.service.LoginServiceImplTest"

# 단일 테스트 메서드 실행
./gradlew test --tests "com.leedahun.identityservice.domain.auth.service.LoginServiceImplTest.login_Success"

# 로컬 애플리케이션 실행 (jwt_key, jasypt_key 환경변수 필요)
./gradlew bootRun --args='--spring.profiles.active=local'

# JaCoCo 커버리지 리포트 생성
./gradlew jacocoTestReport
# 리포트 경로: build/reports/jacoco/test/html/index.html
```

## 필수 환경변수

- `jwt_key`: JWT 서명용 HMAC512 시크릿
- `jasypt_key`: application.yml의 `ENC()` 값 복호화 키

## 아키텍처 개요

**Spring Boot 3.5 마이크로서비스** (Java 17)로, key-feed 시스템의 사용자 인증 및 사용자 설정을 담당합니다. 포트 **8081**에서 실행되며 **Eureka**에 등록됩니다.

### 도메인 구조 (DDD)

```
src/main/java/com/leedahun/identityservice/
├── config/           # 전역 설정 (Jasypt, JPA 감사, 로깅 AOP, P6spy)
├── common/           # 공통: BaseTimeEntity, 예외, 응답 래퍼, 유틸리티
├── domain/
│   ├── auth/         # 인증: User 엔티티, JWT, 로그인/회원가입, 이메일 인증
│   ├── bookmark/     # 사용자 북마크 (폴더 구조)
│   ├── keyword/      # 사용자 키워드 (알림 토글)
│   └── source/       # RSS/피드 소스 관리 (Source, UserSource)
└── infra/client/     # OpenFeign 클라이언트 (FeedInternalApiClient → feed-service)
```

### 주요 패턴

- **Gateway-First 인증**: 보안 필터가 API Gateway의 `X-User-Id`, `X-User-Roles` 헤더를 읽음
- **Internal API 패턴**: `/internal/**` 엔드포인트는 서비스 간 호출용으로 인증 우회
- **커서 기반 페이지네이션**: 효율적인 목록 조회를 위해 `CursorPage` 사용
- **속성 암호화**: 민감한 값은 Jasypt의 `ENC()`로 암호화
- **Base Entity**: 모든 엔티티는 `BaseTimeEntity`를 상속하여 createdAt/updatedAt 자동 관리

### 보안 흐름

1. 공개 엔드포인트: `/api/auth/**` (회원가입, 로그인, 이메일 인증)
2. 보호 엔드포인트: `/api/keywords/**`, `/api/sources/**`, `/api/bookmarks/**`
3. 내부 엔드포인트: `/internal/**`, `/actuator/**` (인증 없음)
4. JWT: Access 토큰 (10분), Refresh 토큰 (14일, HTTP-only 쿠키)

### 서비스 간 통신

- **FeedInternalApiClient**: `POST /internal/feeds/contents`로 `feed-service` 호출
- Eureka를 통한 서비스 디스커버리, Spring Cloud LoadBalancer로 로드밸런싱

### 엔티티 관계

```
User (1:N) Keyword
User (1:N) UserSource → Source
User (1:N) BookmarkFolder (1:N) Bookmark
```

### 애플리케이션 제한

- 사용자당 최대 키워드: 20개
- 사용자당 최대 북마크 폴더: 7개
- 이메일 인증: 최대 5회 시도, 15분 잠금, 5분 코드 만료

## Spring 프로필

- `local`: MySQL localhost, 개발용 JWT 만료시간 연장
- `prod`: 외부 DB, 표준 JWT 만료시간 (기본 활성화)

## 테스트

- 서비스 단위 테스트: Mockito 사용
- 컨트롤러 통합 테스트: `@SpringBootTest` 사용
- 보안 테스트: `@WithAnonymousUser` 커스텀 어노테이션
- 외부 API 모킹: WireMock 사용
- JaCoCo 제외 대상: `**/common/**`, `**/config/**`, `**/dto/**`, `**/entity/**`, `**/constant/**`

## 코딩 컨벤션

### 중복 코드 제거
- 중복되는 코드는 별도 메서드로 추출하여 재사용
- 예: `resolveUser(userId)` 같은 공통 조회 로직은 private 메서드로 분리

### 단일 책임 원칙 (SRP)
- 클래스와 메서드는 하나의 책임만 가지도록 설계
- 서비스 계층: 비즈니스 로직만 담당
- 컨트롤러 계층: 요청/응답 처리만 담당
- 리포지토리 계층: 데이터 접근만 담당
