import React, { useState } from 'react';
import { Form, Input, Select, InputNumber, Button, Card, message, Space, Divider, Table, Modal } from 'antd';
import { SaveOutlined, ReloadOutlined, PlusOutlined, DeleteOutlined, EditOutlined } from '@ant-design/icons';

const { Option } = Select;
const { TextArea } = Input;

interface ParameterConfig {
  id?: number;
  parameterName: string;
  valueSourceType: string;
  valueSource: string;
  description: string;
  isActive: boolean;
  sortOrder: number;
}

interface BatchJobCreateProps {}

const BatchJobCreate: React.FC<BatchJobCreateProps> = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [parameterType, setParameterType] = useState<string>('SINGLE');
  const [parameterConfigs, setParameterConfigs] = useState<ParameterConfig[]>([]);
  const [parameterModalVisible, setParameterModalVisible] = useState(false);
  const [editingParameter, setEditingParameter] = useState<ParameterConfig | null>(null);
  const [parameterForm] = Form.useForm();

  const handleSubmit = async (values: any) => {
    setLoading(true);
    try {
      const jobData = {
        ...values,
        parameterConfigs: parameterConfigs
      };
      console.log('Job 생성 요청:', jobData);
      message.success('배치 작업이 성공적으로 생성되었습니다.');
      form.resetFields();
      setParameterConfigs([]);
    } catch (error) {
      message.error('배치 작업 생성에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    form.resetFields();
    setParameterConfigs([]);
  };

  const getParameterTypeDescription = (type: string) => {
    switch (type) {
      case 'SINGLE':
        return '단일 파라미터로 API를 1회 호출합니다.';
      case 'MULTI_PARAM':
        return '첫 번째 파라미터의 여러 값들에 대해 순차적으로 API를 호출합니다.';
      case 'MATRIX':
        return '모든 파라미터들의 조합(데카르트 곱)으로 API를 호출합니다.';
      default:
        return '';
    }
  };

  const handleAddParameter = () => {
    setEditingParameter(null);
    parameterForm.resetFields();
    setParameterModalVisible(true);
  };

  const handleEditParameter = (parameter: ParameterConfig) => {
    setEditingParameter(parameter);
    parameterForm.setFieldsValue(parameter);
    setParameterModalVisible(true);
  };

  const handleDeleteParameter = (index: number) => {
    const newConfigs = parameterConfigs.filter((_, i) => i !== index);
    setParameterConfigs(newConfigs);
    message.success('파라미터가 삭제되었습니다.');
  };

  const handleParameterModalOk = () => {
    parameterForm.validateFields().then(values => {
      if (editingParameter) {
        // 수정
        const newConfigs = parameterConfigs.map(config => 
          config === editingParameter ? { ...values } : config
        );
        setParameterConfigs(newConfigs);
        message.success('파라미터가 수정되었습니다.');
      } else {
        // 추가
        const newConfig: ParameterConfig = {
          ...values,
          sortOrder: parameterConfigs.length + 1
        };
        setParameterConfigs([...parameterConfigs, newConfig]);
        message.success('파라미터가 추가되었습니다.');
      }
      setParameterModalVisible(false);
      parameterForm.resetFields();
    });
  };

  const parameterColumns = [
    {
      title: '순서',
      dataIndex: 'sortOrder',
      key: 'sortOrder',
      width: 60,
    },
    {
      title: '파라미터명',
      dataIndex: 'parameterName',
      key: 'parameterName',
    },
    {
      title: '값 소스 타입',
      dataIndex: 'valueSourceType',
      key: 'valueSourceType',
      render: (type: string) => {
        const typeLabels: { [key: string]: string } = {
          'DB_QUERY': 'DB 쿼리',
          'STATIC_LIST': '정적 목록',
          'DATE_RANGE': '날짜 범위',
          'API_CALL': 'API 호출',
          'FILE_LIST': '파일 목록'
        };
        return typeLabels[type] || type;
      }
    },
    {
      title: '설명',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '활성화',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (active: boolean) => active ? '✅' : '❌'
    },
    {
      title: '작업',
      key: 'actions',
      width: 120,
      render: (_: any, record: ParameterConfig, index: number) => (
        <Space>
          <Button 
            size="small" 
            icon={<EditOutlined />} 
            onClick={() => handleEditParameter(record)}
          />
          <Button 
            size="small" 
            danger 
            icon={<DeleteOutlined />} 
            onClick={() => handleDeleteParameter(index)}
          />
        </Space>
      ),
    },
  ];

  const sampleValueSources = {
    DB_QUERY: {
      regionCodes: 'SELECT lawd_cd FROM region_codes WHERE is_active = true ORDER BY lawd_cd',
      industries: 'SELECT code FROM industry_codes WHERE level = 1 ORDER BY code'
    },
    STATIC_LIST: {
      months: '["202401", "202402", "202403", "202404", "202405", "202406"]',
      categories: '["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"]'
    },
    DATE_RANGE: {
      monthly: '{"startDate":"2024-01-01","endDate":"2024-06-01","format":"yyyyMM","interval":"MONTH"}',
      daily: '{"startDate":"2024-01-01","endDate":"2024-01-31","format":"yyyyMMdd","interval":"DAY"}'
    },
    API_CALL: {
      sample: '{"url":"https://api.example.com/codes","method":"GET","jsonPath":"data.codes[]"}'
    }
  };

  return (
    <div style={{ padding: '24px' }}>
      <Card title="유연한 다중 파라미터 배치 작업 생성">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            methodType: 'API_GET',
            resourceWeight: 1,
            parameterType: 'SINGLE',
            batchSize: 1,
            delaySeconds: 0,
            status: 'ACTIVE'
          }}
        >
          {/* 기본 정보 */}
          <Card size="small" title="기본 정보" style={{ marginBottom: 16 }}>
            <Form.Item
              label="Job 코드"
              name="jobCode"
              rules={[
                { required: true, message: 'Job 코드를 입력해주세요.' },
                { pattern: /^[A-Z0-9_]+$/, message: '대문자, 숫자, 언더스코어만 사용 가능합니다.' }
              ]}
            >
              <Input placeholder="예: FLEXIBLE_API_JOB" />
            </Form.Item>

            <Form.Item
              label="Job 명"
              name="jobName"
              rules={[{ required: true, message: 'Job 명을 입력해주세요.' }]}
            >
              <Input placeholder="예: 유연한 다중 파라미터 API 데이터 수집" />
            </Form.Item>

            <Form.Item label="설명" name="description">
              <TextArea 
                rows={3} 
                placeholder="Job에 대한 상세 설명을 입력해주세요."
              />
            </Form.Item>
          </Card>

          {/* API 설정 */}
          <Card size="small" title="API 설정" style={{ marginBottom: 16 }}>
            <Form.Item
              label="HTTP 메소드"
              name="methodType"
              rules={[{ required: true, message: 'HTTP 메소드를 선택해주세요.' }]}
            >
              <Select>
                <Option value="API_GET">GET</Option>
                <Option value="API_POST">POST</Option>
              </Select>
            </Form.Item>

            <Form.Item
              label="API URL"
              name="resourceUrl"
              rules={[{ required: true, message: 'API URL을 입력해주세요.' }]}
            >
              <Input placeholder="API 엔드포인트 URL을 입력해주세요." />
            </Form.Item>

            <Form.Item
              label="기본 파라미터 (JSON)"
              name="parameters"
              rules={[{ required: true, message: '기본 파라미터를 입력해주세요.' }]}
            >
              <TextArea
                rows={4}
                placeholder='{"serviceKey":"YOUR_API_KEY","numOfRows":"1000"}'
              />
            </Form.Item>
          </Card>

          {/* 파라미터 타입 설정 */}
          <Card size="small" title="파라미터 타입 설정" style={{ marginBottom: 16 }}>
            <Form.Item
              label="파라미터 타입"
              name="parameterType"
              rules={[{ required: true, message: '파라미터 타입을 선택해주세요.' }]}
            >
              <Select onChange={setParameterType}>
                <Option value="SINGLE">단일 파라미터</Option>
                <Option value="MULTI_PARAM">다중 파라미터 (순차)</Option>
                <Option value="MATRIX">매트릭스 파라미터 (조합)</Option>
              </Select>
            </Form.Item>

            <div style={{ marginBottom: 16, padding: 12, backgroundColor: '#f6f8fa', borderRadius: 6 }}>
              <small style={{ color: '#666' }}>
                {getParameterTypeDescription(parameterType)}
              </small>
            </div>

            {parameterType !== 'SINGLE' && (
              <>
                <Form.Item
                  label="배치 크기"
                  name="batchSize"
                  rules={[{ required: true, message: '배치 크기를 입력해주세요.' }]}
                >
                  <InputNumber
                    min={1}
                    max={100}
                    style={{ width: '100%' }}
                    placeholder="한 번에 처리할 파라미터 조합 수"
                  />
                </Form.Item>

                <Form.Item
                  label="API 호출 간 지연 시간 (초)"
                  name="delaySeconds"
                  rules={[{ required: true, message: '지연 시간을 입력해주세요.' }]}
                >
                  <InputNumber
                    min={0}
                    max={60}
                    style={{ width: '100%' }}
                    placeholder="API 제한 대응을 위한 지연 시간"
                  />
                </Form.Item>
              </>
            )}
          </Card>

          {/* 파라미터 설정 */}
          {parameterType !== 'SINGLE' && (
            <Card size="small" title="파라미터 설정" style={{ marginBottom: 16 }}>
              <div style={{ marginBottom: 16 }}>
                <Button 
                  type="primary" 
                  icon={<PlusOutlined />} 
                  onClick={handleAddParameter}
                >
                  파라미터 추가
                </Button>
              </div>
              
              <Table
                dataSource={parameterConfigs}
                columns={parameterColumns}
                rowKey={(record, index) => index?.toString() || '0'}
                pagination={false}
                size="small"
                locale={{ emptyText: '파라미터를 추가해주세요.' }}
              />
            </Card>
          )}

          {/* 스케줄 및 기타 설정 */}
          <Card size="small" title="스케줄 및 기타 설정" style={{ marginBottom: 16 }}>
            <Form.Item
              label="Cron 표현식"
              name="cronExpression"
              rules={[{ required: true, message: 'Cron 표현식을 입력해주세요.' }]}
            >
              <Input placeholder="예: 0 0 2 * * ? (매일 새벽 2시)" />
            </Form.Item>

            <Form.Item
              label="리소스 가중치"
              name="resourceWeight"
              rules={[{ required: true, message: '리소스 가중치를 입력해주세요.' }]}
            >
              <InputNumber
                min={1}
                max={10}
                style={{ width: '100%' }}
                placeholder="Job 실행 우선순위 (1~10)"
              />
            </Form.Item>

            <Form.Item label="상태" name="status">
              <Select>
                <Option value="ACTIVE">활성화</Option>
                <Option value="INACTIVE">비활성화</Option>
              </Select>
            </Form.Item>
          </Card>

          <Divider />

          <Form.Item>
            <Space>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                icon={<SaveOutlined />}
                size="large"
              >
                배치 작업 생성
              </Button>
              <Button 
                htmlType="button" 
                onClick={handleReset}
                icon={<ReloadOutlined />}
                size="large"
              >
                초기화
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      {/* 파라미터 설정 모달 */}
      <Modal
        title={editingParameter ? "파라미터 수정" : "파라미터 추가"}
        open={parameterModalVisible}
        onOk={handleParameterModalOk}
        onCancel={() => setParameterModalVisible(false)}
        width={600}
      >
        <Form
          form={parameterForm}
          layout="vertical"
          initialValues={{
            isActive: true,
            valueSourceType: 'DB_QUERY'
          }}
        >
          <Form.Item
            label="파라미터명"
            name="parameterName"
            rules={[{ required: true, message: '파라미터명을 입력해주세요.' }]}
          >
            <Input placeholder="예: LAWD_CD, DEAL_YMD, CATEGORY_CD" />
          </Form.Item>

          <Form.Item
            label="값 소스 타입"
            name="valueSourceType"
            rules={[{ required: true, message: '값 소스 타입을 선택해주세요.' }]}
          >
            <Select>
              <Option value="DB_QUERY">DB 쿼리</Option>
              <Option value="STATIC_LIST">정적 목록</Option>
              <Option value="DATE_RANGE">날짜 범위</Option>
              <Option value="API_CALL">API 호출</Option>
              <Option value="FILE_LIST">파일 목록</Option>
            </Select>
          </Form.Item>

          <Form.Item
            label="값 소스"
            name="valueSource"
            rules={[{ required: true, message: '값 소스를 입력해주세요.' }]}
          >
            <TextArea
              rows={4}
              placeholder="값 소스 타입에 따른 설정을 입력해주세요."
            />
          </Form.Item>

          <Form.Item label="설명" name="description">
            <Input placeholder="파라미터에 대한 설명을 입력해주세요." />
          </Form.Item>
        </Form>

        <div style={{ marginTop: 16, fontSize: '12px', color: '#666' }}>
          <strong>샘플 값 소스:</strong>
          <pre style={{ fontSize: '11px', marginTop: 8 }}>
{`DB_QUERY: ${sampleValueSources.DB_QUERY.regionCodes}

STATIC_LIST: ${sampleValueSources.STATIC_LIST.months}

DATE_RANGE: ${sampleValueSources.DATE_RANGE.monthly}

API_CALL: ${sampleValueSources.API_CALL.sample}`}
          </pre>
        </div>
      </Modal>
    </div>
  );
};

export default BatchJobCreate; 