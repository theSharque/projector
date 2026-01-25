import { useParams, useNavigate } from 'react-router-dom';
import { Form, Input, Button, Card, Space, message } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { functionalAreaApi } from '@/api';
import type { FunctionalArea } from '@/types/api.types';
import Loading from '@/components/common/Loading';

const { TextArea } = Input;

const FunctionalAreaFormPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const isEdit = id && id !== 'new';

  const { data: functionalArea, isLoading } = useQuery({
    queryKey: ['functional-area', id],
    queryFn: () => functionalAreaApi.getById(Number(id)),
    enabled: !!isEdit,
  });

  const createMutation = useMutation({
    mutationFn: functionalAreaApi.create,
    onSuccess: () => {
      message.success('Functional area created successfully');
      queryClient.invalidateQueries({ queryKey: ['functional-areas'] });
      navigate('/functional-areas');
    },
    onError: () => {
      message.error('Failed to create functional area');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Omit<FunctionalArea, 'id' | 'createDate' | 'updateDate'> }) =>
      functionalAreaApi.update(id, data),
    onSuccess: () => {
      message.success('Functional area updated successfully');
      queryClient.invalidateQueries({ queryKey: ['functional-areas'] });
      navigate('/functional-areas');
    },
    onError: () => {
      message.error('Failed to update functional area');
    },
  });

  const onFinish = (values: Omit<FunctionalArea, 'id' | 'createDate' | 'updateDate'>) => {
    if (isEdit) {
      updateMutation.mutate({ id: Number(id), data: values });
    } else {
      createMutation.mutate(values);
    }
  };

  if (isEdit && isLoading) {
    return <Loading />;
  }

  if (isEdit && functionalArea) {
    form.setFieldsValue(functionalArea);
  }

  return (
    <Card title={isEdit ? 'Edit Functional Area' : 'Create Functional Area'}>
      <Form form={form} onFinish={onFinish} layout="vertical">
        <Form.Item
          label="Name"
          name="name"
          rules={[{ required: true, message: 'Please input functional area name!' }]}
        >
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
            <Button onClick={() => navigate('/functional-areas')}>Cancel</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default FunctionalAreaFormPage;
