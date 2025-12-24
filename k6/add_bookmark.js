import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';
import exec from 'k6/execution'; // [핵심] 실행 컨텍스트 정보를 가져오기 위함

// 1. 테스트 설정 (Insert는 DB 부하가 크므로 조회보다 보수적으로 잡는 게 일반적)
export const options = {
  stages: [
    { duration: '10s', target: 10 }, // 10초간 10명까지 서서히 증가 (Warm-up)
    { duration: '1m',  target: 30 }, // 1분간 30명 유지 (부하 지속)
    { duration: '10s', target: 0 },  // 10초간 종료 (Cool-down)
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],   // 실패율 1% 미만
    http_req_duration: ['p(95)<1000'], // 쓰기는 조회보다 느리므로 기준을 1초(1000ms)로 완화
  },
};

// 2. 공통 변수
const BASE_URL = 'http://localhost:8000';
const ACCESS_TOKEN = __ENV.TOKEN || 'YOUR_JWT_TOKEN_HERE';

// 테스트를 재실행할 때 ID 중복 방지를 위한 시작 오프셋
// 예: 첫 번째 테스트 후 5000건이 들어갔다면, 두 번째 실행 때는 5000으로 설정
const START_OFFSET = 0; 

export default function () {
  // 3. 데이터 생성 로직
  
  // [핵심 로직] 순차적 contentId 생성
  // exec.scenario.iterationInTest: 모든 VU를 통틀어 현재 몇 번째 실행인지(0부터 시작) 알려줌
  // 병렬 실행 환경에서도 절대 중복되지 않는 순차적인 숫자를 보장합니다.
  const uniqueNum = exec.scenario.iterationInTest + 1 + START_OFFSET;
  const contentId = String(uniqueNum); // String 타입 요구사항 반영

  // folderId 랜덤 (1~7)
  const folderId = randomIntBetween(1, 7);

  // 4. 요청 본문(Body) 구성
  const payload = JSON.stringify({
    contentId: contentId,
    folderId: folderId,
  });

  const params = {
    headers: {
      'Authorization': `Bearer ${ACCESS_TOKEN}`,
      'Content-Type': 'application/json',
    },
    tags: { name: 'InsertBookmark' },
  };

  // 5. POST 요청 전송
  const res = http.post(`${BASE_URL}/api/bookmarks`, payload, params);

  // 6. 검증 (Check)
  // 보통 생성 성공 시 200 OK 혹은 201 Created 가 리턴됩니다. 둘 다 허용하도록 작성했습니다.
  check(res, {
    'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'response time < 1s': (r) => r.timings.duration < 1000,
  });

  // 7. 실패 시 디버깅을 위한 로그 (옵션)
  // 409 Conflict(중복) 에러 등이 나면 로그를 찍어봅니다.
  if (res.status !== 200 && res.status !== 201) {
    console.log(`Failed! status: ${res.status}, body: ${res.body}, contentId: ${contentId}`);
  }

  // 8. 씽크 타임 (Insert는 연속 호출 시 DB 락 경합이 심하므로 약간의 텀을 줍니다)
  sleep(1);
}
