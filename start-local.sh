#!/bin/bash

# DSFlow 로컬 개발환경 시작 스크립트

echo "🚀 DSFlow 로컬 개발환경을 시작합니다..."

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 함수 정의
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo -e "${RED}❌ $1이 설치되지 않았습니다.${NC}"
        echo -e "${YELLOW}💡 $2${NC}"
        exit 1
    else
        echo -e "${GREEN}✅ $1 확인됨${NC}"
    fi
}

# 필수 프로그램 확인
echo -e "${BLUE}📋 필수 프로그램 확인 중...${NC}"
check_command "java" "Java 17 이상을 설치해주세요: https://adoptium.net/"
check_command "node" "Node.js 18 이상을 설치해주세요: https://nodejs.org/"

# Java 버전 확인
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}❌ Java 17 이상이 필요합니다. 현재 버전: Java $JAVA_VERSION${NC}"
    exit 1
fi

# Node.js 버전 확인
NODE_VERSION=$(node -v | sed 's/v//' | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo -e "${RED}❌ Node.js 18 이상이 필요합니다. 현재 버전: Node $NODE_VERSION${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 모든 필수 프로그램이 확인되었습니다!${NC}"
echo

# 포트 확인
echo -e "${BLUE}🔍 포트 사용 확인 중...${NC}"
if lsof -i :8080 &> /dev/null; then
    echo -e "${YELLOW}⚠️  8080 포트가 사용 중입니다.${NC}"
    echo -e "${YELLOW}📝 사용 중인 프로세스를 종료하거나 다른 포트를 사용하세요.${NC}"
    lsof -i :8080
    echo
fi

if lsof -i :3000 &> /dev/null; then
    echo -e "${YELLOW}⚠️  3000 포트가 사용 중입니다.${NC}"
    echo -e "${YELLOW}📝 사용 중인 프로세스를 종료하세요.${NC}"
    lsof -i :3000
    echo
fi

# 선택 메뉴
echo -e "${BLUE}🎯 실행할 서비스를 선택하세요:${NC}"
echo "1) Backend만 실행 (Spring Boot)"
echo "2) Frontend만 실행 (React + Vite)" 
echo "3) 전체 실행 (Backend + Frontend)"
echo "4) 종료"
echo

read -p "선택 (1-4): " choice

case $choice in
    1)
        echo -e "${GREEN}🔧 Backend 실행 중...${NC}"
        cd backend
        echo -e "${YELLOW}📂 backend 디렉토리로 이동${NC}"
        echo -e "${YELLOW}🔨 Maven 의존성 확인 및 컴파일 중...${NC}"
        ./mvnw clean compile
        echo -e "${GREEN}🚀 Spring Boot 애플리케이션 시작!${NC}"
        echo -e "${BLUE}📍 API 서버: http://localhost:8080/api${NC}"
        echo -e "${BLUE}📍 H2 콘솔: http://localhost:8080/api/h2-console${NC}"
        echo -e "${BLUE}📍 Swagger: http://localhost:8080/api/swagger-ui.html${NC}"
        ./mvnw spring-boot:run
        ;;
    2)
        echo -e "${GREEN}🔧 Frontend 실행 중...${NC}"
        cd frontend
        echo -e "${YELLOW}📂 frontend 디렉토리로 이동${NC}"
        if [ ! -d "node_modules" ]; then
            echo -e "${YELLOW}📦 npm 의존성 설치 중...${NC}"
            npm install
        fi
        echo -e "${GREEN}🚀 React 개발 서버 시작!${NC}"
        echo -e "${BLUE}📍 웹 애플리케이션: http://localhost:3000${NC}"
        npm run dev
        ;;
    3)
        echo -e "${GREEN}🔧 전체 서비스 실행 중...${NC}"
        
        # Backend 백그라운드 실행
        echo -e "${YELLOW}🔨 Backend 컴파일 중...${NC}"
        cd backend
        ./mvnw clean compile
        echo -e "${GREEN}🚀 Backend 시작! (백그라운드)${NC}"
        ./mvnw spring-boot:run > ../backend.log 2>&1 &
        BACKEND_PID=$!
        cd ..
        
        # Backend 시작 대기
        echo -e "${YELLOW}⏳ Backend 시작 대기 중... (30초)${NC}"
        sleep 30
        
        # Frontend 실행
        echo -e "${YELLOW}📦 Frontend 의존성 확인 중...${NC}"
        cd frontend
        if [ ! -d "node_modules" ]; then
            npm install
        fi
        echo -e "${GREEN}🚀 Frontend 시작!${NC}"
        echo
        echo -e "${GREEN}✅ 전체 시스템이 실행되었습니다!${NC}"
        echo -e "${BLUE}📍 웹 애플리케이션: http://localhost:3000${NC}"
        echo -e "${BLUE}📍 API 서버: http://localhost:8080/api${NC}"
        echo -e "${BLUE}📍 H2 콘솔: http://localhost:8080/api/h2-console${NC}"
        echo -e "${BLUE}📍 Swagger: http://localhost:8080/api/swagger-ui.html${NC}"
        echo
        echo -e "${YELLOW}📝 Backend 로그는 backend.log 파일에서 확인할 수 있습니다.${NC}"
        echo -e "${YELLOW}🛑 종료하려면 Ctrl+C를 누르세요.${NC}"
        
        # Frontend 실행 (포그라운드)
        npm run dev
        
        # 종료 시 Backend 프로세스도 종료
        echo -e "${YELLOW}🛑 시스템 종료 중...${NC}"
        kill $BACKEND_PID 2>/dev/null
        ;;
    4)
        echo -e "${GREEN}👋 종료합니다.${NC}"
        exit 0
        ;;
    *)
        echo -e "${RED}❌ 잘못된 선택입니다.${NC}"
        exit 1
        ;;
esac 