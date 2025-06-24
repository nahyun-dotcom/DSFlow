import React from 'react'
import { Result, Button } from 'antd'
import { useNavigate } from 'react-router-dom'

const NotFound: React.FC = () => {
  const navigate = useNavigate()

  return (
    <Result
      status="404"
      title="404"
      subTitle="페이지를 찾을 수 없습니다."
      extra={
        <Button type="primary" onClick={() => navigate('/')}>
          홈으로 이동
        </Button>
      }
    />
  )
}

export default NotFound 