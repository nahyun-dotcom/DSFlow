import React from 'react'
import { Routes, Route } from 'react-router-dom'
import { Layout } from 'antd'
import AppHeader from './components/layout/AppHeader'
import AppSider from './components/layout/AppSider'
import Dashboard from './pages/Dashboard'
import JobList from './pages/JobList'
import JobForm from './pages/JobForm'
import LogList from './pages/LogList'
import CodeSync from './pages/CodeSync'
import Statistics from "./pages/Statistics";
import NotFound from './pages/NotFound'

const { Content } = Layout

function App() {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <AppHeader />
      <Layout>
        <AppSider />
        <Layout style={{ padding: '24px' }}>
          <Content
            style={{
              padding: 24,
              margin: 0,
              minHeight: 280,
              background: '#fff',
              borderRadius: 8,
            }}
          >
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/jobs" element={<JobList />} />
              <Route path="/jobs/new" element={<JobForm />} />
              <Route path="/jobs/:jobCode/edit" element={<JobForm />} />
              <Route path="/logs" element={<LogList />} />
              <Route path="/code-sync" element={<CodeSync />} />
                <Route path="/statistics" element={<Statistics />} />
              <Route path="*" element={<NotFound />} />
            </Routes>
          </Content>
        </Layout>
      </Layout>
    </Layout>
  )
}

export default App 