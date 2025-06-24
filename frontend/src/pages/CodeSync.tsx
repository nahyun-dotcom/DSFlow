import React, { useState, useEffect } from 'react';
import { 
  Card, 
  Button, 
  Table, 
  Space, 
  Tag, 
  message, 
  Modal, 
  Row, 
  Col,
  Tooltip,
  Spin,
  Form,
  Input,
  Select,
  Switch,
  InputNumber,
  Divider
} from 'antd';
import { 
  SyncOutlined, 
  CheckCircleOutlined, 
  ExclamationCircleOutlined,
  CloudSyncOutlined,
  DatabaseOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  ApiOutlined
} from '@ant-design/icons';
import { api } from '../api';

const { TextArea } = Input;
const { Option } = Select;

interface CodeSyncJob {
  id?: number;
  syncJobCode: string;
  syncJobName: string;
  targetCategoryCode: string;
  apiUrl: string;
  httpMethod: string;
  requestHeaders?: string;
  requestParameters?: string;
  requestBody?: string;
  codeValueJsonPath: string;
  codeNameJsonPath: string;
  metadataJsonPath?: string;
  parentCodeJsonPath?: string;
  cronExpression: string;
  isActive: boolean;
  autoSync: boolean;
  timeoutSeconds: number;
  retryCount: number;
  description?: string;
  lastSyncResult?: string;
  lastSyncTime?: string;
  lastSyncCount?: number;
}

interface CodeCategory {
  id: number;
  categoryCode: string;
  categoryName: string;
  description: string;
}

