# Antigravity 지침 (React + Vite 프로젝트)

## 🚫 Figma MCP 연결 확인
- Figma URL을 전달받았을 때 **반드시 MCP 서버 연결 상태를 먼저 확인**
- **MCP 서버가 연결되지 않았다면**: "피그마 MCP 서버에 연결되어 있지 않습니다. 먼저 MCP 설정을 확인하고 연결해주세요."라고 응답하고 작업 중단
- 연결 확인 방법: MCP Servers 목록에서 Figma 항목이 `connected` 또는 `running` 상태인지 체크

## 🎨 Figma 디자인 처리 규칙
- Figma MCP 서버의 URL(예: `https://mcp.figma.com/...`)을 **절대 아이콘/이미지 소스로 사용하지 말기**
- 디자인에서 아이콘 추출 시:
  - 실제 아이콘 SVG 파일을 프로젝트 `public/icons/` 또는 `src/assets/icons/`에 저장
  - import 경로: `import IconName from '@/assets/icons/icon-name.svg'`
  - CSS: `background-image: url('/icons/icon-name.svg')` 또는 `<img src="/icons/icon-name.svg" />`

## 🔄 빌드 및 검증 워크플로우
- **모든 작업 완료 후 반드시 빌드 수행**
  1. `npm run build` 실행
  2. 빌드 에러 발생 시: 에러 로그 표시 → 수정 → 재빌드
  3. 빌드 성공 후: `npm run preview`로 로컬 확인
  4. 성공 메시지: "빌드 성공! preview 서버: http://localhost:5173 확인하세요."
