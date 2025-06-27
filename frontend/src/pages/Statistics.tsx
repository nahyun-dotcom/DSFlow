import React, { useState, useEffect } from 'react';
import { Table, DatePicker, Card, Statistic, Row, Col } from 'antd';
import {getJobStatistics, getJobStatisticsByPeriod, JobStatisticsDto} from '../api';
import { Dayjs } from 'dayjs';

const Statistics: React.FC = () => {
    const [statistics, setStatistics] = useState<JobStatisticsDto[]>([]);
    const [loading, setLoading] = useState(false);

    // TODO: 컴포넌트 마운트 시 데이터 로드
    useEffect(() => {
        setLoading(true);
        getJobStatistics()
            .then((data)=>setStatistics(data))
            .catch((error)=>console.error("데이터 로드 실패:", error))
            .finally(()=>setLoading(false));
    }, []);

    // TODO: 기간 필터 핸들러

    const handleDateRangeChange = (
        dates: [Dayjs, Dayjs] | null,
        _dateStrings: [string, string]
    ) => {
        if (!dates) return;

        const [startDate, endDate] = dates;

        setLoading(true);

        getJobStatisticsByPeriod(
            startDate.toISOString(),
            endDate.toISOString()
        )
            .then((data) => setStatistics(data))
            .catch((error) => console.error('통계 조회 실패:', error))
            .finally(() => setLoading(false));
    };

    // TODO: 테이블 컬럼 정의
    const columns = [
        {
            title: 'Job 코드',
            dataIndex: 'jobCode',
            key: 'jobCode',
        },
        {
            title: 'Job 이름',
            dataIndex: 'jobName',
            key: 'jobName',
        },
        {
            title: '총 실행 횟수',
            dataIndex: 'totalExecutions',
            key: 'totalExecutions',
        },
        {
            title: '성공률',
            dataIndex: 'successRate',
            key: 'successRate',
            render: (rate: number) => `${rate.toFixed(1)}%`,
        },
        // TODO: 나머지 컬럼들 추가
        {
            title: '성공 실행 횟수',
            dataIndex: 'successfulExecutions',
            key: 'successfulExecutions',
        },
        {
            title: '실패 실행 횟수',
            dataIndex: 'failedExecutions',
            key: 'failedExecutions'
        },
        {
            title: '마지막 실행 시간',
            dataIndex: 'lastExecutionTime',
            key: 'lastExecutionTime',
        }
    ];

    return (
        <div>
            <h2>Job 실행 통계</h2>

            {/* TODO: 요약 통계 카드들 */}
            <Row gutter={16} style={{ marginBottom: 16 }}>
                <Col span={6}>
                    <Card>
                        <Statistic title="전체 Job 수" value={statistics.length} />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic
                            title="전체 실행 횟수"
                            value={statistics.reduce((sum, job) => sum + job.totalExecutions, 0)}
                        />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic
                            title="전체 성공 횟수"
                            value={statistics.reduce((sum, job) => sum + job.successfulExecutions, 0)}
                        />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic
                            title="전체 실패 횟수"
                            value={statistics.reduce((sum, job) => sum + job.failedExecutions, 0)}
                        />
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic
                            title="평균 성공률"
                            value={
                                statistics.length > 0
                                    ? (
                                        statistics.reduce((sum, job) => sum + job.successRate, 0) /
                                        statistics.length
                                    ).toFixed(1)
                                    : 0
                            }
                            suffix="%"
                        />
                    </Card>
                </Col>
            </Row>


            {/* TODO: 기간 선택 필터 */}
            <DatePicker.RangePicker
                onChange={handleDateRangeChange}
                style={{ marginBottom: 16 }}
            />

            {/* TODO: 통계 테이블 */}
            <Table
                columns={columns}
                dataSource={statistics}
                loading={loading}
                rowKey="jobCode"
            />
        </div>
    );
};

export default Statistics;