const CodeSync: React.FC = () => {
  const [syncJobs, setSyncJobs] = useState<CodeSyncJob[]>([]);
  const [categories, setCategories] = useState<CodeCategory[]>([]);
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState<Record<string, boolean>>({});
  const [modalVisible, setModalVisible] = useState(false);
  const [editingJob, setEditingJob] = useState<CodeSyncJob | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchSyncJobs();
    fetchCategories();
  }, []);

  const fetchSyncJobs = async () => {
    try {
      setLoading(true);
      const response = await api.get('/user-code-sync');
      setSyncJobs(response.data);
    } catch (error) {
      message.error('동기화 작업 목록 조회 실패');
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await api.get('/codes/categories');
      setCategories(response.data);
    } catch (error) {
      message.error('카테고리 목록 조회 실패');
    }
  };

  const handleCreateJob = () => {
    setEditingJob(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEditJob = (job: CodeSyncJob) => {
    setEditingJob(job);
    form.setFieldsValue(job);
    setModalVisible(true);
  };

  const handleDeleteJob = async (syncJobCode: string) => {
    Modal.confirm({
      title: '동기화 작업 삭제',
      content: '정말로 이 동기화 작업을 삭제하시겠습니까?',
      onOk: async () => {
        try {
          await api.delete(`/user-code-sync/${syncJobCode}`);
          message.success('동기화 작업이 삭제되었습니다.');
          fetchSyncJobs();
        } catch (error) {
          message.error('동기화 작업 삭제 실패');
        }
      }
    });
  };

  const handleExecuteJob = async (syncJobCode: string) => {
    try {
      setSyncing(prev => ({ ...prev, [syncJobCode]: true }));
      await api.post(`/user-code-sync/${syncJobCode}/execute`);
      message.success(`${syncJobCode} 동기화가 시작되었습니다.`);
      
      setTimeout(() => {
        fetchSyncJobs();
      }, 3000);
      
    } catch (error) {
      message.error('동기화 실행 실패');
    } finally {
      setSyncing(prev => ({ ...prev, [syncJobCode]: false }));
    }
  };

  const handleTestApi = async () => {
    try {
      const values = form.getFieldsValue();
      await api.post('/user-code-sync/test-api', values);
      message.success('API 연결 테스트 성공!');
    } catch (error) {
      message.error('API 연결 테스트 실패');
    }
  };

  const handleSaveJob = async (values: any) => {
    try {
      if (editingJob) {
        await api.put(`/user-code-sync/${editingJob.syncJobCode}`, values);
        message.success('동기화 작업이 수정되었습니다.');
      } else {
        await api.post('/user-code-sync', values);
        message.success('동기화 작업이 생성되었습니다.');
      }
      setModalVisible(false);
      fetchSyncJobs();
    } catch (error) {
      message.error('저장 실패');
    }
  };

  const getStatusColor = (result?: string) => {
    if (!result) return 'default';
    if (result.startsWith('SUCCESS')) return 'green';
    if (result.startsWith('FAILED')) return 'red';
    return 'blue';
  };

  const getStatusText = (result?: string) => {
    if (!result) return '미실행';
    if (result.startsWith('SUCCESS')) return '성공';
    if (result.startsWith('FAILED')) return '실패';
    return '실행중';
  };

  const columns = [
    {
      title: '작업명',
      dataIndex: 'syncJobName',
      key: 'syncJobName',
      render: (text: string, record: CodeSyncJob) => (
        <div>
          <strong>{text}</strong>
          <div style={{ fontSize: '12px', color: '#666' }}>{record.syncJobCode}</div>
        </div>
      )
    },
    {
      title: '대상 카테고리',
      dataIndex: 'targetCategoryCode',
      key: 'targetCategoryCode',
      render: (categoryCode: string) => {
        const category = categories.find(c => c.categoryCode === categoryCode);
        return (
          <div>
            <strong>{category?.categoryName || categoryCode}</strong>
            <div style={{ fontSize: '12px', color: '#666' }}>{categoryCode}</div>
          </div>
        );
      }
    },
    {
      title: 'API URL',
      dataIndex: 'apiUrl',
      key: 'apiUrl',
      ellipsis: true,
      render: (url: string, record: CodeSyncJob) => (
        <Tooltip title={url}>
          <div>
            <Tag color="blue">{record.httpMethod}</Tag>
            {url.length > 50 ? `${url.substring(0, 50)}...` : url}
          </div>
        </Tooltip>
      )
    },
    {
      title: '자동 동기화',
      dataIndex: 'autoSync',
      key: 'autoSync',
      render: (autoSync: boolean, record: CodeSyncJob) => (
        <div>
          <Tag color={autoSync ? 'green' : 'default'}>
            {autoSync ? 'ON' : 'OFF'}
          </Tag>
          {autoSync && (
            <div style={{ fontSize: '12px', color: '#666' }}>{record.cronExpression}</div>
          )}
        </div>
      )
    },
    {
      title: '마지막 실행',
      key: 'lastSync',
      render: (record: CodeSyncJob) => (
        <div>
          <Tag color={getStatusColor(record.lastSyncResult)}>
            {getStatusText(record.lastSyncResult)}
          </Tag>
          {record.lastSyncTime && (
            <div style={{ fontSize: '12px', color: '#666' }}>
              {new Date(record.lastSyncTime).toLocaleString()}
            </div>
          )}
          {record.lastSyncCount && (
            <div style={{ fontSize: '12px', color: '#666' }}>
              {record.lastSyncCount}개 동기화
            </div>
          )}
        </div>
      )
    },
    {
      title: '작업',
      key: 'actions',
      render: (record: CodeSyncJob) => (
        <Space>
          <Tooltip title="실행">
            <Button
              type="primary"
              icon={<PlayCircleOutlined />}
              size="small"
              loading={syncing[record.syncJobCode]}
              onClick={() => handleExecuteJob(record.syncJobCode)}
            />
          </Tooltip>
          <Tooltip title="수정">
            <Button
              icon={<EditOutlined />}
              size="small"
              onClick={() => handleEditJob(record)}
            />
          </Tooltip>
          <Tooltip title="삭제">
            <Button
              danger
              icon={<DeleteOutlined />}
              size="small"
              onClick={() => handleDeleteJob(record.syncJobCode)}
            />
          </Tooltip>
        </Space>
      )
    }
  ];

  return (
    <div style={{ padding: '24px' }}>
      <Card
        title={
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <CloudSyncOutlined />
            <span>사용자 정의 코드 동기화 관리</span>
          </div>
        }
        extra={
          <Space>
            <Button 
              icon={<SyncOutlined />} 
              onClick={fetchSyncJobs}
              loading={loading}
            >
              새로고침
            </Button>
            <Button 
              type="primary" 
              icon={<PlusOutlined />}
              onClick={handleCreateJob}
            >
              API 등록
            </Button>
          </Space>
        }
      >
        {loading ? (
          <div style={{ textAlign: 'center', padding: '50px' }}>
            <Spin size="large" />
          </div>
        ) : (
          <>
            {/* 요약 정보 */}
            <Row gutter={16} style={{ marginBottom: '24px' }}>
              <Col span={8}>
                <Card size="small">
                  <div style={{ textAlign: 'center' }}>
                    <DatabaseOutlined style={{ fontSize: '24px', color: '#1890ff' }} />
                    <div style={{ marginTop: '8px' }}>
                      <div style={{ fontSize: '20px', fontWeight: 'bold' }}>{syncJobs.length}</div>
                      <div style={{ color: '#666' }}>등록된 API</div>
                    </div>
                  </div>
                </Card>
              </Col>
              <Col span={8}>
                <Card size="small">
                  <div style={{ textAlign: 'center' }}>
                    <CheckCircleOutlined style={{ fontSize: '24px', color: '#52c41a' }} />
                    <div style={{ marginTop: '8px' }}>
                      <div style={{ fontSize: '20px', fontWeight: 'bold' }}>
                        {syncJobs.filter(job => job.autoSync).length}
                      </div>
                      <div style={{ color: '#666' }}>자동 동기화</div>
                    </div>
                  </div>
                </Card>
              </Col>
              <Col span={8}>
                <Card size="small">
                  <div style={{ textAlign: 'center' }}>
                    <ApiOutlined style={{ fontSize: '24px', color: '#722ed1' }} />
                    <div style={{ marginTop: '8px' }}>
                      <div style={{ fontSize: '20px', fontWeight: 'bold' }}>
                        {categories.length}
                      </div>
                      <div style={{ color: '#666' }}>코드 카테고리</div>
                    </div>
                  </div>
                </Card>
              </Col>
            </Row>

            {/* 동기화 작업 테이블 */}
            <Table
              columns={columns}
              dataSource={syncJobs}
              rowKey="syncJobCode"
              pagination={{ pageSize: 10 }}
              size="middle"
            />

            {/* 도움말 정보 */}
            <Card 
              size="small" 
              title="사용 가이드" 
              style={{ marginTop: '24px' }}
            >
              <Row gutter={16}>
                <Col span={12}>
                  <h4>🚀 API 등록 방법</h4>
                  <ul style={{ margin: 0, paddingLeft: '20px' }}>
                    <li>원하는 외부 API의 URL과 메소드 설정</li>
                    <li>JSON 응답에서 코드값과 코드명이 있는 경로 지정</li>
                    <li>자동 동기화 주기 설정 (Cron 표현식)</li>
                    <li>API 테스트 후 저장</li>
                  </ul>
                </Col>
                <Col span={12}>
                  <h4>📊 지원하는 API 형태</h4>
                  <ul style={{ margin: 0, paddingLeft: '20px' }}>
                    <li>공공데이터포털 API</li>
                    <li>일반적인 REST API (JSON 응답)</li>
                    <li>인증 헤더가 필요한 API</li>
                    <li>POST 방식의 API도 지원</li>
                  </ul>
                </Col>
              </Row>
            </Card>
          </>
        )}
      </Card>

      {/* API 등록/수정 모달 */}
      <Modal
        title={editingJob ? 'API 동기화 설정 수정' : '새로운 API 등록'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        width={800}
        footer={null}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSaveJob}
          initialValues={{
            httpMethod: 'GET',
            timeoutSeconds: 30,
            retryCount: 3,
            isActive: true,
            autoSync: true,
            cronExpression: '0 0 2 * * ?'
          }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item 
                name="syncJobCode" 
                label="동기화 작업 코드" 
                rules={[{ required: true, message: '작업 코드를 입력하세요!' }]}
              >
                <Input placeholder="예: CUSTOM_REGION_SYNC" disabled={!!editingJob} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item 
                name="syncJobName" 
                label="작업명" 
                rules={[{ required: true, message: '작업명을 입력하세요!' }]}
              >
                <Input placeholder="예: 커스텀 지역코드 동기화" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item 
            name="targetCategoryCode" 
            label="대상 코드 카테고리" 
            rules={[{ required: true, message: '카테고리를 선택하세요!' }]}
          >
            <Select placeholder="동기화할 코드 카테고리 선택">
              {categories.map(category => (
                <Option key={category.categoryCode} value={category.categoryCode}>
                  {category.categoryName} ({category.categoryCode})
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Divider>API 설정</Divider>

          <Row gutter={16}>
            <Col span={4}>
              <Form.Item 
                name="httpMethod" 
                label="HTTP 메소드"
                rules={[{ required: true }]}
              >
                <Select>
                  <Option value="GET">GET</Option>
                  <Option value="POST">POST</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={20}>
              <Form.Item 
                name="apiUrl" 
                label="API URL" 
                rules={[{ required: true, message: 'API URL을 입력하세요!' }]}
              >
                <Input placeholder="https://api.example.com/codes" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="requestHeaders" label="요청 헤더 (JSON)">
                <TextArea 
                  placeholder='{"Authorization": "Bearer YOUR_TOKEN"}'
                  rows={3}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="requestParameters" label="요청 파라미터 (JSON)">
                <TextArea 
                  placeholder='{"format": "json", "limit": "1000"}'
                  rows={3}
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="requestBody" label="요청 본문 (POST용, JSON)">
            <TextArea 
              placeholder='{"query": "getAllCodes"}'
              rows={2}
            />
          </Form.Item>

          <Divider>응답 데이터 매핑</Divider>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item 
                name="codeValueJsonPath" 
                label="코드값 JSON 경로" 
                rules={[{ required: true, message: '코드값 경로를 입력하세요!' }]}
              >
                <Input placeholder="$.code 또는 code" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item 
                name="codeNameJsonPath" 
                label="코드명 JSON 경로" 
                rules={[{ required: true, message: '코드명 경로를 입력하세요!' }]}
              >
                <Input placeholder="$.name 또는 name" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="metadataJsonPath" label="메타데이터 JSON 경로">
                <Input placeholder="$.metadata (선택사항)" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="parentCodeJsonPath" label="부모코드 JSON 경로">
                <Input placeholder="$.parentCode (선택사항)" />
              </Form.Item>
            </Col>
          </Row>

          <Divider>동기화 설정</Divider>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="autoSync" label="자동 동기화" valuePropName="checked">
                <Switch />
              </Form.Item>
            </Col>
            <Col span={16}>
              <Form.Item name="cronExpression" label="동기화 주기 (Cron)">
                <Input placeholder="0 0 2 * * ? (매일 새벽 2시)" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="timeoutSeconds" label="타임아웃(초)">
                <InputNumber min={5} max={300} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="retryCount" label="재시도 횟수">
                <InputNumber min={0} max={10} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="isActive" label="활성화" valuePropName="checked">
                <Switch />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="description" label="설명">
            <TextArea 
              placeholder="동기화 작업에 대한 상세 설명"
              rows={2}
            />
          </Form.Item>

          <div style={{ textAlign: 'right', marginTop: '24px' }}>
            <Space>
              <Button onClick={() => setModalVisible(false)}>
                취소
              </Button>
              <Button onClick={handleTestApi}>
                API 테스트
              </Button>
              <Button type="primary" htmlType="submit">
                {editingJob ? '수정' : '등록'}
              </Button>
            </Space>
          </div>
        </Form>
      </Modal>
    </div>
  );
};

export default CodeSync; 