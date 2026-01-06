#!/bin/bash

# 1. 에러 발생 시 즉시 중단 설정
set -e

# 2. 변수 설정
JWT_KEY=$1
JASYPT_KEY=$2
IMAGE_NAME=$3

echo "🚀 [1/3] 빌드 시작 (Gradle clean build)..."
# 환경 변수를 부여하며 빌드 실행
jwt_key=$JWT_KEY jasypt_key=$JASYPT_KEY ./gradlew clean -x build

echo "🐳 [2/3] 도커 이미지 빌드 (platform: linux/amd64)..."
docker build --platform linux/amd64 -t $IMAGE_NAME .

echo "📤 [3/3] 도커 허브 푸시..."
docker push $IMAGE_NAME

echo "✅ 배포 이미지가 성공적으로 업로드되었습니다!"
