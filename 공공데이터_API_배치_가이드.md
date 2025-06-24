# 유연한 다중 파라미터 배치 작업 시스템 가이드

## 개요

이 가이드는 다양한 종류의 파라미터들을 유연하게 조합하여 API를 호출하는 배치 작업 시스템 사용법을 설명합니다. 지역코드, 날짜뿐만 아니라 업종코드, 카테고리코드, 기관코드 등 어떤 종류의 파라미터든 매트릭스 형태로 조합하여 대량의 API 호출을 자동화할 수 있습니다.

## 시스템 특징

### 1. 유연한 파라미터 조합 지원
- **SINGLE**: 단일 파라미터 작업
- **MULTI_PARAM**: 하나의 파라미터에 대해 여러 값들을 순차 처리
- **MATRIX**: 여러 파라미터들의 조합을 매트릭스 형태로 처리 (데카르트 곱)

### 2. 다양한 값 소스 지원
- **DB_QUERY**: 데이터베이스 쿼리 결과
- **STATIC_LIST**: 정적 값 목록 (JSON 배열)
- **DATE_RANGE**: 날짜 범위 (시작일, 종료일, 간격)
- **API_CALL**: 외부 API 호출 결과
- **FILE_LIST**: 파일에서 읽어온 값 목록

### 3. API 제한 대응
- 배치 크기 조절 (동시 처리 개수 제한)
- API 호출 간 지연 시간 설정
- 시간차 분산 호출

## 사용 방법

### 1. Job 정의 등록

#### 부동산 실거래가 수집 Job 등록 예시
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
  "delaySeconds": 1
}
```

### 2. 파라미터 설정 등록

#### 지역코드 파라미터 설정 (DB 쿼리)
```json
{
  "parameterName": "LAWD_CD",
  "valueSourceType": "DB_QUERY",
  "valueSource": "SELECT lawd_cd FROM region_codes WHERE is_active = true ORDER BY lawd_cd",
  "description": "법정동코드 - 활성화된 모든 지역",
  "sortOrder": 1
}
```

#### 계약연월 파라미터 설정 (날짜 범위)
```json
{
  "parameterName": "DEAL_YMD",
  "valueSourceType": "DATE_RANGE",
  "valueSource": "{\"startDate\":\"2024-01-01\",\"endDate\":\"2024-06-01\",\"format\":\"yyyyMM\",\"interval\":\"MONTH\"}",
  "description": "계약연월 - 최근 6개월",
  "sortOrder": 2
}
```

#### 업종코드 파라미터 설정 (정적 목록)
```json
{
  "parameterName": "INDUSTRY_CD",
  "valueSourceType": "STATIC_LIST",
  "valueSource": "[\"A\", \"B\", \"C\", \"D\", \"E\", \"F\", \"G\", \"H\", \"I\", \"J\"]",
  "description": "한국표준산업분류 대분류 코드",
  "sortOrder": 1
}
```

#### 외부 API 호출로 파라미터 값 가져오기
```json
{
  "parameterName": "CATEGORY_CD",
  "valueSourceType": "API_CALL",
  "valueSource": "{\"url\":\"https://api.example.com/categories\",\"method\":\"GET\",\"jsonPath\":\"data.categories[].code\"}",
  "description": "외부 API에서 가져온 카테고리 코드",
  "sortOrder": 3
}
```

### 3. 파라미터 타입별 동작 방식

#### SINGLE 타입
- 기본 파라미터만 사용하여 1회 API 호출
- 파라미터 설정 불필요

#### MULTI_PARAM 타입
- 첫 번째 파라미터의 여러 값들에 대해 순차적으로 API 호출
- 예: 업종코드 A, B, C, D... 각각에 대해 API 호출

#### MATRIX 타입
- 모든 파라미터들의 데카르트 곱으로 API 호출
- 예: 지역코드(100개) × 계약연월(6개) = 600개 API 호출

### 4. REST API 사용법

#### Job 실행
```bash
curl -X POST "http://localhost:8080/api/jobs/execute" \
  -H "Content-Type: application/json" \
  -d "{\"jobCode\": \"REAL_ESTATE_APARTMENT_TRADE\"}"
