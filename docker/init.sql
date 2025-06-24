-- DSFlow Database Initialization Script

-- Create database if not exists (this is automatically done by POSTGRES_DB env var)
-- CREATE DATABASE IF NOT EXISTS dsflow;

-- Create user if not exists (this is automatically done by POSTGRES_USER/POSTGRES_PASSWORD env vars)
-- CREATE USER IF NOT EXISTS dsflow WITH PASSWORD 'password';

-- Grant privileges
-- GRANT ALL PRIVILEGES ON DATABASE dsflow TO dsflow;

-- Switch to dsflow database
\c dsflow;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create job_definitions table
CREATE TABLE IF NOT EXISTS job_definitions (
    id BIGSERIAL PRIMARY KEY,
    job_code VARCHAR(100) UNIQUE NOT NULL,
    job_name VARCHAR(200) NOT NULL,
    description TEXT,
    method_type VARCHAR(10) NOT NULL CHECK (method_type IN ('GET', 'POST', 'PUT', 'DELETE')),
    resource_url VARCHAR(500) NOT NULL,
    parameters TEXT,
    cron_expression VARCHAR(100) NOT NULL,
    resource_weight INTEGER DEFAULT 1 CHECK (resource_weight BETWEEN 1 AND 10),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    parameter_type VARCHAR(20) NOT NULL DEFAULT 'SINGLE',
    batch_size INT NOT NULL DEFAULT 1,
    delay_seconds INT NOT NULL DEFAULT 0
);

-- Create job_execution_logs table
CREATE TABLE IF NOT EXISTS job_execution_logs (
    id BIGSERIAL PRIMARY KEY,
    job_code VARCHAR(100) NOT NULL,
    execution_status VARCHAR(20) NOT NULL CHECK (execution_status IN ('SUCCESS', 'FAILURE', 'RUNNING', 'CANCELLED')),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    execution_time_ms BIGINT,
    response_status INTEGER,
    response_body TEXT,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_job_definitions_job_code ON job_definitions(job_code);
CREATE INDEX IF NOT EXISTS idx_job_definitions_status ON job_definitions(status);
CREATE INDEX IF NOT EXISTS idx_job_execution_logs_job_code ON job_execution_logs(job_code);
CREATE INDEX IF NOT EXISTS idx_job_execution_logs_execution_status ON job_execution_logs(execution_status);
CREATE INDEX IF NOT EXISTS idx_job_execution_logs_start_time ON job_execution_logs(start_time);

-- Insert sample data for testing
INSERT INTO job_definitions (
    job_code, 
    job_name, 
    description, 
    method_type, 
    resource_url, 
    parameters, 
    cron_expression, 
    resource_weight, 
    status, 
    created_by
) VALUES 
(
    'SAMPLE_API_JOB',
    'Sample API Job',
    'This is a sample job for testing purposes',
    'GET',
    'https://jsonplaceholder.typicode.com/posts/1',
    '{}',
    '0 */5 * * * ?',
    1,
    'ACTIVE',
    'system'
),
(
    'HEALTH_CHECK',
    'Health Check Job',
    'Health check endpoint monitoring',
    'GET',
    'https://httpbin.org/status/200',
    '{}',
    '0 */1 * * * ?',
    1,
    'ACTIVE',
    'system'
)
ON CONFLICT (job_code) DO NOTHING;

-- Grant permissions to dsflow user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO dsflow;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO dsflow;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO dsflow;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO dsflow;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO dsflow;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO dsflow;

-- 지역코드 테이블 생성
CREATE TABLE IF NOT EXISTS region_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lawd_cd VARCHAR(5) NOT NULL UNIQUE,
    region_name VARCHAR(100) NOT NULL,
    sido_name VARCHAR(100),
    gugun_name VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- JobDefinition 테이블에서 특정 필드들 제거하고 일반적인 필드들만 유지
ALTER TABLE job_definitions 
DROP COLUMN IF EXISTS use_region_codes,
DROP COLUMN IF EXISTS date_range_months;

-- Job 파라미터 설정 테이블 생성
CREATE TABLE IF NOT EXISTS job_parameter_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_definition_id BIGINT NOT NULL,
    parameter_name VARCHAR(50) NOT NULL,
    value_source_type VARCHAR(20) NOT NULL,
    value_source TEXT,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (job_definition_id) REFERENCES job_definitions(id) ON DELETE CASCADE
);

