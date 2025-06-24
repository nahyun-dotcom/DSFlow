-- DSFlow 재설계된 데이터베이스 스키마
-- 시니어 DBA 설계: 완전 유연한 코드 관리 시스템

-- 기존 테이블 유지
-- job_definitions, job_execution_logs, job_parameter_configs

-- 1. 코드 카테고리 테이블 (기존 region_codes 대체)
CREATE TABLE IF NOT EXISTS code_categories (
    id BIGSERIAL PRIMARY KEY,
    category_code VARCHAR(50) UNIQUE NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 2. 코드 값 테이블 (계층구조 지원)
CREATE TABLE IF NOT EXISTS code_values (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    code_value VARCHAR(100) NOT NULL,
    code_name VARCHAR(200) NOT NULL,
    parent_code_value VARCHAR(100), -- 계층구조 지원
    metadata JSONB, -- 추가 메타데이터 (PostgreSQL JSONB)
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    FOREIGN KEY (category_id) REFERENCES code_categories(id) ON DELETE CASCADE,
    UNIQUE(category_id, code_value) -- 카테고리 내에서 코드 값은 유일
);

-- 3. 인덱스 추가 (성능 최적화)
CREATE INDEX IF NOT EXISTS idx_code_categories_category_code ON code_categories(category_code);
CREATE INDEX IF NOT EXISTS idx_code_categories_active ON code_categories(is_active);
CREATE INDEX IF NOT EXISTS idx_code_values_category_id ON code_values(category_id);
CREATE INDEX IF NOT EXISTS idx_code_values_code_value ON code_values(code_value);
CREATE INDEX IF NOT EXISTS idx_code_values_parent_code ON code_values(parent_code_value);
CREATE INDEX IF NOT EXISTS idx_code_values_active ON code_values(is_active);
CREATE INDEX IF NOT EXISTS idx_code_values_metadata ON code_values USING GIN (metadata); -- JSONB 인덱스

-- 4. 코드 카테고리 샘플 데이터
INSERT INTO code_categories (category_code, category_name, description, sort_order, created_by) VALUES
('REGION', '지역코드', '법정동코드 및 행정구역 코드', 1, 'system'),
('INDUSTRY', '업종코드', '한국표준산업분류 코드', 2, 'system'),
('BUSINESS_TYPE', '사업체유형', '기업 규모별 분류 코드', 3, 'system'),
('EDUCATION_LEVEL', '학력코드', '교육 수준 분류 코드', 4, 'system'),
('VEHICLE_TYPE', '차량유형', '자동차 분류 코드', 5, 'system'),
('DATE_FORMAT', '날짜형식', '날짜 포맷 코드', 6, 'system')
ON CONFLICT (category_code) DO NOTHING;

-- 5. 지역코드 샘플 데이터 (기존 region_codes 데이터 마이그레이션)
INSERT INTO code_values (category_id, code_value, code_name, metadata, sort_order, created_by)
SELECT 
    (SELECT id FROM code_categories WHERE category_code = 'REGION'),
    '11110', '서울특별시 종로구', 
    '{"sido":"서울특별시","gugun":"종로구","level":"gu"}', 1, 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM code_values cv 
    JOIN code_categories cc ON cv.category_id = cc.id 
    WHERE cc.category_code = 'REGION' AND cv.code_value = '11110'
);

INSERT INTO code_values (category_id, code_value, code_name, metadata, sort_order, created_by)
SELECT 
    (SELECT id FROM code_categories WHERE category_code = 'REGION'),
    '11140', '서울특별시 중구',
    '{"sido":"서울특별시","gugun":"중구","level":"gu"}', 2, 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM code_values cv 
    JOIN code_categories cc ON cv.category_id = cc.id 
    WHERE cc.category_code = 'REGION' AND cv.code_value = '11140'
);

INSERT INTO code_values (category_id, code_value, code_name, metadata, sort_order, created_by)
SELECT 
    (SELECT id FROM code_categories WHERE category_code = 'REGION'),
    '41110', '경기도 수원시',
    '{"sido":"경기도","gugun":"수원시","level":"si"}', 3, 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM code_values cv 
    JOIN code_categories cc ON cv.category_id = cc.id 
    WHERE cc.category_code = 'REGION' AND cv.code_value = '41110'
);

