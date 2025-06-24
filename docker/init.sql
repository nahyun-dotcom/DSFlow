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
    updated_by VARCHAR(100)
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

COMMIT; 