-- 서울특별시 주요 구 지역코드 샘플 데이터 삽입
INSERT IGNORE INTO region_codes (lawd_cd, region_name, sido_name, gugun_name, is_active) VALUES
('11110', '서울특별시 종로구', '서울특별시', '종로구', TRUE),
('11140', '서울특별시 중구', '서울특별시', '중구', TRUE),
('11170', '서울특별시 용산구', '서울특별시', '용산구', TRUE),
('11200', '서울특별시 성동구', '서울특별시', '성동구', TRUE),
('11215', '서울특별시 광진구', '서울특별시', '광진구', TRUE),
('11230', '서울특별시 동대문구', '서울특별시', '동대문구', TRUE),
('11260', '서울특별시 중랑구', '서울특별시', '중랑구', TRUE),
('11290', '서울특별시 성북구', '서울특별시', '성북구', TRUE),
('11305', '서울특별시 강북구', '서울특별시', '강북구', TRUE),
('11320', '서울특별시 도봉구', '서울특별시', '도봉구', TRUE),
('11350', '서울특별시 노원구', '서울특별시', '노원구', TRUE),
('11380', '서울특별시 은평구', '서울특별시', '은평구', TRUE),
('11410', '서울특별시 서대문구', '서울특별시', '서대문구', TRUE),
('11440', '서울특별시 마포구', '서울특별시', '마포구', TRUE),
('11470', '서울특별시 양천구', '서울특별시', '양천구', TRUE),
('11500', '서울특별시 강서구', '서울특별시', '강서구', TRUE),
('11530', '서울특별시 구로구', '서울특별시', '구로구', TRUE),
('11545', '서울특별시 금천구', '서울특별시', '금천구', TRUE),
('11560', '서울특별시 영등포구', '서울특별시', '영등포구', TRUE),
('11590', '서울특별시 동작구', '서울특별시', '동작구', TRUE),
('11620', '서울특별시 관악구', '서울특별시', '관악구', TRUE),
('11650', '서울특별시 서초구', '서울특별시', '서초구', TRUE),
('11680', '서울특별시 강남구', '서울특별시', '강남구', TRUE),
('11710', '서울특별시 송파구', '서울특별시', '송파구', TRUE),
('11740', '서울특별시 강동구', '서울특별시', '강동구', TRUE),

-- 경기도 주요 시군 지역코드 샘플 데이터
('41110', '경기도 수원시', '경기도', '수원시', TRUE),
('41130', '경기도 성남시', '경기도', '성남시', TRUE),
('41150', '경기도 안양시', '경기도', '안양시', TRUE),
('41170', '경기도 부천시', '경기도', '부천시', TRUE),
('41190', '경기도 광명시', '경기도', '광명시', TRUE),
('41210', '경기도 평택시', '경기도', '평택시', TRUE),
('41220', '경기도 동두천시', '경기도', '동두천시', TRUE),
('41250', '경기도 안산시', '경기도', '안산시', TRUE),
('41270', '경기도 고양시', '경기도', '고양시', TRUE),
('41280', '경기도 과천시', '경기도', '과천시', TRUE),
('41290', '경기도 구리시', '경기도', '구리시', TRUE),
('41310', '경기도 남양주시', '경기도', '남양주시', TRUE),
('41360', '경기도 오산시', '경기도', '오산시', TRUE),
('41370', '경기도 시흥시', '경기도', '시흥시', TRUE),
('41390', '경기도 군포시', '경기도', '군포시', TRUE),
('41410', '경기도 의왕시', '경기도', '의왕시', TRUE),
('41430', '경기도 하남시', '경기도', '하남시', TRUE),
('41450', '경기도 용인시', '경기도', '용인시', TRUE),
('41460', '경기도 파주시', '경기도', '파주시', TRUE),
('41480', '경기도 이천시', '경기도', '이천시', TRUE),
('41500', '경기도 안성시', '경기도', '안성시', TRUE),
('41550', '경기도 김포시', '경기도', '김포시', TRUE),
('41570', '경기도 화성시', '경기도', '화성시', TRUE),
('41590', '경기도 광주시', '경기도', '광주시', TRUE),
('41610', '경기도 양주시', '경기도', '양주시', TRUE),
('41630', '경기도 포천시', '경기도', '포천시', TRUE),
('41650', '경기도 여주시', '경기도', '여주시', TRUE);

