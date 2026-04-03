# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

feed-service는 key-feed 마이크로서비스 생태계의 핵심 서비스로, 사용자가 구독한 소스들의 콘텐츠를 집계하여 개인화된 피드를 제공합니다.

**핵심 기능:**
- 사용자별 개인화 피드 생성 (구독 소스 기반)
- MySQL 기반 커서 페이지네이션 (contentId 기반)
- 북마크 정보 통합
- 내부 API를 통한 콘텐츠 조회

## 빌드 및 실행 명령어

### 빌드
```bash
jwt_key=key_feed_jwt_key;jasypt_key=key_feed_jasypt_key ./gradlew clean build  # 전체 빌드 (테스트 포함)
```

### 테스트
```bash
jwt_key=key_feed_jwt_key;jasypt_key=key_feed_jasypt_key ./gradlew test                           # 전체 테스트 실행
jwt_key=key_feed_jwt_key;jasypt_key=key_feed_jasypt_key ./gradlew test --tests FeedServiceImplTest   # 특정 테스트 클래스 실행
jwt_key=key_feed_jwt_key;jasypt_key=key_feed_jasypt_key ./gradlew test --tests "*FeedController*"    # 패턴 매칭 테스트 실행
jwt_key=key_feed_jwt_key;jasypt_key=key_feed_jasypt_key ./gradlew jacocoTestReport                   # 테스트 커버리지 리포트 생성
```

**커버리지 리포트 위치:** `build/reports/jacoco/test/html/index.html`

### 로컬 실행
```bash
jwt_key=key_feed_jwt_key;jasypt_key=key_feed_jasypt_key ./gradlew bootRun                    # 기본 프로파일로 실행
jwt_key=key_feed_jwt_key;jasypt_key=key_feed_jasypt_key ./gradlew bootRun --args='--spring.profiles.active=local'  # local 프로파일
```

**필수 환경 변수:**
- `jwt_key`: JWT 토큰 서명 키
- `jasypt_key`: Jasypt 암호화 키 (설정 파일 복호화용)

**로컬 실행 전 필수 인프라:**
- MySQL (localhost:3306, database: key_feed_feed)
- identity-service (Feign 클라이언트 연동용)

## 아키텍처 구조

### 레이어 구조
```
Controller (REST API)
    ↓
Service (비즈니스 로직)
    ↓
Repository (데이터 접근)
    ↓
Data Store (MySQL)
```

### 패키지 구조

```
domain/
└── feed/                           # Feed 도메인 (비즈니스 로직)
    ├── controller/                # REST 엔드포인트
    │   ├── FeedController         # 사용자 API (/api/feed)
    │   └── FeedInternalController # 내부 API (/internal/feeds)
    ├── entity/Content             # JPA 엔티티 (MySQL)
    ├── service/FeedService        # 비즈니스 로직
    └── repository/
        └── ContentRepository      # JPA (MySQL)
infra/
└── client/                        # 외부 서비스 연동 (인프라 계층)
    └── UserInternalApiClient      # identity-service Feign 클라이언트
```

### 핵심 엔티티

**Content (MySQL - 진실의 원천)**
- `id`: 콘텐츠 고유 ID (커서 페이지네이션 기준)
- `sourceId`: 소스 ID (identity-service 참조)
- `title`, `summary`: 콘텐츠 메타데이터
- `publishedAt`: 발행 시각

## 외부 서비스 연동

### identity-service 연동 (Feign Client)

**1. 사용자 구독 소스 조회**
```
GET /internal/sources/user/{userId}
→ List<SourceResponseDto>
용도: 피드 생성 시 필터링할 소스 ID 추출
에러 처리: 실패 시 InternalApiRequestException (503)
```

**2. 북마크 상태 조회**
```
POST /internal/bookmarks/user/{userId}/check
Body: List<String> contentIds
→ Map<String, Long> (contentId → bookmarkId)
용도: 피드 아이템에 북마크 정보 부여
에러 처리: Graceful degradation (실패해도 피드 반환, bookmarkId=null)
```

**3. 사용자 키워드 조회**
```
GET /internal/users/{userId}/keywords
→ List<KeywordResponseDto>
현재 상태: 조회는 하지만 피드 로직에 미사용 (향후 키워드 기반 검색 예정)
```

**Feign 설정:**
- Connect Timeout: 5초
- Read Timeout: 5초
- Logger Level: BASIC

## 인증 및 보안

### 게이트웨이 헤더 기반 인증
- **인증 방식:** API Gateway에서 설정한 HTTP 헤더 검증
- **필수 헤더:**
  - `X-User-Id`: 사용자 ID (Long 타입)
  - `X-User-Roles`: 사용자 권한 (콤마 구분)

**인증 필터:** `GatewayHeaderAuthenticationFilter`가 헤더를 파싱하여 SecurityContext에 저장

### 엔드포인트 권한

