import React from 'react'
import { Layout, Typography } from 'antd'

const { Header } = Layout
const { Title } = Typography

const AppHeader: React.FC = () => {
  return (
    <Header style={{ background: '#001529', padding: '0 24px' }}>
      <Title level={3} style={{ color: 'white', margin: '16px 0' }}>
        DSFlow - 배치 관리 시스템
      </Title>
    </Header>
  )
}

export default AppHeader 