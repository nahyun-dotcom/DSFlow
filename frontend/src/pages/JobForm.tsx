import React, { useState } from 'react'
import { Typography, Form, Input, Button, Select, message, TimePicker, Radio, Card, Table, Modal, Space, Divider } from 'antd'
import { createJob } from '../api'
import { useNavigate } from 'react-router-dom'
import { PlusOutlined, DeleteOutlined, EditOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'

const { Title } = Typography
const { Option } = Select
const { TextArea } = Input

interface ParameterConfig {
  id?: number;
  parameterName: string;
  valueSourceType: string;
  valueSource: string;
  description: string;
  isActive: boolean;
  sortOrder: number;
}

const initialValues = {
  jobCode: '',
  jobName: '',
  description: '',
  methodType: 'API_GET',
  resourceUrl: 'https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade',
  parameters: '{"serviceKey": "${APT_API_KEY}", "numOfRows": "1000"}',
  scheduleType: 'daily',
  executeTime: dayjs('02:00', 'HH:mm'),
  dayOfWeek: 1,
  dayOfMonth: 1,
  resourceWeight: 1,
  status: 'ACTIVE',
  parameterType: 'SINGLE',
  batchSize: 1,
  delaySeconds: 0,
}

// 스케줄 타입에 따라 cron 표현식 생성
const generateCronExpression = (scheduleType: string, executeTime: any, dayOfWeek?: number, dayOfMonth?: number) => {
  const hour = executeTime.hour()
  const minute = executeTime.minute()
  
  switch (scheduleType) {
    case 'daily':
      return `0 ${minute} ${hour} * * ?`
    case 'weekly':
      return `0 ${minute} ${hour} ? * ${dayOfWeek}`
    case 'monthly':
      return `0 ${minute} ${hour} ${dayOfMonth} * ?`
    default:
      return `0 ${minute} ${hour} * * ?`
  }
}

const JobForm: React.FC = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [scheduleType, setScheduleType] = useState('daily')
  const [parameterType, setParameterType] = useState<string>('SINGLE')
  const [parameterConfigs, setParameterConfigs] = useState<ParameterConfig[]>([])
  const [parameterModalVisible, setParameterModalVisible] = useState(false)
  const [editingParameter, setEditingParameter] = useState<ParameterConfig | null>(null)
  const [parameterForm] = Form.useForm()
  const navigate = useNavigate()

  const onFinish = async (values: any) => {
    setLoading(true)
    try {
      // cron 표현식 자동 생성
      const cronExpression = generateCronExpression(
        values.scheduleType,
        values.executeTime,
        values.dayOfWeek,
        values.dayOfMonth
      )
      
      // cronExpression을 추가하고 스케줄 관련 필드는 제거
      const { scheduleType: _, executeTime: __, dayOfWeek: ___, dayOfMonth: ____, ...jobData } = values
      const finalData = { 
        ...jobData, 
        cronExpression,
        parameterConfigs: parameterConfigs 
      }
      
      console.log('🚀 전송할 데이터:', finalData)
      const response = await createJob(finalData)
      console.log('성공 응답:', response)
      message.success('Job이 성공적으로 등록되었습니다.')
      navigate('/jobs')
    } catch (e: any) {
      console.error('❌ 전체 에러 객체:', e)
      const status = e?.response?.status
      const data = e?.response?.data
      
      if (e?.response) {
        console.error(`🔴 서버 응답 에러 [${status}]:`, data)
        message.error(`서버 에러 [${status}]: ${data?.message || '알 수 없는 에러'}`)
      } else if (e?.request) {
        console.error('🔴 네트워크 에러 - 응답 없음:', e.request)
        message.error('네트워크 에러: 서버와 연결할 수 없습니다.')
      } else {
        console.error('🔴 기타 에러:', e.message)
        message.error(`에러: ${e.message || '알 수 없는 에러가 발생했습니다.'}`)
      }
    } finally {
      setLoading(false)
    }
  }

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
        const newConfigs = parameterConfigs.map(config => 
          config === editingParameter ? { ...values } : config
        );
        setParameterConfigs(newConfigs);
        message.success('파라미터가 수정되었습니다.');
      } else {
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

  const renderScheduleOptions = () => {
    return (
      <>
        <Form.Item name="scheduleType" label="실행 주기" rules={[{ required: true }]}>
          <Radio.Group onChange={(e) => setScheduleType(e.target.value)}>
            <Radio value="daily">매일</Radio>
            <Radio value="weekly">매주</Radio>
            <Radio value="monthly">매월</Radio>
          </Radio.Group>
        </Form.Item>
        
        <Form.Item name="executeTime" label="실행 시간" rules={[{ required: true, message: '실행 시간을 선택해주세요' }]}>
          <TimePicker format="HH:mm" placeholder="시간 선택" style={{ width: '100%' }} />
        </Form.Item>
        
        {scheduleType === 'weekly' && (
          <Form.Item name="dayOfWeek" label="요일" rules={[{ required: true }]}>
            <Select placeholder="요일 선택">
              <Option value={1}>월요일</Option>
              <Option value={2}>화요일</Option>
              <Option value={3}>수요일</Option>
              <Option value={4}>목요일</Option>
              <Option value={5}>금요일</Option>
              <Option value={6}>토요일</Option>
              <Option value={7}>일요일</Option>
            </Select>
          </Form.Item>
        )}
        
        {scheduleType === 'monthly' && (
          <Form.Item name="dayOfMonth" label="날짜" rules={[{ required: true }]}>
            <Select placeholder="날짜 선택">
              {Array.from({ length: 31 }, (_, i) => (
                <Option key={i + 1} value={i + 1}>{i + 1}일</Option>
              ))}
            </Select>
          </Form.Item>
        )}
      </>
    )
  }

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <Title level={2}>Job 등록</Title>
      <Form
        form={form}
        layout="vertical"
        initialValues={initialValues}
        onFinish={onFinish}
      >
        {/* 기본 정보 */}
        <Card size="small" title="기본 정보" style={{ marginBottom: 16 }}>
          <Form.Item name="jobCode" label="Job 코드" rules={[{ required: true, message: '필수 입력' }, { pattern: /^[A-Z0-9_]+$/, message: '대문자, 숫자, 언더스코어만 입력하세요' }]}> 
            <Input placeholder="예: FLEXIBLE_API_JOB" />
          </Form.Item>
          <Form.Item name="jobName" label="Job 명" rules={[{ required: true, message: '필수 입력' }]}> 
            <Input placeholder="예: 유연한 다중 파라미터 API 데이터 수집" />
          </Form.Item>
          <Form.Item name="description" label="설명"> 
            <TextArea rows={2} placeholder="Job에 대한 상세 설명을 입력해주세요." />
          </Form.Item>
        </Card>

        {/* API 설정 */}
        <Card size="small" title="API 설정" style={{ marginBottom: 16 }}>
          <Form.Item name="methodType" label="메소드 타입" rules={[{ required: true }]}> 
            <Select>
              <Option value="API_GET">API GET</Option>
              <Option value="API_POST">API POST</Option>
              <Option value="FILE_DOWNLOAD">파일 다운로드</Option>
              <Option value="FILE_PROCESS">파일 처리</Option>
            </Select>
          </Form.Item>
          <Form.Item name="resourceUrl" label="리소스 URL" rules={[{ required: true, message: '필수 입력' }]}> 
            <Input placeholder="API 엔드포인트 URL을 입력해주세요." />
          </Form.Item>
          <Form.Item name="parameters" label="기본 파라미터 (JSON)" rules={[{ required: false }]}> 
            <TextArea rows={3} placeholder='{"serviceKey":"YOUR_API_KEY","numOfRows":"1000"}' />
          </Form.Item>
        </Card>

        {/* 파라미터 타입 설정 */}
        <Card size="small" title="파라미터 타입 설정" style={{ marginBottom: 16 }}>
          <Form.Item
            name="parameterType" 
            label="파라미터 타입"
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
                name="batchSize" 
                label="배치 크기"
                rules={[{ required: true, message: '배치 크기를 입력해주세요.' }]}
              >
                <Input 
                  type="number" 
                  min={1} 
                  max={100} 
                  placeholder="한 번에 처리할 파라미터 조합 수"
                />
              </Form.Item>

              <Form.Item
                name="delaySeconds" 
                label="API 호출 간 지연 시간 (초)"
                rules={[{ required: true, message: '지연 시간을 입력해주세요.' }]}
              >
                <Input 
                  type="number" 
                  min={0} 
                  max={60} 
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

        {/* 스케줄 설정 */}
        <Card size="small" title="스케줄 설정" style={{ marginBottom: 16 }}>
          {renderScheduleOptions()}
        </Card>

        {/* 기타 설정 */}
        <Card size="small" title="기타 설정" style={{ marginBottom: 16 }}>
          <Form.Item name="resourceWeight" label="리소스 가중치" rules={[{ required: true }]}> 
            <Input type="number" min={1} max={10} placeholder="Job 실행 우선순위 (1~10)" />
          </Form.Item>
          <Form.Item name="status" label="상태" rules={[{ required: true }]}> 
            <Select>
              <Option value="ACTIVE">활성</Option>
              <Option value="INACTIVE">비활성</Option>
            </Select>
          </Form.Item>
        </Card>

        <Divider />

        <Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} block size="large">
            Job 등록
          </Button>
        </Form.Item>
      </Form>

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
{`DB_QUERY: SELECT lawd_cd FROM region_codes WHERE is_active = true

STATIC_LIST: ["202401", "202402", "202403", "202404"]

DATE_RANGE: {"startDate":"2024-01-01","endDate":"2024-06-01","format":"yyyyMM","interval":"MONTH"}

API_CALL: {"url":"https://api.example.com/codes","method":"GET","jsonPath":"data.codes[]"}`}
          </pre>
        </div>
      </Modal>
    </div>
  )
}

export default JobForm 