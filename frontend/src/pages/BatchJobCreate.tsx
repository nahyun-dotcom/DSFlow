import React, { useState } from 'react';
import { Form, Input, Select, InputNumber, Switch, Button, Card, message, Space, Divider } from 'antd';
import { SaveOutlined, ReloadOutlined } from '@ant-design/icons';

const { Option } = Select;
const { TextArea } = Input;

interface BatchJobCreateProps {}

const BatchJobCreate: React.FC<BatchJobCreateProps> = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [parameterType, setParameterType] = useState<string>('SINGLE');

  const handleSubmit = async (values: any) => {
    setLoading(true);
    try {
      // API 호출 로직
      console.log('Job 생성 요청:', values);
      message.success('배치 작업이 성공적으로 생성되었습니다.');
      form.resetFields();
    } catch (error) {
      message.error('배치 작업 생성에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    form.resetFields();
  };

  const getParameterTypeDescription = (type: string) => {
    switch (type) {
      case 'SINGLE':
        return '단일 파라미터로 API를 1회 호출합니다.';
      case 'MULTI_REGION':
        return '활성화된 모든 지역코드에 대해 API를 호출합니다.';
      case 'MULTI_DATE':
        return '지정된 월 범위에 대해 날짜별로 API를 호출합니다.';
      case 'MATRIX':
        return '지역코드 × 날짜 조합으로 매트릭스 형태의 API 호출을 수행합니다.';
      default:
        return '';
    }
  };

  const sampleUrls = {
    apartment: 'http://openapi.molit.go.kr/OpenAPI_ToolInstallPackage/service/rest/RTMSOBJSvc/getRTMSDataSvcAptTradeDev',
    officetel: 'http://openapi.molit.go.kr/OpenAPI_ToolInstallPackage/service/rest/RTMSOBJSvc/getRTMSDataSvcOffiTrade',
    house: 'http://openapi.molit.go.kr/OpenAPI_ToolInstallPackage/service/rest/RTMSOBJSvc/getRTMSDataSvcSHTrade'
  };

  const sampleParameters = {
    realestate: '{"serviceKey":"YOUR_SERVICE_KEY","numOfRows":"1000","pageNo":"1"}',
    weather: '{"serviceKey":"YOUR_SERVICE_KEY","base_date":"20231201","base_time":"0500","nx":"55","ny":"127"}',
    traffic: '{"serviceKey":"YOUR_SERVICE_KEY","type":"xml","numOfRows":"10","pageNo":"1"}'
  };

  return (
    <div style={{ padding: '24px' }}>
      <Card title="공공데이터 API 배치 작업 생성">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            methodType: 'API_GET',
            resourceWeight: 1,
            parameterType: 'SINGLE',
            batchSize: 1,
            delaySeconds: 0,
            useRegionCodes: false,
            dateRangeMonths: 1,
            status: 'ACTIVE'
          }}
        >
          {/* 기본 정보 */}
          <Card size="small" title="기본 정보" style={{ marginBottom: 16 }}>
            <Form.Item
              label="Job 코드"
              name="jobCode"
              rules={[
                { required: true, message: 'Job 코드를 입력해주세요.' },
                { pattern: /^[A-Z0-9_]+$/, message: '대문자, 숫자, 언더스코어만 사용 가능합니다.' }
              ]}
            >
              <Input placeholder="예: REAL_ESTATE_APARTMENT_TRADE" />
            </Form.Item>

            <Form.Item
              label="Job 명"
              name="jobName"
              rules={[{ required: true, message: 'Job 명을 입력해주세요.' }]}
            >
              <Input placeholder="예: 아파트 실거래가 데이터 수집" />
            </Form.Item>

            <Form.Item label="설명" name="description">
              <TextArea 
                rows={3} 
                placeholder="Job에 대한 상세 설명을 입력해주세요."
              />
            </Form.Item>
          </Card>

          {/* API 설정 */}
          <Card size="small" title="API 설정" style={{ marginBottom: 16 }}>
            <Form.Item
              label="HTTP 메소드"
              name="methodType"
              rules={[{ required: true, message: 'HTTP 메소드를 선택해주세요.' }]}
            >
              <Select>
                <Option value="API_GET">GET</Option>
                <Option value="API_POST">POST</Option>
              </Select>
            </Form.Item>

            <Form.Item
              label="API URL"
              name="resourceUrl"
              rules={[{ required: true, message: 'API URL을 입력해주세요.' }]}
            >
              <Input.Group compact>
                <Input
                  style={{ width: 'calc(100% - 200px)' }}
                  placeholder="API 엔드포인트 URL을 입력해주세요."
                />
                <Select 
                  style={{ width: 200 }} 
                  placeholder="샘플 선택"
                  onChange={(value) => form.setFieldsValue({ resourceUrl: value })}
                >
                  <Option value={sampleUrls.apartment}>아파트 실거래가</Option>
                  <Option value={sampleUrls.officetel}>오피스텔 실거래가</Option>
                  <Option value={sampleUrls.house}>단독/연립 실거래가</Option>
                </Select>
              </Input.Group>
            </Form.Item>

            <Form.Item
              label="기본 파라미터 (JSON)"
              name="parameters"
              rules={[{ required: true, message: '기본 파라미터를 입력해주세요.' }]}
            >
              <Input.Group compact>
                <TextArea
                  style={{ width: 'calc(100% - 200px)' }}
                  rows={4}
                  placeholder='{"serviceKey":"YOUR_API_KEY","numOfRows":"1000"}'
                />
                <Select 
                  style={{ width: 200 }} 
                  placeholder="샘플 선택"
                  onChange={(value) => form.setFieldsValue({ parameters: value })}
                >
                  <Option value={sampleParameters.realestate}>부동산 API</Option>
                  <Option value={sampleParameters.weather}>기상청 API</Option>
                  <Option value={sampleParameters.traffic}>교통정보 API</Option>
                </Select>
              </Input.Group>
            </Form.Item>
          </Card>

          {/* 다중 파라미터 설정 */}
          <Card size="small" title="다중 파라미터 설정" style={{ marginBottom: 16 }}>
            <Form.Item
              label="파라미터 타입"
              name="parameterType"
              rules={[{ required: true, message: '파라미터 타입을 선택해주세요.' }]}
            >
              <Select onChange={setParameterType}>
                <Option value="SINGLE">단일 파라미터</Option>
                <Option value="MULTI_REGION">다중 지역코드</Option>
                <Option value="MULTI_DATE">다중 날짜</Option>
                <Option value="MATRIX">지역코드 × 날짜 매트릭스</Option>
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
                  label="배치 크기"
                  name="batchSize"
                  rules={[{ required: true, message: '배치 크기를 입력해주세요.' }]}
                >
                  <InputNumber
                    min={1}
                    max={100}
                    style={{ width: '100%' }}
                    placeholder="한 번에 처리할 파라미터 조합 수"
                  />
                </Form.Item>

                <Form.Item
                  label="API 호출 간 지연 시간 (초)"
                  name="delaySeconds"
                  rules={[{ required: true, message: '지연 시간을 입력해주세요.' }]}
                >
                  <InputNumber
                    min={0}
                    max={60}
                    style={{ width: '100%' }}
                    placeholder="API 제한 대응을 위한 지연 시간"
                  />
                </Form.Item>
              </>
            )}

            {(parameterType === 'MULTI_REGION' || parameterType === 'MATRIX') && (
              <Form.Item label="지역코드 사용" name="useRegionCodes" valuePropName="checked">
                <Switch />
              </Form.Item>
            )}

            {(parameterType === 'MULTI_DATE' || parameterType === 'MATRIX') && (
              <Form.Item
                label="처리할 월 범위"
                name="dateRangeMonths"
                rules={[{ required: true, message: '월 범위를 입력해주세요.' }]}
              >
                <InputNumber
                  min={1}
                  max={24}
                  style={{ width: '100%' }}
                  placeholder="현재 기준 과거 몇 개월까지 처리할지 설정"
                />
              </Form.Item>
            )}
          </Card>

          {/* 스케줄 및 기타 설정 */}
          <Card size="small" title="스케줄 및 기타 설정" style={{ marginBottom: 16 }}>
            <Form.Item
              label="Cron 표현식"
              name="cronExpression"
              rules={[{ required: true, message: 'Cron 표현식을 입력해주세요.' }]}
            >
              <Input.Group compact>
                <Input
                  style={{ width: 'calc(100% - 200px)' }}
                  placeholder="예: 0 0 2 * * ? (매일 새벽 2시)"
                />
                <Select 
                  style={{ width: 200 }} 
                  placeholder="샘플 선택"
                  onChange={(value) => form.setFieldsValue({ cronExpression: value })}
                >
                  <Option value="0 0 2 * * ?">매일 새벽 2시</Option>
                  <Option value="0 30 2 * * ?">매일 새벽 2시 30분</Option>
                  <Option value="0 0 3 * * ?">매일 새벽 3시</Option>
                  <Option value="0 0 2 * * MON">매주 월요일 새벽 2시</Option>
                </Select>
              </Input.Group>
            </Form.Item>

            <Form.Item
              label="리소스 가중치"
              name="resourceWeight"
              rules={[{ required: true, message: '리소스 가중치를 입력해주세요.' }]}
            >
              <InputNumber
                min={1}
                max={10}
                style={{ width: '100%' }}
                placeholder="Job 실행 우선순위 (1~10)"
              />
            </Form.Item>

            <Form.Item label="상태" name="status">
              <Select>
                <Option value="ACTIVE">활성화</Option>
                <Option value="INACTIVE">비활성화</Option>
              </Select>
            </Form.Item>
          </Card>

          <Divider />

          <Form.Item>
            <Space>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                icon={<SaveOutlined />}
                size="large"
              >
                배치 작업 생성
              </Button>
              <Button 
                htmlType="button" 
                onClick={handleReset}
                icon={<ReloadOutlined />}
                size="large"
              >
                초기화
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default BatchJobCreate; 