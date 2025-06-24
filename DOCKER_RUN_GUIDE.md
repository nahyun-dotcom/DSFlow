# DSFlow 전체 시스템 실행 가이드 (Docker Compose)

## 🚨 사전 요구사항

### 1. Docker 설치 확인
```bash
# Docker 설치 여부 확인
docker --version
docker-compose --version
```

**만약 Docker가 설치되어 있지 않다면:**

#### macOS에서 Docker 설치:
```bash
# Option 1: Homebrew 사용 (권장)
brew install --cask docker

# Option 2: 직접 다운로드
# https://www.docker.com/products/docker-desktop/ 에서 macOS 버전 다운로드
```

#### Docker Desktop 실행:
1. Applications 폴더에서 Docker 앱 실행
2. 시스템 트레이에 Docker 아이콘이 나타날 때까지 대기
3. 아이콘이 초록색이 되면 준비 완료

---

## 📋 단계별 실행 방법

### 1단계: 프로젝트 루트 디렉토리로 이동
```bash
cd /Users/nahyunpark/workspace/DSFlow
```

### 2단계: 환경 변수 설정 (이미 완료됨)
```bash
# .env 파일이 이미 존재하고 실제 Slack 웹훅 URL이 설정되어 있습니다
# 추가 설정이 필요한 경우:
# vim docker/.env
```

**현재 .env 파일 내용:**
```bash
# Slack 웹훅 URL (Job 실패 시 알림용)
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/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

# 데이터베이스 설정
DB_USERNAME=dsflow
DB_PASSWORD=password
DB_NAME=dsflow

# 기타 설정
SPRING_PROFILES_ACTIVE=prod
```

### 3단계: 기존 컨테이너 정리 (선택사항)
```bash
# 이전에 실행된 컨테이너가 있다면 정리
docker-compose -f docker/docker-compose.yml down -v
```

### 4단계: 전체 시스템 빌드 및 실행
```bash
# 백그라운드에서 모든 서비스 빌드 및 실행
docker-compose -f docker/docker-compose.yml up --build -d
```

**실행되는 서비스:**
- **PostgreSQL**: 데이터베이스 (포트 5432)
- **Backend**: Spring Boot API 서버 (포트 8080)
- **Frontend**: React 웹 애플리케이션 (포트 3000)

### 5단계: 서비스 상태 확인
```bash
# 모든 컨테이너 상태 확인
docker-compose -f docker/docker-compose.yml ps

# 실시간 로그 확인 (선택사항)
docker-compose -f docker/docker-compose.yml logs -f

# 특정 서비스 로그만 확인
docker-compose -f docker/docker-compose.yml logs backend
docker-compose -f docker/docker-compose.yml logs frontend
docker-compose -f docker/docker-compose.yml logs postgres
```

---

## 🌐 서비스 접속 정보

시스템이 정상적으로 실행되면 다음 주소로 접속할 수 있습니다:

| 서비스 | URL | 설명 |
|--------|-----|------|
| **프론트엔드** | http://localhost:3000 | React 웹 애플리케이션 |
| **백엔드 API** | http://localhost:8080 | Spring Boot REST API |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | API 문서 및 테스트 |
| **데이터베이스** | localhost:5432 | PostgreSQL (dsflow/password) |

---

## 🛠️ 문제 해결

### 컨테이너가 시작되지 않는 경우:
```bash
# 1. 컨테이너 상태 확인
docker-compose -f docker/docker-compose.yml ps

# 2. 로그 확인
docker-compose -f docker/docker-compose.yml logs

# 3. 완전히 정리하고 다시 시작
docker-compose -f docker/docker-compose.yml down -v
docker system prune -f
docker-compose -f docker/docker-compose.yml up --build -d
```

### 포트 충돌이 발생하는 경우:
```bash
# 포트 사용 중인 프로세스 확인
lsof -i :3000
lsof -i :8080
lsof -i :5432

# 프로세스 종료 후 다시 실행
```

### 데이터베이스 초기화가 필요한 경우:
```bash
# PostgreSQL 볼륨 삭제 후 재시작
docker-compose -f docker/docker-compose.yml down -v
docker volume rm docker_postgres_data
docker-compose -f docker/docker-compose.yml up --build -d
```

---

## 🔧 개발 모드 실행

개발 중 코드 변경사항을 빠르게 반영하려면:

```bash
# 특정 서비스만 재빌드
docker-compose -f docker/docker-compose.yml up --build backend -d
docker-compose -f docker/docker-compose.yml up --build frontend -d

# 로그를 실시간으로 보면서 실행
docker-compose -f docker/docker-compose.yml up --build
```

---

## 📊 시스템 종료

```bash
# 모든 서비스 중지 (데이터 보존)
docker-compose -f docker/docker-compose.yml stop

# 모든 서비스 중지 및 컨테이너 삭제 (데이터 보존)
docker-compose -f docker/docker-compose.yml down

# 모든 서비스 중지 및 볼륨까지 삭제 (데이터 완전 삭제)
docker-compose -f docker/docker-compose.yml down -v
```

---

## 📝 샘플 데이터

시스템 실행 시 다음 샘플 Job이 자동으로 생성됩니다:

1. **SAMPLE_API_JOB**: 5분마다 실행되는 샘플 API 호출
2. **HEALTH_CHECK**: 1분마다 실행되는 헬스체크

이 샘플 데이터는 프론트엔드에서 확인하거나 Swagger UI에서 테스트할 수 있습니다.

---

## ⚡ 빠른 실행 명령어

```bash
# 한 줄로 전체 시스템 실행
cd /Users/nahyunpark/workspace/DSFlow && docker-compose -f docker/docker-compose.yml up --build -d

# 실행 후 상태 확인
docker-compose -f docker/docker-compose.yml ps && echo "🌐 프론트엔드: http://localhost:3000" && echo "🔧 백엔드: http://localhost:8080" && echo "📚 Swagger: http://localhost:8080/swagger-ui.html"
``` 