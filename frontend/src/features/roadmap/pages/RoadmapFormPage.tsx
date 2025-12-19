import { useParams, useNavigate } from 'react-router-dom';
import { Form, Input, Button, Card, Space, message } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { roadmapApi } from '@/api';
import type { Roadmap } from '@/types/api.types';
import UserSelector from '@/components/relations/UserSelector';
import RoadmapParticipantSelector from '@/components/relations/RoadmapParticipantSelector';
import Loading from '@/components/common/Loading';

const { TextArea } = Input;

const RoadmapFormPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const isEdit = id && id !== 'new';

  const { data: roadmap, isLoading } = useQuery({
    queryKey: ['roadmap', id],
    queryFn: () => roadmapApi.getById(Number(id)),
    enabled: !!isEdit,
  });

  const createMutation = useMutation({
    mutationFn: roadmapApi.create,
    onSuccess: () => {
      message.success('Roadmap created successfully');
      queryClient.invalidateQueries({ queryKey: ['roadmaps'] });
      navigate('/roadmaps');
    },
    onError: () => {
      message.error('Failed to create roadmap');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, roadmap }: { id: number; roadmap: Roadmap }) => roadmapApi.update(id, roadmap),
    onSuccess: () => {
      message.success('Roadmap updated successfully');
      queryClient.invalidateQueries({ queryKey: ['roadmaps'] });
      navigate('/roadmaps');
    },
    onError: () => {
      message.error('Failed to update roadmap');
    },
  });

  const onFinish = (values: Roadmap) => {
    if (isEdit) {
      updateMutation.mutate({ id: Number(id), roadmap: values });
    } else {
      createMutation.mutate(values);
    }
  };

  if (isEdit && isLoading) {
    return <Loading />;
  }

  if (isEdit && roadmap) {
    form.setFieldsValue(roadmap);
  }

  return (
    <Card title={isEdit ? 'Edit Roadmap' : 'Create Roadmap'}>
      <Form form={form} onFinish={onFinish} layout="vertical">
        <Form.Item
          label="Project Name"
          name="projectName"
          rules={[{ required: true, message: 'Please input project name!' }]}
        >
          <Input />
        </Form.Item>

        <Form.Item
          label="Author"
          name="authorId"
          rules={[{ required: true, message: 'Please select author!' }]}
        >
          <UserSelector />
        </Form.Item>

        <Form.Item label="Participants" name="participantIds">
          <RoadmapParticipantSelector />
        </Form.Item>

        <Form.Item label="Mission" name="mission">
          <TextArea rows={3} />
        </Form.Item>

        <Form.Item label="Description" name="description">
          <TextArea rows={4} />
        </Form.Item>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={createMutation.isPending || updateMutation.isPending}>
              {isEdit ? 'Update' : 'Create'}
            </Button>
            <Button onClick={() => navigate('/roadmaps')}>Cancel</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default RoadmapFormPage;

