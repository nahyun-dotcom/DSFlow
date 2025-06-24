# DSFlow 배포 가이드

## 환경별 배포 방법

### 1. 개발 환경 (로컬)
```bash
# PostgreSQL 컨테이너 + init.sql 자동 실행
docker-compose -f docker/docker-compose.dev.yml up -d

# 또는 개별 서비스 실행
cd frontend && npm run dev
cd backend && mvn spring-boot:run
```

**특징:**
- ✅ `init.sql` 파일로 DB 자동 초기화
- ✅ 샘플 데이터 자동 생성
- ✅ 코드 변경 시 Hot Reload

### 2. 스테이징 환경
```bash
# 내장 PostgreSQL 사용 (운영과 유사한 환경)
docker-compose -f docker/docker-compose.yml up -d
```

**특징:**
- ✅ `init.sql` 파일로 DB 초기화
- ✅ 프로덕션 빌드 테스트
- ✅ 운영과 동일한 컨테이너 구성

### 3. 운영 환경
```bash
# 외부 PostgreSQL 서버 사용
docker-compose -f docker/docker-compose.prod.yml --env-file docker/env.prod up -d
```

**특징:**
- ❌ `init.sql` 불필요 (외부 DB 사용)
- ✅ 환경변수로 DB 연결 설정
- ✅ 포트 80 사용
- ✅ PostgreSQL 컨테이너 제외

## init.sql 파일의 역할

### 개발/스테이징 단계
- **필요함** ✅
- PostgreSQL 컨테이너 최초 시작 시 실행
- 테이블 생성 + 샘플 데이터 삽입
- 매번 새로운 환경에서 일관된 DB 상태 보장

### 운영 배포 후
- **불필요함** ❌
- 이미 설정된 외부 PostgreSQL 서버 사용
- 실제 데이터가 있는 운영 DB에 연결
- init.sql은 개발용으로만 활용

## 운영 배포 체크리스트

### 1. 사전 준비
- [ ] PostgreSQL 서버 설정 완료
- [ ] 데이터베이스 및 사용자 계정 생성
- [ ] 방화벽 설정 (포트 5432 허용)

### 2. 초기 데이터베이스 설정
```sql
-- 운영 DB에서 최초 1회 실행
\i /path/to/init.sql
```

### 3. 환경변수 설정
```bash
# docker/env.prod 파일 수정
DB_HOST=your-production-db-server.com
DB_PASSWORD=secure-production-password
```

### 4. 배포 실행
```bash
# 운영 배포
docker-compose -f docker/docker-compose.prod.yml --env-file docker/env.prod up -d

# 로그 확인
docker-compose -f docker/docker-compose.prod.yml logs -f
```

### 5. 배포 후 확인
- [ ] 애플리케이션 접속 확인 (http://your-server)
- [ ] DB 연결 상태 확인
- [ ] API 동작 테스트
- [ ] 로그 모니터링

## 배포 전략 요약

| 환경 | PostgreSQL | init.sql | 포트 | 명령어 |
|------|------------|----------|------|--------|
| 개발 | 컨테이너 | ✅ 필요 | 3000/8080 | `docker-compose -f dev.yml up` |
| 스테이징 | 컨테이너 | ✅ 필요 | 3000/8080 | `docker-compose up` |
| 운영 | 외부 서버 | ❌ 불필요 | 80/8080 | `docker-compose -f prod.yml up` |

## 마이그레이션 시나리오

만약 개발 환경에서 운영 환경으로 데이터를 마이그레이션해야 한다면:

```bash
# 1. 개발 DB 덤프
docker exec dsflow-postgres pg_dump -U dsflow dsflow > backup.sql

# 2. 운영 DB로 복원
psql -h production-server -U dsflow -d dsflow < backup.sql
``` 