```
/api/feed/**          → authenticated (X-User-Id 필수)
/internal/**          → permitAll (서비스 간 통신)
/actuator/**          → permitAll (헬스체크)
```

**세션 정책:** STATELESS (마이크로서비스 패턴)

### Controller에서 사용자 ID 추출
```java
@GetMapping
public ResponseEntity<?> getMyFeeds(
    @AuthenticationPrincipal Long userId,  // SecurityContext에서 자동 주입
    @RequestParam(required = false) Long lastId,
    @RequestParam(defaultValue = "10") int size
) { ... }
```

## 데이터 흐름 및 페이지네이션

### 피드 조회 플로우

```
1. Client → GET /api/feed?lastId={contentId}&size=10
   Headers: X-User-Id: 42

2. GatewayHeaderAuthenticationFilter
   → userId 추출 및 SecurityContext 설정

3. FeedService.fetchUserSourceMapping(42)
   → identity-service 호출
   → 사용자 구독 소스 ID + userDefinedName 매핑 반환 {100: "블로그", 200: "뉴스"}

4. FeedService.getPersonalizedFeeds(42, {100:..., 200:...}, lastId, 10)
   → MySQL 쿼리:
     - Filter: source_id IN [100, 200]
     - Cursor: content_id < lastId (커서, 첫 페이지는 생략)
     - Sort: content_id DESC
     - Size: 11 (hasNext 판단용 +1)

5. 북마크 정보 조회
   → identity-service POST /internal/bookmarks/user/42/check
   → contentId → bookmarkId 매핑

6. Response 구성
   {
     "content": [...],
     "hasNext": true,
     "nextCursorId": 1050  // 마지막 아이템의 content_id
   }
```

### 커서 기반 페이지네이션

**커서 기준:** `content_id` (MySQL auto-increment)
- 정렬 기준이 삽입 순서(크롤링 순서)와 동일
- `publishedAt` 기준 정렬이 아님에 유의

**첫 페이지:**
- `lastId` 파라미터 없음
- 가장 최근에 삽입된 콘텐츠부터 size+1개 조회

**다음 페이지:**
- `lastId={이전 응답의 nextCursorId}` 사용
- `content_id < lastId` 조건으로 필터링

**hasNext 판단:**
- size+1개 조회하여 실제 size개만 반환
- 조회 결과 > size이면 hasNext=true

## 설정 및 환경

### 프로파일 전략

```yaml
application.yml                 # 공통 설정 (port, feign timeout)
application-local.yml          # 로컬 개발 (MySQL localhost)
application-prod.yml           # 운영 환경 (암호화된 DB 정보)
```

**활성 프로파일:** `spring.profiles.active=prod` (기본값)

### JPA 및 데이터베이스

**DDL 전략:** `validate` (스키마 자동 변경 안 함)
- 운영 환경에서 안전성 보장
- 스키마 변경은 마이그레이션 스크립트로 관리 필요

**Auditing 설정:**
- `@CreatedDate` → `createdAt` (생성 시각, 업데이트 불가)
- `@LastModifiedDate` → `updatedAt` (수정 시각)
- `BaseTimeEntity` 상속으로 자동 관리

**SQL 로깅:**
- P6Spy: 실제 쿼리 파라미터 포함 로깅
- 커스텀 포맷터로 가독성 향상

## 테스트 전략

### 테스트 구조
```
src/test/java/
├── FeedServiceApplicationTests     # 통합 테스트
├── auth/                           # 테스트 유틸 (커스텀 애노테이션)
├── common/filter/                  # 보안 필터 테스트
└── domain/feed/
    ├── controller/                 # MockMvc 기반
    ├── service/impl/               # 핵심 비즈니스 로직
    └── repository/                 # 데이터 레이어
```

### 핵심 테스트: FeedServiceImplTest

**fetchUserSourceMapping() 테스트:**
- ✓ 정상 소스 조회 및 ID 추출
- ✓ 빈 소스 목록 처리
- ✓ FeignException → InternalApiRequestException
- ✓ 일반 예외 → InternalServerProcessingException
- ✓ null/empty/whitespace userDefinedName 필터링

**getPersonalizedFeeds() 테스트:**
- ✓ 빈 sourceMapping → 빈 응답 (Repository 호출 안 함)
- ✓ 첫 페이지 로드 (lastId=null, hasNext=true)
- ✓ 커서 기반 다음 페이지 (hasNext=false)
- ✓ 북마크 정보 통합
- ✓ 북마크 API 실패 시 Graceful degradation
- ✓ userDefinedName이 sourceName에 정상 매핑

**테스트 실행:**
```bash
# FeedServiceImplTest만 실행
jwt_key=key_feed_jwt_key;jasypt_key=key_feed_jasypt_key ./gradlew test --tests "*FeedServiceTest*"

# 커버리지 포함
jwt_key=key_feed_jwt_key;jasypt_key=key_feed_jasypt_key ./gradlew test jacocoTestReport
```