```

#### 파라미터 설정 조회
```bash
curl -X GET "http://localhost:8080/api/job-parameter-configs?jobDefinitionId=1"
```

## 실제 사용 시나리오

### 시나리오 1: 전국 부동산 실거래가 수집

**설정**:
- parameterType: `MATRIX`
- 지역코드: DB에서 250개 지역 조회
- 계약연월: 최근 12개월
- batchSize: `20`
- delaySeconds: `1`

**결과**: 250개 지역 × 12개월 = 3,000개 API 호출
**소요시간**: (3,000 ÷ 20) × 1초 = 150초

### 시나리오 2: 업종별 사업체 통계 수집

**설정**:
- parameterType: `MULTI_PARAM`
- 업종코드: 정적 목록 21개 (A~U)
- batchSize: `5`
- delaySeconds: `2`

**결과**: 21개 업종코드에 대해 순차 API 호출
**소요시간**: (21 ÷ 5) × 2초 = 8.4초

### 시나리오 3: 다차원 통계 데이터 수집

**설정**:
- parameterType: `MATRIX`
- 지역코드: 17개 시도
- 업종코드: 21개 대분류
- 기간코드: 4개 분기
- batchSize: `10`
- delaySeconds: `1`

**결과**: 17 × 21 × 4 = 1,428개 API 호출
**소요시간**: (1,428 ÷ 10) × 1초 = 142.8초

## 값 소스 타입별 상세 설정

### DB_QUERY
```sql
-- 활성화된 지역코드 조회
SELECT lawd_cd FROM region_codes WHERE is_active = true ORDER BY lawd_cd

-- 특정 레벨의 업종코드 조회
SELECT code FROM industry_codes WHERE level = 1 AND is_active = true

-- 최근 등록된 기관코드 조회
SELECT org_code FROM organization_codes WHERE created_at >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
```

### STATIC_LIST
```json
// 한국표준산업분류 대분류
["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U"]

// 월별 코드
["202401", "202402", "202403", "202404", "202405", "202406"]

// 사업체 규모 코드
["1", "2", "3", "4", "5"]
```

### DATE_RANGE
```json
// 월별 범위 (yyyyMM 형식)
{
  "startDate": "2024-01-01",
  "endDate": "2024-12-01", 
  "format": "yyyyMM",
  "interval": "MONTH"
}

// 일별 범위 (yyyyMMdd 형식)
{
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "format": "yyyyMMdd", 
  "interval": "DAY"
}

// 분기별 범위 (yyyy0Q 형식)
{
  "startDate": "2024-01-01",
  "endDate": "2024-12-01",
  "format": "yyyy'Q'Q",
  "interval": "QUARTER"
}
```

### API_CALL
```json
// 외부 API에서 코드 목록 가져오기
{
  "url": "https://api.example.com/codes",
  "method": "GET",
  "jsonPath": "data.codes[]"
}

// 인증이 필요한 API
{
  "url": "https://api.example.com/categories",
  "method": "POST",
  "headers": {
    "Authorization": "Bearer YOUR_TOKEN"
  },
  "jsonPath": "result.categories[].id"
}
```

## 최적화 팁

### 1. 배치 크기 조절
- API 제한이 엄격한 경우: batchSize `1-5`
- 일반적인 공공데이터 API: batchSize `10-20`
- 내부 API나 제한이 없는 경우: batchSize `50-100`

### 2. 지연 시간 설정
- 공공데이터 API: `1-2초`
- 상용 API 서비스: `0.5-1초`
- 내부 API: `0-0.5초`

### 3. 스케줄링 전략
```bash
# 대용량 데이터 수집: 새벽 시간대
"0 0 2 * * ?" # 매일 새벽 2시

# 중간 규모: 이른 아침
"0 0 6 * * ?" # 매일 아침 6시

# 소규모: 주기적 수집
"0 */30 * * * ?" # 30분마다
```

### 4. 매트릭스 조합 최적화
- 파라미터 순서를 변경 빈도 순으로 정렬
- 가장 자주 변하는 파라미터를 마지막에 배치
- 총 조합 수가 너무 많으면 파라미터를 분할하여 여러 Job으로 나눔

## 에러 처리 및 모니터링

### 1. 값 소스 오류
- DB_QUERY: 쿼리 문법 오류, 권한 부족
- API_CALL: 네트워크 오류, 인증 실패
- STATIC_LIST: JSON 형식 오류

### 2. API 호출 오류
- 개별 파라미터 조합 실패 시 다음 조합 계속 처리
- 전체 Job 실행 결과에 성공/실패 건수 기록
- 실패율이 임계치를 초과하면 Job 중단

### 3. 성능 모니터링
- 파라미터 조합 생성 시간
- API 호출 응답 시간
- 배치 처리 처리량 (TPS)

이 시스템을 통해 어떤 종류의 공공데이터나 API든 유연하게 다중 파라미터를 조합하여 대량의 데이터를 효율적으로 수집할 수 있습니다. 