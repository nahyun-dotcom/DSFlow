#!/bin/bash

# DSFlow 전체 시스템 실행 스크립트

set -e

echo "🚀 DSFlow 시스템을 시작합니다..."

# Docker 설치 확인
if ! command -v docker &> /dev/null; then
    echo "❌ Docker가 설치되어 있지 않습니다."
    echo "📥 다음 방법으로 Docker를 설치하세요:"
    echo "   - macOS: brew install --cask docker"
    echo "   - 또는 https://www.docker.com/products/docker-desktop/ 에서 다운로드"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose가 설치되어 있지 않습니다."
    echo "📥 Docker Desktop을 설치하면 Docker Compose도 함께 설치됩니다."
    exit 1
fi

# Docker 서비스 실행 확인
if ! docker info &> /dev/null; then
    echo "❌ Docker 서비스가 실행되고 있지 않습니다."
    echo "🔧 Docker Desktop을 실행하고 다시 시도하세요."
    exit 1
fi

echo "✅ Docker 환경이 준비되었습니다."

# 프로젝트 루트 디렉토리로 이동
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "📁 작업 디렉토리: $(pwd)"

# 환경 변수 파일 확인
if [ ! -f "docker/.env" ]; then
    echo "⚠️  .env 파일이 없습니다. env.example에서 복사합니다..."
    if [ -f "docker/env.example" ]; then
        cp docker/env.example docker/.env
        echo "✅ .env 파일이 생성되었습니다."
    else
        echo "❌ env.example 파일을 찾을 수 없습니다."
        exit 1
    fi
else
    echo "✅ .env 파일이 존재합니다."
fi

# 이전 컨테이너 정리 (선택사항)
echo "🧹 이전 컨테이너를 정리합니다..."
docker-compose -f docker/docker-compose.yml down > /dev/null 2>&1 || true

# 시스템 빌드 및 실행
echo "🔨 시스템을 빌드하고 실행합니다..."
echo "   이 과정은 몇 분이 걸릴 수 있습니다..."

if docker-compose -f docker/docker-compose.yml up --build -d; then
    echo ""
    echo "🎉 DSFlow 시스템이 성공적으로 시작되었습니다!"
    echo ""
    echo "📊 서비스 상태:"
    docker-compose -f docker/docker-compose.yml ps
    echo ""
    echo "🌐 접속 정보:"
    echo "   - 프론트엔드:  http://localhost:3000"
    echo "   - 백엔드 API:  http://localhost:8080"
    echo "   - Swagger UI:  http://localhost:8080/swagger-ui.html"
    echo "   - 데이터베이스: localhost:5432 (dsflow/password)"
    echo ""
    echo "📝 로그 확인: docker-compose -f docker/docker-compose.yml logs -f"
    echo "🛑 시스템 종료: docker-compose -f docker/docker-compose.yml down"
    echo ""
    echo "✨ 즐거운 개발하세요!"
else
    echo ""
    echo "❌ 시스템 실행에 실패했습니다."
    echo "📋 문제 해결을 위해 로그를 확인하세요:"
    echo "   docker-compose -f docker/docker-compose.yml logs"
    exit 1
fi 