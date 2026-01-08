import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// 1. 부하 테스트 설정 (Load Configuration) - 동일
export const options = {
  stages: [
    { duration: '30s', target: 10 },  // Warming up
    { duration: '1m', target: 50 },   // Stress Test
    { duration: '30s', target: 0 },   // Cool down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% 요청이 0.5초 이내
    http_req_failed: ['rate<0.01'],   // 에러율 1% 미만
  },
};

export default function () {
  // 2. 토큰 확인
  const token = __ENV.MY_TOKEN;
  if (!token) {
    throw new Error('MY_TOKEN 환경변수가 설정되지 않았습니다.');
  }

  // 3. [수정] 검색 커서(lastPublishedAt) 랜덤화
  // Elasticsearch 페이징은 '시간(Timestamp)'을 기준으로 합니다.
  // 최근 1년(365일) 내의 랜덤한 과거 시점을 생성하여 커서로 사용합니다.
  
  const ONE_YEAR_MS = 365 * 24 * 60 * 60 * 1000; // 1년(밀리초)
  const now = Date.now();
  
  // 0 ~ 1년 사이의 랜덤 시간을 뺌 -> 과거의 랜덤한 시점이 됨
  const randomTimeOffset = randomIntBetween(0, ONE_YEAR_MS);
  const lastPublishedAt = now - randomTimeOffset;
  
  const size = 10;

  // [수정] 쿼리 파라미터 변경: lastId -> lastPublishedAt
  const url = `http://localhost:8000/api/feed?lastId=${lastPublishedAt}&size=${size}`;

  const params = {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    tags: { name: 'GetFeedApi_ES' },
  };

  // 4. API 요청
  const res = http.get(url, params);

  // [디버깅] 요청 실패 시(200 아님) 응답 본문 출력
  if (res.status !== 200) {
    console.error(`Error: Status ${res.status}, Body: ${res.body}`);
  }

  // 5. 검증
  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  // 6. Think Time (0.1 ~ 0.6초)
  sleep(Math.random() * 0.5 + 0.1);
}
