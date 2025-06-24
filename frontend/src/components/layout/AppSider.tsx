import React from 'react'
import { Layout, Menu } from 'antd'
import { useNavigate, useLocation } from 'react-router-dom'
import {
  DashboardOutlined,
  SettingOutlined,
  FileTextOutlined,
  CloudSyncOutlined,
  PlayCircleOutlined
} from '@ant-design/icons'

const { Sider } = Layout

const AppSider: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()

  const menuItems = [
    {
      key: '/',
      icon: <DashboardOutlined />,
      label: '대시보드',
    },
    {
      key: '/jobs',
      icon: <SettingOutlined />,
      label: 'Job 관리',
    },
    {
      key: '/logs',
      icon: <FileTextOutlined />,
      label: '실행 로그',
    },
    {
      key: '/code-sync',
      icon: <CloudSyncOutlined />,
      label: '코드 동기화',
    },
  ]

  return (
    <Sider width={200} style={{ background: '#fff' }}>
      <Menu
        mode="inline"
        selectedKeys={[location.pathname]}
        style={{ height: '100%', borderRight: 0 }}
        onClick={({ key }) => navigate(key)}
        items={menuItems}
      />
    </Sider>
  )
}

export default AppSider 