# DSFlow 프로젝트 완전 가이드

## 📋 목차
1. [프로젝트 개요](#프로젝트-개요)
2. [기술 스택](#기술-스택)
3. [프로젝트 구조](#프로젝트-구조)
4. [환경 설정](#환경-설정)
5. [백엔드 개발](#백엔드-개발)
6. [프론트엔드 개발](#프론트엔드-개발)
7. [API 설계](#api-설계)
8. [개발 과정과 문제 해결](#개발-과정과-문제-해결)
9. [코드 컨벤션](#코드-컨벤션)
10. [신입 개발자 과제](#신입-개발자-과제)

---

## 프로젝트 개요

### 🎯 DSFlow란?
DSFlow는 **데이터 수집 및 처리를 자동화하는 스케줄링 시스템**입니다.
- 공공 API 데이터 수집 자동화
- 스케줄 기반 Job 실행
- 웹 기반 관리 인터페이스 제공

### 🚀 주요 기능
1. **Job 등록/관리**: 데이터 수집 작업을 등록하고 관리
2. **스케줄링**: Cron 표현식 기반 자동 실행
3. **모니터링**: Job 실행 상태 및 로그 확인
4. **API 연동**: RESTful API를 통한 외부 데이터 수집

---

## 기술 스택

### 🔧 백엔드
- **Spring Boot 3.2.0**: Java 웹 애플리케이션 프레임워크
- **Spring Batch**: 대용량 배치 처리
- **Spring Data JPA**: 데이터베이스 접근 계층
- **H2 Database**: 개발용 인메모리 데이터베이스
- **PostgreSQL**: 운영 데이터베이스
- **Quartz Scheduler**: 스케줄링 엔진

### 🖥️ 프론트엔드
- **React 18**: 사용자 인터페이스 라이브러리
- **TypeScript**: 정적 타입 지원
- **Vite**: 빠른 개발 서버 및 빌드 도구
- **Ant Design**: UI 컴포넌트 라이브러리
- **Axios**: HTTP 클라이언트

### 🐳 인프라
- **Docker**: 컨테이너화
- **Docker Compose**: 다중 컨테이너 관리
- **Nginx**: 프론트엔드 서빙 (운영환경)

---

## 프로젝트 구조

```
DSFlow/
├── backend/                    # Spring Boot 백엔드
│   ├── src/main/java/com/datasolution/dsflow/
│   │   ├── batch/             # Spring Batch 설정
│   │   ├── controller/        # REST API 컨트롤러
│   │   ├── dto/              # 데이터 전송 객체
│   │   ├── entity/           # JPA 엔티티
│   │   ├── exception/        # 예외 처리
│   │   ├── repository/       # 데이터 접근 계층
│   │   ├── service/          # 비즈니스 로직
│   │   └── util/             # 유틸리티 클래스
│   └── src/main/resources/
│       └── application.yml    # 설정 파일
├── frontend/                  # React 프론트엔드
│   ├── src/
│   │   ├── components/       # 재사용 컴포넌트
│   │   ├── pages/           # 페이지 컴포넌트
│   │   └── api.ts           # API 클라이언트
│   ├── package.json         # 의존성 관리
│   └── vite.config.ts       # Vite 설정
├── docker/                   # Docker 설정
│   ├── docker-compose.yml    # 운영환경
│   ├── docker-compose.dev.yml # 개발환경
│   └── init.sql             # DB 초기화
└── README.md
```

---

## 환경 설정

### 🔨 개발 환경 요구사항
- **Java 17**: OpenJDK 또는 Oracle JDK
- **Node.js 18**: JavaScript 런타임
- **Docker & Docker Compose**: 컨테이너 환경
- **Git**: 버전 관리

### 🚀 개발 환경 실행

```bash
# 1. 저장소 클론
git clone <repository-url>
cd DSFlow

# 2. 환경 변수 설정
cp docker/env.example docker/.env

# 3. 개발 서버 실행
cd docker
docker-compose -f docker-compose.dev.yml up -d

# 4. 서비스 접속
# - 프론트엔드: http://localhost:3000
# - 백엔드: http://localhost:8080
# - 데이터베이스: localhost:5432
```

---

## 백엔드 개발

### 📊 데이터베이스 설계

#### JobDefinition 엔티티
```java
@Entity
@Table(name = "job_definitions")
public class JobDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String jobCode;        // Job 고유 코드
    
    private String jobName;        // Job 이름
    private String description;    // 설명
    
    @Enumerated(EnumType.STRING)
    private MethodType methodType; // API_GET, API_POST, FILE_DOWNLOAD
    
    private String resourceUrl;    // 데이터 소스 URL
    private String parameters;     // JSON 형태의 파라미터
    private String cronExpression; // Cron 스케줄 표현식
    
    private Integer resourceWeight; // 리소스 가중치 (1-10)
    
    @Enumerated(EnumType.STRING)
    private JobStatus status;      // ACTIVE, INACTIVE
}
```

#### JobExecutionLog 엔티티
```java
@Entity
@Table(name = "job_execution_logs")
public class JobExecutionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String jobCode;        // 실행된 Job 코드
    private LocalDateTime startTime; // 시작 시간
    private LocalDateTime endTime;   // 종료 시간
    
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status; // SUCCESS, FAILED, RUNNING
    
    @Lob
    private String errorMessage;   // 에러 메시지
    
    @Lob
    private String resultData;     // 실행 결과 데이터
}
```

### 🔧 핵심 서비스 구현

#### JobDefinitionService
```java
@Service
@Transactional
public class JobDefinitionService {
    
    // Job 생성
    public JobDefinitionDto createJob(JobDefinitionDto dto) {
        // 1. 중복 Job 코드 검증
        validateJobCode(dto.getJobCode());
        
        // 2. Cron 표현식 검증
        validateCronExpression(dto.getCronExpression());
        
        // 3. 엔티티 생성 및 저장
        JobDefinition entity = JobDefinition.builder()
            .jobCode(dto.getJobCode())
            .jobName(dto.getJobName())
            // ... 기타 필드
            .build();
            
        return convertToDto(repository.save(entity));
    }
    
    // Cron 표현식 검증
    private void validateCronExpression(String cronExpression) {
        try {
            CronExpression.parse(cronExpression);
        } catch (Exception e) {
            throw new BusinessException("잘못된 Cron 표현식입니다");
        }
    }
}
```

### 🌐 REST API 설계

#### JobDefinitionController
```java
@RestController
@RequestMapping("/jobs")
public class JobDefinitionController {
    
    @PostMapping
    public ResponseEntity<JobDefinitionDto> createJob(@Valid @RequestBody JobDefinitionDto dto) {
        return ResponseEntity.ok(jobDefinitionService.createJob(dto));
    }
    
    @GetMapping
    public ResponseEntity<Page<JobDefinitionDto>> getAllJobs(Pageable pageable) {
        return ResponseEntity.ok(jobDefinitionService.getAllJobs(pageable));
    }
    
    @GetMapping("/{jobCode}")
    public ResponseEntity<JobDefinitionDto> getJob(@PathVariable String jobCode) {
        return ResponseEntity.ok(jobDefinitionService.getJobByCode(jobCode));
    }
}
```

---

## 프론트엔드 개발

### ⚛️ React 컴포넌트 구조

#### 페이지 컴포넌트
```
src/pages/
├── Dashboard.tsx      # 대시보드 (메인 페이지)
├── JobForm.tsx       # Job 등록/수정 폼
├── JobList.tsx       # Job 목록
└── LogList.tsx       # 실행 로그 목록
```

#### 레이아웃 컴포넌트
```
src/components/layout/
├── AppHeader.tsx     # 상단 헤더
└── AppSider.tsx      # 사이드바 네비게이션
```

### 🎨 UI/UX 디자인

#### JobForm 컴포넌트 (사용자 친화적 스케줄 입력)
```typescript
const JobForm: React.FC = () => {
  const [scheduleType, setScheduleType] = useState('daily');
  
  // Cron 표현식 자동 생성
  const generateCronExpression = (scheduleType: string, executeTime: any, dayOfWeek?: number) => {
    const hour = executeTime.hour();
    const minute = executeTime.minute();
    
    switch (scheduleType) {
      case 'daily':
        return `0 ${minute} ${hour} * * ?`;
      case 'weekly':
        return `0 ${minute} ${hour} ? * ${dayOfWeek}`;
      case 'monthly':
        return `0 ${minute} ${hour} ${dayOfMonth} * ?`;
    }
  };
  
  return (
    <Form onFinish={onFinish}>
      {/* 스케줄 타입 선택 */}
      <Form.Item name="scheduleType" label="실행 주기">
        <Radio.Group onChange={(e) => setScheduleType(e.target.value)}>
          <Radio value="daily">매일</Radio>
          <Radio value="weekly">매주</Radio>
          <Radio value="monthly">매월</Radio>
        </Radio.Group>
      </Form.Item>
      
      {/* 실행 시간 선택 */}
      <Form.Item name="executeTime" label="실행 시간">
        <TimePicker format="HH:mm" />
      </Form.Item>
      
      {/* 조건부 렌더링: 요일/날짜 선택 */}
      {scheduleType === 'weekly' && (
        <Form.Item name="dayOfWeek" label="요일">
          <Select>
            <Option value={1}>월요일</Option>
            <Option value={2}>화요일</Option>
            {/* ... */}
          </Select>
        </Form.Item>
      )}
    </Form>
  );
};
```

### 🔗 API 클라이언트

#### api.ts
```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Job 생성
export const createJob = async (jobData: JobDefinitionDto): Promise<JobDefinitionDto> => {
  const response = await api.post('/jobs', jobData);
  return response.data;
};

// Job 목록 조회
export const getJobs = async (page = 0, size = 20): Promise<Page<JobDefinitionDto>> => {
  const response = await api.get(`/jobs?page=${page}&size=${size}`);
  return response.data;
};
```

---

## API 설계

### 📝 API 명세서

#### Job 관리 API

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/jobs` | Job 생성 | JobDefinitionDto | JobDefinitionDto |
| GET | `/api/jobs` | Job 목록 조회 | - | Page<JobDefinitionDto> |
| GET | `/api/jobs/{jobCode}` | Job 상세 조회 | - | JobDefinitionDto |
| PUT | `/api/jobs/{jobCode}` | Job 수정 | JobDefinitionDto | JobDefinitionDto |
| DELETE | `/api/jobs/{jobCode}` | Job 삭제 | - | - |

#### 실행 로그 API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/logs` | 실행 로그 목록 |
| GET | `/api/logs/{jobCode}` | 특정 Job 실행 로그 |

### 🔍 DTO 설계

#### JobDefinitionDto
```java
public class JobDefinitionDto {
    private Long id;
    
    @NotBlank(message = "Job 코드는 필수입니다")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "대문자, 숫자, 언더스코어만 사용 가능")
    private String jobCode;
    
    @NotBlank(message = "Job 이름은 필수입니다")
    private String jobName;
    
    private String description;
    
    @NotNull(message = "메소드 타입은 필수입니다")
    private MethodType methodType;
    
    @NotBlank(message = "리소스 URL은 필수입니다")
    @URL(message = "올바른 URL 형식이 아닙니다")
    private String resourceUrl;
    
    private String parameters;
    
    @NotBlank(message = "Cron 표현식은 필수입니다")
    private String cronExpression;
    
    @Min(value = 1, message = "리소스 가중치는 1 이상이어야 합니다")
    @Max(value = 10, message = "리소스 가중치는 10 이하여야 합니다")
    private Integer resourceWeight;
    
    @NotNull(message = "상태는 필수입니다")
    private JobStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 개발 과정과 문제 해결

### 🚧 주요 개발 이슈들

#### 1. Maven Wrapper 문제
**문제**: `.mvn/wrapper` 디렉토리가 없어서 빌드 실패
```bash
Error: Could not find or load main class org.apache.maven.wrapper.MavenWrapperMain
```

**해결책**:
```bash
# Maven Wrapper 생성
mvn wrapper:wrapper

# 실행 권한 부여
chmod +x mvnw
```

#### 2. Docker 네트워킹 문제
**문제**: 프론트엔드에서 백엔드 API 호출 시 500 오류
- 프록시 설정: `localhost:8080` → 연결 실패
- 백엔드 context path: `/api`

**해결책**:
```typescript
// vite.config.ts
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://dsflow-backend-dev:8080', // 컨테이너명 사용
        changeOrigin: true,
      },
    },
  },
});
```

```yaml
# docker-compose.dev.yml
services:
  frontend-dev:
    networks:
      - dsflow-network
  backend-dev:
    networks:
      - dsflow-network
networks:
  dsflow-network:
    driver: bridge
```

#### 3. Cron 표현식 검증 문제
**문제**: Spring Boot의 `@Pattern` 정규식이 Quartz Cron 형식과 불일치

**해결책**:
```java
@Component
public class CronExpressionValidator {
    public boolean isValid(String cronExpression) {
        try {
            CronExpression.parse(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

#### 4. 사용자 경험 개선
**문제**: 복잡한 Cron 표현식을 사용자가 직접 입력하기 어려움

**해결책**: 사용자 친화적 UI 구현
- 실행 주기 선택 (매일/매주/매월)
- TimePicker로 시간 선택
- 요일/날짜 선택 드롭다운
- 자동 Cron 표현식 생성

---

## 코드 컨벤션

### ☕ Java (백엔드)

#### 네이밍 컨벤션
```java
// 클래스: PascalCase
public class JobDefinitionService { }

// 메소드/변수: camelCase
private String jobCode;
public void createJob() { }

// 상수: UPPER_SNAKE_CASE
public static final String DEFAULT_STATUS = "ACTIVE";

// 패키지: lowercase
package com.datasolution.dsflow.service;
```

#### 어노테이션 규칙
```java
@Entity
@Table(name = "job_definitions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDefinition {
    // 필드들...
}
```

#### 예외 처리
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        return ResponseEntity.badRequest()
            .body(ErrorResponse.of(e.getMessage()));
    }
}
```

### ⚛️ TypeScript (프론트엔드)

#### 네이밍 컨벤션
```typescript
// 컴포넌트: PascalCase
const JobForm: React.FC = () => { };

// 변수/함수: camelCase
const jobData = useState();
const handleSubmit = () => { };

// 상수: UPPER_SNAKE_CASE
const API_BASE_URL = '/api';

// 타입/인터페이스: PascalCase
interface JobDefinitionDto {
  jobCode: string;
  jobName: string;
}
```

#### React 컴포넌트 구조
```typescript
import React, { useState, useEffect } from 'react';
import { Form, Button } from 'antd';

interface Props {
  onSubmit: (data: JobData) => void;
}

const JobForm: React.FC<Props> = ({ onSubmit }) => {
  // 1. State 선언
  const [loading, setLoading] = useState(false);
  
  // 2. Effect 훅
  useEffect(() => {
    // 초기화 로직
  }, []);
  
  // 3. 이벤트 핸들러
  const handleSubmit = async (values: any) => {
    setLoading(true);
    try {
      await onSubmit(values);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };
  
  // 4. 렌더링
  return (
    <Form onFinish={handleSubmit}>
      {/* JSX */}
    </Form>
  );
};

export default JobForm;
```

### 📁 파일/폴더 명명 규칙

#### 백엔드
```
controller/     # REST 컨트롤러
service/        # 비즈니스 로직
repository/     # 데이터 접근 계층
entity/         # JPA 엔티티
dto/           # 데이터 전송 객체
exception/     # 예외 클래스
util/          # 유틸리티 클래스
config/        # 설정 클래스
```

#### 프론트엔드
```
pages/         # 페이지 컴포넌트 (PascalCase.tsx)
components/    # 재사용 컴포넌트
api/          # API 클라이언트 (camelCase.ts)
types/        # TypeScript 타입 정의
utils/        # 유틸리티 함수
```

---

## 과제

### 📚 Level 1: 프로젝트 이해 및 환경 구축

#### 환경 설정 및 기술 스택 파악
**목표**: 개발 환경 구축 및 프로젝트 전체 구조 이해

**과제**:
1. **개발 환경 설정**
   - DSFlow 프로젝트 클론 및 실행
   - Docker 환경 이해 및 서비스 확인
   - 개발/운영 환경 차이점 파악

2. **아키텍처 분석**
   - 백엔드/프론트엔드 구조 이해
   - 데이터베이스 스키마 분석
   - API 설계 패턴 파악

**확인 사항**:
- [ ] 프로젝트가 정상적으로 실행되는가?
- [ ] 전체 시스템 아키텍처를 이해했는가?
- [ ] 기존 코드의 패턴과 컨벤션을 파악했는가?

#### 코드 분석 및 이해
**목표**: 기존 코드 구조 파악 및 개선점 도출

**과제**:
1. **코드 리뷰**
   - 전체 소스 코드 분석 및 주석 추가
   - 데이터 흐름 및 비즈니스 로직 파악
   - API 명세서 정리 및 테스트

2. **기술적 분석**
   - 성능 병목 지점 파악
   - 보안 취약점 검토
   - 코드 품질 개선 방안 제시

**제출물**:
- 코드 분석 보고서 (마크다운)
- API 테스트 결과 및 개선 제안서
- 기술적 개선 방안 문서

### 🛠️ Level 2: 핵심 기능 개발

#### Job 실행 통계 대시보드 구현
**목표**: 데이터 시각화 및 분석 기능 구현

**과제**: **Job 실행 히스토리 통계 기능 개발**

**요구사항**:
1. **백엔드**
   - `JobStatisticsController` 및 서비스 계층 구현
   - 성공/실패율, 실행 시간 통계 API
   - 일별/주별/월별 집계 데이터 제공
   - 성능 최적화를 고려한 쿼리 작성

2. **프론트엔드**
   - 인터랙티브 통계 대시보드 구현
   - Chart.js 또는 Ant Design Charts 활용
   - 실시간 필터링 및 드릴다운 기능
   - 반응형 디자인 적용

**기술 요구사항**:
- 기존 아키텍처 패턴 준수
- 포괄적인 에러 처리 및 로깅
- RESTful API 설계 원칙
- 사용자 경험 최적화

#### 실시간 모니터링 시스템 구축
**목표**: 운영 모니터링 및 알림 시스템 구현

**과제**: **실시간 Job 모니터링 및 알림 기능**

**요구사항**:
1. **실시간 통신**
   - WebSocket 기반 실시간 상태 업데이트
   - 브라우저 푸시 알림 시스템
   - 장애 발생 시 즉시 알림

2. **성능 및 확장성**
   - 데이터베이스 인덱싱 최적화
   - 프론트엔드 성능 튜닝
   - 메모리 및 리소스 효율성 개선
   - 대용량 데이터 처리 최적화

**평가 기준**:
- 시스템 아키텍처 설계 능력
- 코드 품질 및 유지보수성
- 성능 최적화 효과
- 사용자 경험 및 운영 편의성

### 🎯 Level 3: 프로덕션 레벨 개발

#### 테스트 자동화 및 품질 보증
**목표**: 프로덕션 레벨 코드 품질 및 안정성 확보

**과제**:
1. **포괄적인 테스트 구현**
   - 유닛 테스트 (JUnit 5, Jest) - 90% 이상 커버리지
   - 통합 테스트 (TestContainers, Spring Boot Test)
   - E2E 테스트 (Cypress, Playwright)
   - 성능 테스트 및 부하 테스트

2. **문서화 자동화**
   - OpenAPI/Swagger 기반 API 문서
   - 코드 주석 및 JavaDoc 완성
   - 아키텍처 결정 기록 (ADR) 작성
   - 운영 가이드 및 트러블슈팅 문서

#### DevOps 및 운영 환경 구축
**목표**: 안정적인 배포 및 운영 시스템 구현

**과제**:
1. **CI/CD 파이프라인 고도화**
   - GitHub Actions 워크플로우 최적화
   - 무중단 배포 (Blue-Green, Canary)
   - 환경별 설정 관리 (Config Map, Secrets)
   - 보안 스캔 및 취약점 검사 자동화

2. **관찰 가능성 (Observability) 구현**
   - 구조화된 로깅 (Structured Logging)
   - 메트릭 수집 및 대시보드 (Micrometer, Prometheus)
   - 분산 추적 (Distributed Tracing)
   - 장애 복구 및 SLA 모니터링

### 📊 과제 평가 기준

#### 코드 품질 (40%)
- [ ] 코딩 컨벤션 준수
- [ ] Clean Code 원칙 적용
- [ ] 디자인 패턴 적절한 사용
- [ ] 에러 처리 완성도

#### 기능 구현 (30%)
- [ ] 요구사항 충족도
- [ ] 사용자 경험 품질
- [ ] 성능 최적화
- [ ] 보안 고려사항

#### 문서화 (20%)
- [ ] 코드 주석 품질
- [ ] API 문서 완성도
- [ ] 사용자 매뉴얼
- [ ] 개발 과정 정리

#### 협업 및 소통 (10%)
- [ ] Git 사용 숙련도
- [ ] 코드 리뷰 참여
- [ ] 질문 및 토론 적극성
- [ ] 문제 해결 능력

### 💡 학습 리소스

#### 공식 문서
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [React Official Docs](https://react.dev/learn)
- [Docker Documentation](https://docs.docker.com/)

#### 추천 도서
- "스프링 부트와 AWS로 혼자 구현하는 웹 서비스" - 이동욱
- "리팩토링" - 마틴 파울러
- "클린 코드" - 로버트 C. 마틴

#### 온라인 강의
- 김영한의 스프링 부트 시리즈
- React 공식 튜토리얼
- Docker 기초 강의

---

## 📞 지원 및 문의

### 🙋‍♂️ 질문 가이드라인
1. **먼저 시도해볼 것들**
   - 공식 문서 확인
   - 에러 메시지 구글링
   - 기존 코드에서 유사 패턴 찾기

2. **질문할 때 포함할 정보**
   - 시도한 내용
   - 에러 메시지 (전체)
   - 예상 결과 vs 실제 결과
   - 환경 정보 (OS, 버전 등)

3. **효과적인 질문 방법**
   - 구체적이고 명확하게
   - 스크린샷이나 코드 첨부
   - 한 번에 하나의 문제만

### 📧 연락처
- **긴급 연락**: Slack #dsflow-support

---

*이 문서는 DSFlow 프로젝트의 개발 가이드입니다. 신입 개발자가 프로젝트를 이해하고 기여할 수 있도록 체계적으로 구성되었습니다.* 