# 1. 빌드 단계 (Node.js)
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# 2. 실행 단계 (Nginx)
FROM nginx:alpine
# 빌드된 결과물(dist)을 Nginx가 서빙할 폴더로 복사
COPY --from=builder /app/dist /usr/share/nginx/html
# SPA 라우팅 처리를 위한 설정 파일 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]