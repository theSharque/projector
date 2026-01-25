import { useParams, useNavigate } from 'react-router-dom';
import { Form, Input, Button, Card, Space, InputNumber, Select, message } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { featureApi } from '@/api';
import type { Feature } from '@/types/api.types';
import { QUARTERS } from '@/utils/constants';
import UserSelector from '@/components/relations/UserSelector';
import FunctionalAreaSelector from '@/components/relations/FunctionalAreaSelector';
import Loading from '@/components/common/Loading';

const { TextArea } = Input;

const FeatureFormPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const isEdit = id && id !== 'new';

  const { data: feature, isLoading } = useQuery({
    queryKey: ['feature', id],
    queryFn: () => featureApi.getById(Number(id)),
    enabled: !!isEdit,
  });

  const createMutation = useMutation({
    mutationFn: featureApi.create,
    onSuccess: () => {
      message.success('Feature created successfully');
      queryClient.invalidateQueries({ queryKey: ['features'] });
      navigate('/features');
    },
    onError: () => {
      message.error('Failed to create feature');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, feature }: { id: number; feature: Feature }) => featureApi.update(id, feature),
    onSuccess: () => {
      message.success('Feature updated successfully');
      queryClient.invalidateQueries({ queryKey: ['features'] });
      navigate('/features');
    },
    onError: () => {
      message.error('Failed to update feature');
    },
  });

  const onFinish = (values: Feature) => {
    if (isEdit) {
      updateMutation.mutate({ id: Number(id), feature: values });
    } else {
      createMutation.mutate(values);
    }
  };

  if (isEdit && isLoading) {
    return <Loading />;
  }

  if (isEdit && feature) {
    form.setFieldsValue(feature);
  }

  return (
    <Card title={isEdit ? 'Edit Feature' : 'Create Feature'}>
      <Form form={form} onFinish={onFinish} layout="vertical">
        <Form.Item
          label="Year"
          name="year"
          rules={[{ required: true, message: 'Please input year!' }]}
        >
          <InputNumber min={2000} max={2500} style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item
          label="Quarter"
          name="quarter"
          rules={[{ required: true, message: 'Please select quarter!' }]}
        >
          <Select>
            {QUARTERS.map((q) => (
              <Select.Option key={q} value={q}>
                {q}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          label="Author"
          name="authorId"
          rules={[{ required: true, message: 'Please select author!' }]}
        >
          <UserSelector />
        </Form.Item>

        <Form.Item
          label="Functional Areas"
          name="functionalAreaIds"
          rules={[
            { required: true, message: 'Please select at least one functional area!' },
            {
              validator: (_, value) => {
                if (!value || value.length === 0) {
                  return Promise.reject(new Error('At least one functional area is required'));
                }
                return Promise.resolve();
              },
            },
          ]}
        >
          <FunctionalAreaSelector />
        </Form.Item>

        <Form.Item label="Sprint" name="sprint">
          <InputNumber min={1} style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item label="Release" name="release">
          <Input />
        </Form.Item>

        <Form.Item label="Summary" name="summary">
          <Input />
        </Form.Item>

        <Form.Item label="Description" name="description">
          <TextArea rows={4} />
        </Form.Item>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={createMutation.isPending || updateMutation.isPending}>
              {isEdit ? 'Update' : 'Create'}
            </Button>
            <Button onClick={() => navigate('/features')}>Cancel</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default FeatureFormPage;

