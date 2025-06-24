# DSFlow - 배치 관리 시스템

DataSolution에서 개발한 공공 API 기반 데이터 수집, 스케줄링, 상태 추적 및 알림이 가능한 배치 관리 시스템입니다.

## 🚀 기술 스택

### 백엔드
- Spring Boot 3.2.0
- Spring Batch 5.0
- Spring Data JPA
- PostgreSQL 15
- Swagger UI
- Slack Webhook 알림

### 프론트엔드
- React 18
- TypeScript
- Vite
- Ant Design 5.x
- React Query
- Zustand

## 📁 프로젝트 구조

```
DSFlow/
├── backend/                 # Spring Boot 백엔드
├── frontend/               # React 프론트엔드
├── docker/                 # Docker 설정
├── docs/                   # 문서
└── README.md
```

## 🐳 빠른 시작

### 전체 시스템 실행 (Docker Compose)

#### 사전 준비
```bash
# Docker 설치 여부 확인
docker --version
docker-compose --version
```

**Docker가 설치되어 있지 않다면:**
- macOS: `brew install --cask docker` 또는 [Docker Desktop](https://www.docker.com/products/docker-desktop/) 다운로드
- Docker Desktop 실행 후 시스템 트레이에 아이콘이 초록색이 될 때까지 대기

#### 실행 단계
```bash
# 1. 환경 변수 설정 (이미 완료됨)
# cp docker/.env.example docker/.env  # 이미 .env 파일이 존재함
# .env 파일에 실제 Slack Webhook URL이 설정되어 있음

# 2. 전체 시스템 빌드 및 실행
docker-compose -f docker/docker-compose.yml up --build -d

# 3. 서비스 상태 확인
docker-compose -f docker/docker-compose.yml ps

# 4. 로그 확인 (선택사항)
docker-compose -f docker/docker-compose.yml logs -f
```

#### 접속 정보
- **프론트엔드**: http://localhost:3000
- **백엔드 API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **데이터베이스**: localhost:5432 (dsflow/password)

#### 시스템 종료
```bash
# 서비스 중지
docker-compose -f docker/docker-compose.yml down

# 데이터까지 완전 삭제
docker-compose -f docker/docker-compose.yml down -v
```

> 📝 **자세한 실행 가이드**: [DOCKER_RUN_GUIDE.md](./DOCKER_RUN_GUIDE.md)를 참조하세요.

#### 🎯 한 줄 실행 (권장)
```bash
# 실행 권한 부여 (최초 1회)
chmod +x run.sh

# 전체 시스템 자동 실행
./run.sh
```

### 개발 모드 실행

#### 백엔드
```bash
cd backend
./mvnw spring-boot:run
```

#### 프론트엔드
```bash
cd frontend
npm install
npm run dev
```

## 📝 주요 기능

- ✅ Job 정의 등록/수정/삭제
- ✅ 스케줄 기반 자동 실행
- ✅ 수동 Job 실행
- ✅ 실행 로그 추적 및 조회
- ✅ 리소스 가중치 기반 실행 시각 추천
- ✅ Slack 알림 (실패 시)
- ✅ 대시보드 통계

## 🛠️ API 문서

백엔드 실행 후 Swagger UI에서 확인: http://localhost:8080/swagger-ui.html

## 📧 문의

DataSolution 개발팀 