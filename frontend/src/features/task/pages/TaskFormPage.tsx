import { taskApi } from '@/api';
import Loading from '@/components/common/Loading';
import FeatureSelector from '@/components/relations/FeatureSelector';
import RoadmapSelector from '@/components/relations/RoadmapSelector';
import UserSelector from '@/components/relations/UserSelector';
import type { Task } from '@/types/api.types';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Button, Card, Form, Input, Space, message } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';

const { TextArea } = Input;

const TaskFormPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const isEdit = id && id !== 'new';

  const { data: task, isLoading } = useQuery({
    queryKey: ['task', id],
    queryFn: () => taskApi.getById(Number(id!)),
    enabled: !!isEdit,
  });

  const createMutation = useMutation({
    mutationFn: taskApi.create,
    onSuccess: () => {
      message.success('Task created successfully');
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      navigate('/tasks');
    },
    onError: () => {
      message.error('Failed to create task');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, task }: { id: number; task: Task }) => taskApi.update(id, task),
    onSuccess: () => {
      message.success('Task updated successfully');
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      navigate('/tasks');
    },
    onError: () => {
      message.error('Failed to update task');
    },
  });

  const onFinish = (values: Task) => {
    if (isEdit) {
      updateMutation.mutate({ id: Number(id), task: values });
    } else {
      createMutation.mutate(values);
    }
  };

  if (isEdit && isLoading) {
    return <Loading />;
  }

  if (isEdit && task) {
    form.setFieldsValue(task);
  }

  return (
    <Card title={isEdit ? 'Edit Task' : 'Create Task'}>
      <Form form={form} onFinish={onFinish} layout="vertical">
        <Form.Item
          label="Feature"
          name="featureId"
          rules={[{ required: true, message: 'Please select feature!' }]}
        >
          <FeatureSelector />
        </Form.Item>

        <Form.Item
          label="Roadmap"
          name="roadmapId"
          rules={[{ required: true, message: 'Please select roadmap!' }]}
        >
          <RoadmapSelector />
        </Form.Item>

        <Form.Item
          label="Author"
          name="authorId"
          rules={[{ required: true, message: 'Please select author!' }]}
        >
          <UserSelector />
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
            <Button onClick={() => navigate('/tasks')}>Cancel</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default TaskFormPage;

