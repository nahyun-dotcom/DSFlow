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

-- 새로운 유연한 코드 관리 시스템
-- 코드 카테고리 테이블 (지역, 업종, 사업체유형 등 모든 종류의 카테고리)
CREATE TABLE IF NOT EXISTS code_categories (
    id BIGSERIAL PRIMARY KEY,
    category_code VARCHAR(50) NOT NULL UNIQUE,
    category_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 코드 값 테이블 (실제 코드 값들)
CREATE TABLE IF NOT EXISTS code_values (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    code_value VARCHAR(100) NOT NULL,
    code_name VARCHAR(200) NOT NULL,
    parent_code_value VARCHAR(100),
    metadata TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    FOREIGN KEY (category_id) REFERENCES code_categories(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_code_categories_category_code ON code_categories(category_code);
CREATE INDEX IF NOT EXISTS idx_code_categories_is_active ON code_categories(is_active);
CREATE INDEX IF NOT EXISTS idx_code_values_category_id ON code_values(category_id);
CREATE INDEX IF NOT EXISTS idx_code_values_code_value ON code_values(code_value);
CREATE INDEX IF NOT EXISTS idx_code_values_parent_code_value ON code_values(parent_code_value);
CREATE INDEX IF NOT EXISTS idx_code_values_is_active ON code_values(is_active);

-- Job 파라미터 설정 테이블 생성
CREATE TABLE IF NOT EXISTS job_parameter_configs (
    id BIGSERIAL PRIMARY KEY,
    job_definition_id BIGINT NOT NULL,
    parameter_name VARCHAR(50) NOT NULL,
    value_source_type VARCHAR(20) NOT NULL,
    value_source TEXT,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_definition_id) REFERENCES job_definitions(id) ON DELETE CASCADE
);

-- 새로운 코드 시스템 샘플 데이터

-- 1. 코드 카테고리 생성
INSERT INTO code_categories (category_code, category_name, description, is_active, sort_order, created_by) VALUES
('REGION', '지역코드', '법정동코드 및 행정구역 정보', TRUE, 1, 'system'),
('INDUSTRY', '업종코드', '한국표준산업분류 코드', TRUE, 2, 'system'),
('BUSINESS_TYPE', '사업체유형', '개인/법인/정부기관 등 사업체 유형', TRUE, 3, 'system'),
('VEHICLE_TYPE', '차량유형', '승용차/트럭/버스 등 차량 분류', TRUE, 4, 'system')
ON CONFLICT (category_code) DO NOTHING;

-- 2. 지역코드 데이터 (서울시 주요 구)
INSERT INTO code_values (category_id, code_value, code_name, metadata, is_active, sort_order, created_by) VALUES
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '11110', '서울특별시 종로구', '{"sido":"서울특별시","gugun":"종로구","level":"구"}', TRUE, 1, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '11140', '서울특별시 중구', '{"sido":"서울특별시","gugun":"중구","level":"구"}', TRUE, 2, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '11170', '서울특별시 용산구', '{"sido":"서울특별시","gugun":"용산구","level":"구"}', TRUE, 3, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '11200', '서울특별시 성동구', '{"sido":"서울특별시","gugun":"성동구","level":"구"}', TRUE, 4, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '11215', '서울특별시 광진구', '{"sido":"서울특별시","gugun":"광진구","level":"구"}', TRUE, 5, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '11230', '서울특별시 동대문구', '{"sido":"서울특별시","gugun":"동대문구","level":"구"}', TRUE, 6, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '11440', '서울특별시 마포구', '{"sido":"서울특별시","gugun":"마포구","level":"구"}', TRUE, 7, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '11650', '서울특별시 서초구', '{"sido":"서울특별시","gugun":"서초구","level":"구"}', TRUE, 8, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '11680', '서울특별시 강남구', '{"sido":"서울특별시","gugun":"강남구","level":"구"}', TRUE, 9, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '11710', '서울특별시 송파구', '{"sido":"서울특별시","gugun":"송파구","level":"구"}', TRUE, 10, 'system'),

-- 경기도 주요 시
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '41110', '경기도 수원시', '{"sido":"경기도","gugun":"수원시","level":"시"}', TRUE, 11, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '41130', '경기도 성남시', '{"sido":"경기도","gugun":"성남시","level":"시"}', TRUE, 12, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '41150', '경기도 안양시', '{"sido":"경기도","gugun":"안양시","level":"시"}', TRUE, 13, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '41270', '경기도 고양시', '{"sido":"경기도","gugun":"고양시","level":"시"}', TRUE, 14, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'REGION'), '41450', '경기도 용인시', '{"sido":"경기도","gugun":"용인시","level":"시"}', TRUE, 15, 'system');