-- 6. 업종코드 샘플 데이터 (한국표준산업분류)
INSERT INTO code_values (category_id, code_value, code_name, parent_code_value, metadata, sort_order, created_by)
SELECT 
    (SELECT id FROM code_categories WHERE category_code = 'INDUSTRY'),
    'A', '농업, 임업 및 어업', NULL,
    '{"level":1,"description":"농업, 임업 및 어업"}', 1, 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM code_values cv 
    JOIN code_categories cc ON cv.category_id = cc.id 
    WHERE cc.category_code = 'INDUSTRY' AND cv.code_value = 'A'
);

INSERT INTO code_values (category_id, code_value, code_name, parent_code_value, metadata, sort_order, created_by)
SELECT 
    (SELECT id FROM code_categories WHERE category_code = 'INDUSTRY'),
    'A01', '농업', 'A',
    '{"level":2,"parent":"A"}', 1, 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM code_values cv 
    JOIN code_categories cc ON cv.category_id = cc.id 
    WHERE cc.category_code = 'INDUSTRY' AND cv.code_value = 'A01'
);

INSERT INTO code_values (category_id, code_value, code_name, parent_code_value, metadata, sort_order, created_by)
SELECT 
    (SELECT id FROM code_categories WHERE category_code = 'INDUSTRY'),
    'C', '제조업', NULL,
    '{"level":1,"description":"제조업"}', 3, 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM code_values cv 
    JOIN code_categories cc ON cv.category_id = cc.id 
    WHERE cc.category_code = 'INDUSTRY' AND cv.code_value = 'C'
);

-- 7. 사업체 유형 샘플 데이터
INSERT INTO code_values (category_id, code_value, code_name, metadata, sort_order, created_by)
SELECT 
    (SELECT id FROM code_categories WHERE category_code = 'BUSINESS_TYPE'),
    'SMALL', '소기업', 
    '{"employees":"1-50","description":"소규모 기업"}', 1, 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM code_values cv 
    JOIN code_categories cc ON cv.category_id = cc.id 
    WHERE cc.category_code = 'BUSINESS_TYPE' AND cv.code_value = 'SMALL'
);

INSERT INTO code_values (category_id, code_value, code_name, metadata, sort_order, created_by)
SELECT 
    (SELECT id FROM code_categories WHERE category_code = 'BUSINESS_TYPE'),
    'MEDIUM', '중기업',
    '{"employees":"51-300","description":"중간규모 기업"}', 2, 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM code_values cv 
    JOIN code_categories cc ON cv.category_id = cc.id 
    WHERE cc.category_code = 'BUSINESS_TYPE' AND cv.code_value = 'MEDIUM'
);

-- 8. 유연한 쿼리 예시들 (주석으로 제공)
/*
-- 지역코드 조회 (기존 region_codes 대체)
SELECT cv.code_value, cv.code_name 
FROM code_values cv 
JOIN code_categories cc ON cv.category_id = cc.id 
WHERE cc.category_code = 'REGION' AND cv.is_active = true 
ORDER BY cv.sort_order;

-- 업종코드 대분류만 조회
SELECT cv.code_value, cv.code_name 
FROM code_values cv 
JOIN code_categories cc ON cv.category_id = cc.id 
WHERE cc.category_code = 'INDUSTRY' 
  AND cv.parent_code_value IS NULL 
  AND cv.is_active = true;

-- 특정 업종의 하위 분류 조회
SELECT cv.code_value, cv.code_name 
FROM code_values cv 
JOIN code_categories cc ON cv.category_id = cc.id 
WHERE cc.category_code = 'INDUSTRY' 
  AND cv.parent_code_value = 'A' 
  AND cv.is_active = true;

-- JSON 메타데이터 활용 쿼리
SELECT cv.code_value, cv.code_name 
FROM code_values cv 
JOIN code_categories cc ON cv.category_id = cc.id 
WHERE cc.category_code = 'REGION' 
  AND cv.metadata->>'sido' = '서울특별시'
  AND cv.is_active = true;
*/

-- 9. 기존 특정 도메인 테이블들 삭제
DROP TABLE IF EXISTS region_codes;

COMMIT; 