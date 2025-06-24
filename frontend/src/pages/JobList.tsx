import React, { useEffect, useState } from 'react';
import { Button, Table, Typography, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { getJobs } from '../api';

const { Title } = Typography;

const JobList = () => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    getJobs()
      .then(res => setJobs(res.data.content || []))
      .catch(() => message.error('Job 목록 조회 실패'))
      .finally(() => setLoading(false));
  }, []);

  const columns = [
    { title: 'Job 코드', dataIndex: 'jobCode', key: 'jobCode' },
    { title: 'Job 명', dataIndex: 'jobName', key: 'jobName' },
    { title: '설명', dataIndex: 'description', key: 'description' },
    { title: '상태', dataIndex: 'status', key: 'status' },
    { title: '생성일', dataIndex: 'createdAt', key: 'createdAt' },
  ];

  return (
    <div style={{ maxWidth: 900, margin: '0 auto' }}>
      <Title level={2}>Job 목록</Title>
      <Button type="primary" style={{ marginBottom: 16 }} onClick={() => navigate('/jobs/new')}>
        Job 등록
      </Button>
      <Table
        rowKey="jobCode"
        columns={columns}
        dataSource={jobs}
        loading={loading}
        pagination={{ pageSize: 10 }}
      />
    </div>
  );
};

export default JobList; 