-- 부동산 실거래가 데이터 수집을 위한 샘플 Job 정의 추가
INSERT IGNORE INTO job_definitions (
    job_code, 
    job_name, 
    description, 
    method_type, 
    resource_url, 
    parameters, 
    cron_expression, 
    resource_weight, 
    status,
    parameter_type,
    batch_size,
    delay_seconds,
    created_by, 
    updated_by
) VALUES (
    'REAL_ESTATE_APARTMENT_TRADE',
    '아파트 실거래가 데이터 수집',
    '국토교통부 부동산 실거래가 공공 API를 통한 아파트 매매 실거래 정보 수집',
    'API_GET',
    'http://openapi.molit.go.kr/OpenAPI_ToolInstallPackage/service/rest/RTMSOBJSvc/getRTMSDataSvcAptTradeDev',
    '{"serviceKey":"YOUR_SERVICE_KEY","numOfRows":"1000","pageNo":"1"}',
    '0 0 2 * * ?',
    2,
    'ACTIVE',
    'MATRIX',
    10,
    1,
    'system',
    'system'
);

-- 부동산 아파트 Job에 대한 파라미터 설정 추가
INSERT IGNORE INTO job_parameter_configs (
    job_definition_id,
    parameter_name,
    value_source_type,
    value_source,
    description,
    is_active,
    sort_order
) VALUES 
-- 지역코드 파라미터
((SELECT id FROM job_definitions WHERE job_code = 'REAL_ESTATE_APARTMENT_TRADE'),
 'LAWD_CD',
 'DB_QUERY',
 'SELECT lawd_cd FROM region_codes WHERE is_active = true ORDER BY lawd_cd',
 '법정동코드 - 활성화된 모든 지역',
 TRUE,
 1),
-- 계약연월 파라미터
((SELECT id FROM job_definitions WHERE job_code = 'REAL_ESTATE_APARTMENT_TRADE'),
 'DEAL_YMD',
 'DATE_RANGE',
 '{"startDate":"2024-01-01","endDate":"2024-03-01","format":"yyyyMM","interval":"MONTH"}',
 '계약연월 - 최근 3개월',
 TRUE,
 2);

-- 업종별 통계 데이터 수집을 위한 샘플 Job 정의 추가
INSERT IGNORE INTO job_definitions (
    job_code, 
    job_name, 
    description, 
    method_type, 
    resource_url, 
    parameters, 
    cron_expression, 
    resource_weight, 
    status,
    parameter_type,
    batch_size,
    delay_seconds,
    created_by, 
    updated_by
) VALUES (
    'BUSINESS_STATISTICS_BY_INDUSTRY',
    '업종별 통계 데이터 수집',
    '통계청 업종별 사업체 통계 데이터 수집',
    'API_GET',
    'http://kosis.kr/openapi/statisticsData.do',
    '{"serviceKey":"YOUR_SERVICE_KEY","method":"getList","format":"json"}',
    '0 0 3 * * ?',
    1,
    'ACTIVE',
    'MULTI_PARAM',
    5,
    2,
    'system',
    'system'
);

-- 업종별 통계 Job에 대한 파라미터 설정 추가
INSERT IGNORE INTO job_parameter_configs (
    job_definition_id,
    parameter_name,
    value_source_type,
    value_source,
    description,
    is_active,
    sort_order
) VALUES 
-- 업종코드 파라미터 (정적 목록)
((SELECT id FROM job_definitions WHERE job_code = 'BUSINESS_STATISTICS_BY_INDUSTRY'),
 'INDUSTRY_CD',
 'STATIC_LIST',
 '["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U"]',
 '한국표준산업분류 대분류 코드',
 TRUE,
 1);

COMMIT; 