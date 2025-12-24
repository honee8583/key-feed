import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// 1. 테스트 설정 (Options)
// 가장 일반적인 'Load Test' 패턴: 서서히 유입 -> 유지 -> 서서히 감소
export const options = {
  stages: [
    { duration: '30s', target: 50 }, // 30초 동안 가상 유저(VU) 50명까지 증가 (Ramp-up)
    { duration: '1m',  target: 50 }, // 1분 동안 50명 유지 (Steady State)
    { duration: '10s', target: 0 },  // 10초 동안 0명으로 감소 (Ramp-down)
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],   // 에러율이 1% 미만이어야 함
    http_req_duration: ['p(95)<500'], // 95%의 요청이 500ms 이내에 완료되어야 함 (SLA 기준)
  },
};

// 2. 공통 변수 설정
const BASE_URL = 'http://localhost:8000';
// 실제 테스트 시 유효한 토큰을 여기에 넣거나, 환경변수로 주입받으세요.
const ACCESS_TOKEN = __ENV.TOKEN || 'YOUR_ACTUAL_JWT_TOKEN_HERE'; 

export default function () {
  // 3. 동적 데이터 생성 (캐시 방지 및 실제 사용 패턴 모방)
  
  // folderId: 1 ~ 7 사이 랜덤 선택
  const folderId = randomIntBetween(1, 7);

  // lastId: 100만 건 데이터 중 랜덤한 위치 조회 
  // (최신순 조회를 가정하여 50만 ~ 100만 사이의 ID를 랜덤하게 커서로 사용)
  // 실제 DB에 존재하는 ID 범위 내에서 던지는 것이 좋습니다.
  const lastId = randomIntBetween(500000, 999999); 
  
  const size = 20;

  // 4. API 요청 구성
  const params = {
    headers: {
      'Authorization': `Bearer ${ACCESS_TOKEN}`,
      'Content-Type': 'application/json',
    },
    tags: { name: 'GetBookmarks' }, // 결과 리포트에서 태그로 구분하기 위함
  };

  const url = `${BASE_URL}/api/bookmarks?folderId=${folderId}&lastId=${lastId}&size=${size}`;

  // 5. 요청 전송
  const res = http.get(url, params);

  // 6. 결과 검증 (Check)
  check(res, {
    'status is 200': (r) => r.status === 200,
    'content type is json': (r) => r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  // 7. 씽크 타임 (Think Time)
  // 실제 사용자는 연속으로 요청을 보내지 않고 페이지를 읽는 시간이 있음 (0.5~1초 랜덤 대기)
  sleep(Math.random() * 0.5 + 0.5);
}
