import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// 요청 인터셉터
api.interceptors.request.use((config) => {
  console.log('API 요청:', config.method?.toUpperCase(), config.url, config.data)
  return config
}, (error) => {
  console.error('API 요청 에러:', error)
  return Promise.reject(error)
})

// 응답 인터셉터
api.interceptors.response.use((response) => {
  console.log('API 응답:', response.status, response.config.url, response.data)
  return response
}, (error) => {
  console.error('API 응답 에러:', error)
  console.error('Error status:', error.response?.status)
  console.error('Error data:', error.response?.data)
  console.error('Error config:', error.config)
  return Promise.reject(error)
})

export { api }

export const createJob = (job: any) => api.post('/jobs', job)
export const getJobs = () => api.get('/jobs')
export const getJob = (jobCode: string) => api.get(`/jobs/${jobCode}`)
export const updateJob = (jobCode: string, job: any) => api.put(`/jobs/${jobCode}`, job)
export const deleteJob = (jobCode: string) => api.delete(`/jobs/${jobCode}`)
export interface JobStatisticsDto {
  jobCode: string;
  jobName: string;
  totalExecutions: number;
  successfulExecutions: number;
  failedExecutions: number;
  successRate: number;
  lastExecutionTime: string;
}
// TODO: 통계 조회 API 함수 구현
export const getJobStatistics = async (): Promise<JobStatisticsDto[]> => {
  const response = await api.get<JobStatisticsDto[]>('/statistics/jobs');
  return response.data;
};

export const getJobStatisticsByPeriod = async (
    start: string,
    end: string
): Promise<JobStatisticsDto[]> => {
  const response = await api.get<JobStatisticsDto[]>('/statistics/jobs/period', {
    params: { start, end },
  });
  return response.data;
};