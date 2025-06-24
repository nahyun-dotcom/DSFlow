import React, { useState } from 'react'
import { Typography, Form, Input, Button, Select, message, TimePicker, Radio } from 'antd'
import { createJob } from '../api'
import { useNavigate } from 'react-router-dom'
import dayjs from 'dayjs'

const { Title } = Typography
const { Option } = Select

const initialValues = {
  jobCode: '',
  jobName: '',
  description: '',
  methodType: 'API_GET',
  resourceUrl: 'https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade',
  parameters: '{"serviceKey": "${APT_API_KEY}", "LAWD_CD": "11110", "DEAL_YMD": "202401"}',
  scheduleType: 'daily',
  executeTime: dayjs('02:00', 'HH:mm'),
  dayOfWeek: 1, // 월요일
  dayOfMonth: 1, // 매월 1일
  resourceWeight: 1,
  status: 'ACTIVE',
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
      const finalData = { ...jobData, cronExpression }
      
      console.log('🚀 전송할 데이터:', finalData)
      const response = await createJob(finalData)
      console.log('성공 응답:', response)
      message.success('Job이 성공적으로 등록되었습니다.')
      navigate('/jobs')
    } catch (e: any) {
      console.error('❌ 전체 에러 객체:', e)
      console.error('📡 에러 응답:', e?.response)
      console.error('📤 에러 요청:', e?.request)
      console.error('💬 에러 메시지:', e?.message)
      console.error('⚙️ axios config:', e?.config)
      
      // axios 에러인지 확인
      if (e?.response) {
        // 서버에서 응답을 받았지만 에러 상태
        const status = e.response.status
        const data = e.response.data
        console.error(`🔴 서버 응답 에러 [${status}]:`, data)
        message.error(`서버 에러 [${status}]: ${data?.message || '알 수 없는 에러'}`)
      } else if (e?.request) {
        // 요청은 보냈지만 응답을 받지 못함
        console.error('🔴 네트워크 에러 - 응답 없음:', e.request)
        message.error('네트워크 에러: 서버와 연결할 수 없습니다.')
      } else {
        // 기타 에러
        console.error('🔴 기타 에러:', e.message)
        message.error(`에러: ${e.message || '알 수 없는 에러가 발생했습니다.'}`)
      }
    } finally {
      setLoading(false)
    }
  }

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
    <div style={{ maxWidth: 600, margin: '0 auto' }}>
      <Title level={2}>Job 등록</Title>
      <Form
        form={form}
        layout="vertical"
        initialValues={initialValues}
        onFinish={onFinish}
      >
        <Form.Item name="jobCode" label="Job 코드" rules={[{ required: true, message: '필수 입력' }, { pattern: /^[A-Z0-9_]+$/, message: '대문자, 숫자, 언더스코어만 입력하세요' }]}> 
          <Input />
        </Form.Item>
        <Form.Item name="jobName" label="Job 명" rules={[{ required: true, message: '필수 입력' }]}> 
          <Input />
        </Form.Item>
        <Form.Item name="description" label="설명"> 
          <Input.TextArea rows={2} />
        </Form.Item>
        <Form.Item name="methodType" label="메소드 타입" rules={[{ required: true }]}> 
          <Select>
            <Option value="API_GET">API GET</Option>
            <Option value="API_POST">API POST</Option>
            <Option value="FILE_DOWNLOAD">파일 다운로드</Option>
            <Option value="FILE_PROCESS">파일 처리</Option>
          </Select>
        </Form.Item>
        <Form.Item name="resourceUrl" label="리소스 URL" rules={[{ required: true, message: '필수 입력' }]}> 
          <Input />
        </Form.Item>
        <Form.Item name="parameters" label="파라미터 (JSON)" rules={[{ required: false }]}> 
          <Input.TextArea rows={3} />
        </Form.Item>
        
        {renderScheduleOptions()}
        
        <Form.Item name="resourceWeight" label="리소스 가중치" rules={[{ required: true }]}> 
          <Input type="number" min={1} max={10} />
        </Form.Item>
        <Form.Item name="status" label="상태" rules={[{ required: true }]}> 
          <Select>
            <Option value="ACTIVE">활성</Option>
            <Option value="INACTIVE">비활성</Option>
          </Select>
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} block>등록</Button>
        </Form.Item>
      </Form>
    </div>
  )
}

export default JobForm 