# DSFlow - 배치 관리 시스템

공공데이터 API 배치 수집을 위한 종합 관리 시스템입니다. 
사용자가 직접 어떤 API든 등록하여 자동으로 코드 데이터를 동기화할 수 있으며, 
유연한 파라미터 조합으로 대용량 배치 작업을 효율적으로 처리할 수 있습니다.

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

## 🚀 빠른 시작

> 💡 **Docker 메모리 부족 시 로컬 개발환경 사용을 권장합니다!**

### 방법 1: 로컬 개발환경 (권장)

**자동 실행 스크립트:**
```bash
# 저장소 클론
git clone <repository-url>
cd DSFlow


**수동 실행:**
```bash
# Backend 실행 (터미널 1)
cd backend
./mvnw spring-boot:run

# Frontend 실행 (터미널 2)  
cd frontend
npm install
npm run dev
```

**로컬 환경 접속 정보:**
- 🌐 **웹 애플리케이션**: http://localhost:3000
- 🔗 **API 서버**: http://localhost:8080/api  
- 📊 **H2 데이터베이스 콘솔**: http://localhost:8080/api/h2-console
  - JDBC URL: `jdbc:h2:mem:dsflow`
  - Username: `sa`, Password: (비어둠)
- 📚 **Swagger API 문서**: http://localhost:8080/api/swagger-ui.html

> 📖 **상세 가이드**: [로컬_개발환경_가이드.md](./로컬_개발환경_가이드.md) 참조

### 방법 2: Docker 환경 (프로덕션용)

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

### 🔄 코드 동기화 시스템
- **어떤 API든 직접 등록 가능**: 정부기관, 공공데이터포털, 상용서비스, 내부시스템 API 모두 지원
- **자유로운 설정**: HTTP 메소드, 인증 헤더, 파라미터, JSON 응답 경로 매핑 완전 커스터마이징
- **자동 스케줄링**: Cron 표현식으로 주기적 자동 동기화
- **API 테스트 기능**: 등록 전 실제 호출 테스트로 검증
- **데이터 품질 관리**: 백업/복원, 변경 이력 추적

### 🎯 job 생성시 파라미터 조합 시스템  
- **다양한 값 소스**: DB 쿼리, 정적 값, 날짜 범위, API 호출, 파일 읽기
- **매트릭스 조합**: 데카르트 곱으로 모든 파라미터 조합 자동 생성
- **배치 처리**: API 호출 제한 대응 (배치 크기, 지연 시간 설정)
- **동적 파라미터**: 실행 시점에 값 동적 생성 및 조합

### 🏗️ 배치 작업 관리
- ✅ Job 정의 등록/수정/삭제
- ✅ 스케줄 기반 자동 실행
- ✅ 수동 Job 실행
- ✅ 실행 로그 추적 및 조회
- ✅ 리소스 가중치 기반 실행 시각 추천
- ✅ Slack 알림 (실패 시)
- ✅ 대시보드 통계

## 🛠️ API 문서

백엔드 실행 후 Swagger UI에서 확인: http://localhost:8080/swagger-ui.html

