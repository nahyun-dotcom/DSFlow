import React from 'react'
import { Card, Row, Col, Statistic, Typography } from 'antd'
import { 
  PlayCircleOutlined, 
  PauseCircleOutlined, 
  CheckCircleOutlined, 
  CloseCircleOutlined 
} from '@ant-design/icons'

const { Title } = Typography

const Dashboard: React.FC = () => {
  return (
    <div>
      <Title level={2}>대시보드</Title>
      
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="전체 Job"
              value={12}
              prefix={<PlayCircleOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="활성 Job"
              value={8}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="비활성 Job"
              value={3}
              prefix={<PauseCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="실패 Job"
              value={1}
              prefix={<CloseCircleOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24} lg={12}>
          <Card title="최근 실행 로그" size="small">
            <p>최근 실행된 Job들의 로그가 여기에 표시됩니다.</p>
          </Card>
        </Col>
        
        <Col xs={24} lg={12}>
          <Card title="시스템 상태" size="small">
            <p>시스템 상태 정보가 여기에 표시됩니다.</p>
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Dashboard 