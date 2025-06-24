import React from 'react'
import { Typography } from 'antd'

const { Title } = Typography

const LogList: React.FC = () => {
  return (
    <div>
      <Title level={2}>실행 로그</Title>
      <p>실행 로그가 여기에 표시됩니다.</p>
    </div>
  )
}

export default LogList 