### 커버리지 제외 대상
- `common/**` (유틸리티)
- `config/**` (설정)
- `dto/**` (데이터 전송 객체)
- `entity/**` (도메인 모델)
- `constant/**` (상수)

→ 비즈니스 로직에 집중

## 로깅 및 모니터링

### AOP 기반 컨트롤러 로깅

**대상:** 모든 `@RestController` 메서드
**로그 내용:**
- 메서드 진입 (파라미터 포함)
- 메서드 종료 (반환값, 최대 300자)
- 예외 발생 (스택트레이스)
- 실행 시간 (밀리초)

**설정:** `LoggingAspectConfig.java`

### 헬스체크

**Actuator 엔드포인트:** `/actuator/health`
- Kubernetes liveness/readiness probe용

## 에러 처리

### 예외 계층 구조
```
GlobalExceptionHandler
├── InternalApiRequestException (503)
│   → identity-service 연동 실패
├── InternalServerProcessingException (500)
│   → 서버 내부 처리 오류
├── EntityNotFoundException (404)
│   → 엔티티 미존재
├── EntityAlreadyExistsException (409)
│   → 중복 엔티티
└── MethodArgumentNotValidException (400)
    → 요청 검증 실패
```

### Graceful Degradation

**북마크 API 실패:**
```java
try {
    Map<String, Long> bookmarks = client.getBookmarkedContentIds(...);
} catch (Exception e) {
    log.error("북마크 조회 실패", e);
    // 계속 진행, bookmarkId는 null로 반환
}
```

피드 제공이 핵심이므로 부가 기능 실패 시에도 서비스 유지

## 주요 개발 패턴

### 1. 내부 API 호출 패턴
```java
// Feign Client 인터페이스
@FeignClient(name = "identity-service")
public interface UserInternalApiClient {
    @GetMapping("/internal/sources/user/{userId}")
    List<SourceResponseDto> getUserSources(@PathVariable Long userId);
}
```

### 2. 응답 래핑
모든 API 응답은 `HttpResponse<T>`로 래핑:
```java
{
  "success": true,
  "message": "피드 조회 성공",
  "data": { ... }
}
```

### 3. contentId 기반 커서 페이지네이션 구현
```java
// ContentRepository
List<Content> findBySourceIdIn(List<Long> sourceIds, Pageable pageable);
List<Content> findBySourceIdInAndIdBefore(List<Long> sourceIds, Long lastId, Pageable pageable);

// Pageable: PageRequest.of(0, size + 1, Sort.by(DESC, "id"))
```

## Docker 빌드 및 배포

**Dockerfile 위치:** 루트 디렉토리

```bash
# 이미지 빌드
docker build -t feed-service:latest .

# 컨테이너 실행
docker run -p 8082:8082 \
  -e jasypt_key=YOUR_KEY \
  -e SPRING_PROFILES_ACTIVE=prod \
  feed-service:latest
```

**포트:** 8082

## 코드 수정 시 주의사항

### 1. 페이지네이션 로직 수정 시
- `size+1` 조회 패턴 유지 (hasNext 판단용)
- 커서는 `content_id` 기준 (`publishedAt` 아님)
- 정렬 기준이 크롤링 삽입 순서임을 인지

### 2. Feign Client 수정 시
- Timeout 설정 (5초) 적절성 확인
- 에러 처리: 필수 API vs Graceful degradation 구분

### 3. 보안 필터 수정 시
- `GatewayHeaderAuthenticationFilter` 순서 중요
- 헤더 이름 변경 시 Gateway와 동기화 필요
- `/internal/**` 경로는 인증 불필요 유지

### 4. 테스트 작성 시
- Service 레이어 테스트는 Mockito로 의존성 격리
- Controller 테스트는 `@AutoConfigureMockMvc(addFilters=false)`
- `@Nested`와 `@DisplayName`으로 구조화
- AssertJ로 가독성 높은 검증 작성
- 테스트용 `Content` 객체는 `Content.builder()`로 생성 (id 직접 지정 가능)

## 트러블슈팅

### identity-service 연동 실패 (503)
```
원인: identity-service 미실행 또는 네트워크 불가
확인: Feign URL 설정 (application.yml)
해결: 로컬 테스트 시 WireMock 사용 (이미 의존성 포함)
```

### Jasypt 복호화 실패
```
원인: jasypt_key 또는 jwt_key 환경 변수 미설정
해결: gradlew 명령어 실행 시 jwt_key=key_feed_jwt_key;jasypt_key=key_feed_jasypt_key 추가
      또는 export jwt_key=key_feed_jwt_key; export jasypt_key=key_feed_jasypt_key
```

### SpringBoot 통합 테스트 컨텍스트 로드 실패
```
원인: spring-boot-starter-data-elasticsearch 의존성이 남아있어 ES 자동 설정 시도
해결: src/test/resources/application.yml에 ES 자동 설정 제외 항목 유지
      (ElasticsearchClientAutoConfiguration 등 3개 exclude 설정)
```