-- 3. 업종코드 데이터 (한국표준산업분류 대분류)
INSERT INTO code_values (category_id, code_value, code_name, metadata, is_active, sort_order, created_by) VALUES
((SELECT id FROM code_categories WHERE category_code = 'INDUSTRY'), 'A', '농업, 임업 및 어업', '{"level":"대분류","section":"A"}', TRUE, 1, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'INDUSTRY'), 'B', '광업', '{"level":"대분류","section":"B"}', TRUE, 2, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'INDUSTRY'), 'C', '제조업', '{"level":"대분류","section":"C"}', TRUE, 3, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'INDUSTRY'), 'D', '전기, 가스, 증기 및 공기 조절 공급업', '{"level":"대분류","section":"D"}', TRUE, 4, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'INDUSTRY'), 'E', '수도, 하수 및 폐기물 처리, 원료 재생업', '{"level":"대분류","section":"E"}', TRUE, 5, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'INDUSTRY'), 'F', '건설업', '{"level":"대분류","section":"F"}', TRUE, 6, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'INDUSTRY'), 'G', '도매 및 소매업', '{"level":"대분류","section":"G"}', TRUE, 7, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'INDUSTRY'), 'H', '운수 및 창고업', '{"level":"대분류","section":"H"}', TRUE, 8, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'INDUSTRY'), 'I', '숙박 및 음식점업', '{"level":"대분류","section":"I"}', TRUE, 9, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'INDUSTRY'), 'J', '정보통신업', '{"level":"대분류","section":"J"}', TRUE, 10, 'system');

-- 4. 사업체 유형 코드
INSERT INTO code_values (category_id, code_value, code_name, metadata, is_active, sort_order, created_by) VALUES
((SELECT id FROM code_categories WHERE category_code = 'BUSINESS_TYPE'), 'INDIVIDUAL', '개인사업자', '{"type":"individual","tax_type":"소득세"}', TRUE, 1, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'BUSINESS_TYPE'), 'CORPORATION', '법인사업자', '{"type":"corporation","tax_type":"법인세"}', TRUE, 2, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'BUSINESS_TYPE'), 'GOVERNMENT', '정부기관', '{"type":"government","tax_type":"비과세"}', TRUE, 3, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'BUSINESS_TYPE'), 'NON_PROFIT', '비영리단체', '{"type":"non_profit","tax_type":"비과세"}', TRUE, 4, 'system');

-- 5. 차량 유형 코드 (계층구조 예시)
-- 상위 카테고리
INSERT INTO code_values (category_id, code_value, code_name, metadata, is_active, sort_order, created_by) VALUES
((SELECT id FROM code_categories WHERE category_code = 'VEHICLE_TYPE'), 'PASSENGER', '승용차', '{"category":"passenger","seats":"1-9"}', TRUE, 1, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'VEHICLE_TYPE'), 'COMMERCIAL', '상용차', '{"category":"commercial","purpose":"business"}', TRUE, 2, 'system');

-- 하위 카테고리 (parent_code_value 사용)
INSERT INTO code_values (category_id, code_value, code_name, parent_code_value, metadata, is_active, sort_order, created_by) VALUES
((SELECT id FROM code_categories WHERE category_code = 'VEHICLE_TYPE'), 'SEDAN', '승용차-세단', 'PASSENGER', '{"type":"sedan","doors":"4"}', TRUE, 1, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'VEHICLE_TYPE'), 'SUV', '승용차-SUV', 'PASSENGER', '{"type":"suv","height":"high"}', TRUE, 2, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'VEHICLE_TYPE'), 'TRUCK', '상용차-트럭', 'COMMERCIAL', '{"type":"truck","cargo":"yes"}', TRUE, 1, 'system'),
((SELECT id FROM code_categories WHERE category_code = 'VEHICLE_TYPE'), 'BUS', '상용차-버스', 'COMMERCIAL', '{"type":"bus","passengers":"many"}', TRUE, 2, 'system');

-- 부동산 실거래가 데이터 수집을 위한 샘플 Job 정의 추가
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
)
ON CONFLICT (job_code) DO NOTHING;

-- 부동산 아파트 Job에 대한 파라미터 설정 추가
INSERT INTO job_parameter_configs (
    job_definition_id,
    parameter_name,
    value_source_type,
    value_source,
    description,
    is_active,
    sort_order
) VALUES 
-- 지역코드 파라미터 (새로운 코드 시스템 사용)
((SELECT id FROM job_definitions WHERE job_code = 'REAL_ESTATE_APARTMENT_TRADE'),
 'LAWD_CD',
 'DB_QUERY',
 'CODE_CATEGORY:REGION',
 '법정동코드 - 새로운 코드 시스템에서 지역 카테고리',
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
)
ON CONFLICT (job_code) DO NOTHING;

-- 업종별 통계 Job에 대한 파라미터 설정 추가
INSERT INTO job_parameter_configs (
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