# 공공데이터 API 다중 파라미터 배치 작업 가이드

## 개요

이 가이드는 공공데이터 API의 다중 파라미터 조합 호출을 위한 배치 작업 시스템 사용법을 설명합니다. 특히 부동산 실거래가 데이터와 같이 지역코드(LAWD_CD)와 계약연월(DEAL_YMD)을 조합하여 전국 데이터를 수집하는 방법을 다룹니다.

## 시스템 특징

### 1. 다중 파라미터 조합 지원
- **SINGLE**: 단일 파라미터 작업
- **MULTI_REGION**: 다중 지역코드 작업
- **MULTI_DATE**: 다중 날짜 작업  
- **MATRIX**: 지역코드 × 날짜 매트릭스 작업

### 2. API 제한 대응
- 배치 크기 조절 (한 번에 처리할 파라미터 조합 수)
- API 호출 간 지연 시간 설정
- 시간차 분산 호출

### 3. 자동화 스케줄링
- Cron 표현식을 통한 정기 실행
- 매일/매주/매월 자동 데이터 수집

## 사용 방법

### 1. 지역코드 관리

#### 지역코드 조회
```bash
# 활성화된 지역코드 목록 조회
curl -X GET "http://localhost:8080/api/region-codes"

# 지역코드만 조회
curl -X GET "http://localhost:8080/api/region-codes/codes"

# 지역코드 개수 조회
curl -X GET "http://localhost:8080/api/region-codes/count"
```

#### 지역코드 추가
```bash
curl -X POST "http://localhost:8080/api/region-codes" \
  -d "lawdCd=26110" \
  -d "regionName=부산광역시 중구" \
  -d "sidoName=부산광역시" \
  -d "gugunName=중구"
```

#### 지역코드 상태 변경
```bash
# 비활성화
curl -X PATCH "http://localhost:8080/api/region-codes/1/status?isActive=false"

# 활성화
curl -X PATCH "http://localhost:8080/api/region-codes/1/status?isActive=true"
```

### 2. Job 정의 등록

#### 아파트 실거래가 수집 Job 등록 예시
```json
{
  "jobCode": "REAL_ESTATE_APARTMENT_TRADE",
  "jobName": "아파트 실거래가 데이터 수집",
  "description": "국토교통부 부동산 실거래가 공공 API를 통한 아파트 매매 실거래 정보 수집",
  "methodType": "API_GET",
  "resourceUrl": "http://openapi.molit.go.kr/OpenAPI_ToolInstallPackage/service/rest/RTMSOBJSvc/getRTMSDataSvcAptTradeDev",
  "parameters": "{\"serviceKey\":\"YOUR_SERVICE_KEY\",\"numOfRows\":\"1000\",\"pageNo\":\"1\"}",
  "cronExpression": "0 0 2 * * ?",
  "resourceWeight": 2,
  "parameterType": "MATRIX",
  "batchSize": 10,
  "delaySeconds": 1,
  "useRegionCodes": true,
  "dateRangeMonths": 3
}
```

#### 주요 파라미터 설명

- **parameterType**: 
  - `MATRIX`: 지역코드 × 날짜 조합 (전국 × 최근 N개월)
  - `MULTI_REGION`: 지역코드별 단일 날짜
  - `MULTI_DATE`: 단일 지역 × 여러 날짜
  - `SINGLE`: 단일 파라미터

- **batchSize**: 한 번에 처리할 파라미터 조합 수 (API 제한 고려)
- **delaySeconds**: API 호출 간 지연 시간 (초)
- **dateRangeMonths**: 처리할 월 범위 (현재 기준 과거 N개월)

### 3. Job 실행

#### 수동 실행
```bash
curl -X POST "http://localhost:8080/api/jobs/execute" \
  -H "Content-Type: application/json" \
  -d "{\"jobCode\": \"REAL_ESTATE_APARTMENT_TRADE\"}"
```

#### 스케줄 실행
- Cron 표현식에 따라 자동 실행
- `0 0 2 * * ?`: 매일 새벽 2시 실행
- `0 30 2 * * ?`: 매일 새벽 2시 30분 실행

### 4. 실행 모니터링

#### 실행 로그 조회
```bash
# 특정 Job의 실행 로그 조회
curl -X GET "http://localhost:8080/api/logs?jobCode=REAL_ESTATE_APARTMENT_TRADE"

# 최근 실행 로그 조회
curl -X GET "http://localhost:8080/api/logs?limit=10"
```

## 실제 사용 시나리오

### 시나리오 1: 서울시 아파트 실거래가 수집

1. **지역코드 설정**: 서울시 25개 구 지역코드 활성화
2. **Job 설정**: 
   - parameterType: `MATRIX`
   - batchSize: `5` (동시 호출 제한)
   - delaySeconds: `2` (API 제한 대응)
   - dateRangeMonths: `6` (최근 6개월)

3. **예상 호출 수**: 25개 구 × 6개월 = 150개 API 호출
4. **예상 소요 시간**: (150 ÷ 5) × 2초 = 60초

### 시나리오 2: 전국 오피스텔 실거래가 수집

1. **지역코드 설정**: 전국 시/군/구 지역코드 활성화 (약 250개)
2. **Job 설정**:
   - parameterType: `MATRIX`
   - batchSize: `20` (큰 배치 크기)
   - delaySeconds: `1` (빠른 수집)
   - dateRangeMonths: `12` (최근 1년)

3. **예상 호출 수**: 250개 지역 × 12개월 = 3,000개 API 호출
4. **예상 소요 시간**: (3,000 ÷ 20) × 1초 = 150초 (약 2.5분)

## 최적화 팁

### 1. API 제한 대응
- 공공데이터 API는 보통 일일/시간당 호출 제한이 있음
- `batchSize`와 `delaySeconds`를 조절하여 제한에 맞춤
- 여러 시간대에 분산하여 실행

### 2. 스케줄링 전략
```bash
# 아파트: 매일 새벽 2시
"0 0 2 * * ?"

# 오피스텔: 매일 새벽 2시 30분 
"0 30 2 * * ?"

# 연립다세대: 매일 새벽 3시
"0 0 3 * * ?"
```

### 3. 지역별 분할 전략
- 수도권/지방 분리 실행
- 광역시/시군구 분리 실행
- 인구 밀도에 따른 우선순위 설정

## 에러 처리

### 1. API 호출 실패
- 개별 API 호출 실패 시 로그 기록
- 다음 파라미터 조합 계속 처리
- 전체 Job 실행 결과에 성공/실패 건수 기록

### 2. 재시도 전략
- 일시적 네트워크 오류 시 재시도
- 서비스 키 오류, 파라미터 오류 등은 재시도 안함

### 3. 모니터링
- Job 실행 상태 실시간 모니터링
- 실패율이 높을 경우 알림
- API 응답 시간 모니터링

## 확장 방안

### 1. 새로운 공공데이터 API 추가
- 기상청 날씨 데이터
- 통계청 인구 데이터
- 환경부 대기질 데이터

### 2. 데이터 저장 및 가공
- 수집된 데이터의 DB 저장
- 데이터 정제 및 변환
- 분석용 데이터 마트 구축

### 3. 실시간 모니터링 대시보드
- Job 실행 현황 시각화
- API 호출 성공률 모니터링
- 데이터 수집량 추이 분석 