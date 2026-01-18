import { featureApi, roadmapApi, taskApi, userApi } from '@/api';
import Loading from '@/components/common/Loading';
import type { Feature, Roadmap, Task, User } from '@/types/api.types';
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Button, Popconfirm, Select, Space, Table, message } from 'antd';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const TaskListPage = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [featureFilter, setFeatureFilter] = useState<number | undefined>();

  const { data: tasks = [], isLoading: tasksLoading } = useQuery({
    queryKey: ['tasks'],
    queryFn: taskApi.getAll,
  });

  const { data: features = [] } = useQuery({
    queryKey: ['features'],
    queryFn: featureApi.getAll,
  });

  const { data: users = [] } = useQuery({
    queryKey: ['users'],
    queryFn: userApi.getAll,
  });

  const { data: roadmaps = [] } = useQuery({
    queryKey: ['roadmaps'],
    queryFn: roadmapApi.getAll,
  });

  const deleteMutation = useMutation({
    mutationFn: taskApi.delete,
    onSuccess: () => {
      message.success('Task deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
    },
    onError: () => {
      message.error('Failed to delete task');
    },
  });

  const featureMap = new Map(features.map((feature: Feature) => [feature.id, feature]));
  const userMap = new Map(users.map((user: User) => [user.id, user.email]));
  const roadmapMap = new Map(roadmaps.map((roadmap: Roadmap) => [roadmap.id, roadmap]));

  const filteredTasks = tasks.filter((task: Task) => {
    if (featureFilter && task.featureId !== featureFilter) return false;
    return true;
  });

  const formatFeatureLabel = (feature: Feature) => {
    return `${feature.year} ${feature.quarter}${feature.summary ? ` - ${feature.summary}` : ''}`;
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: 'Feature',
      key: 'feature',
      render: (_: unknown, record: Task) => {
        const feature = featureMap.get(record.featureId);
        return feature ? formatFeatureLabel(feature) : `Feature ${record.featureId}`;
      },
    },
    {
      title: 'Roadmap',
      key: 'roadmap',
      render: (_: unknown, record: Task) => {
        const roadmap = roadmapMap.get(record.roadmapId);
        return roadmap ? roadmap.projectName : `Roadmap ${record.roadmapId}`;
      },
    },
    {
      title: 'Summary',
      dataIndex: 'summary',
      key: 'summary',
    },
    {
      title: 'Author',
      key: 'author',
      render: (_: unknown, record: Task) => userMap.get(record.authorId) || `User ${record.authorId}`,
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 150,
      render: (_: unknown, record: Task) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => navigate(`/tasks/${record.id}/edit`)}
          >
            Edit
          </Button>
          <Popconfirm
            title="Are you sure you want to delete this task?"
            onConfirm={() => deleteMutation.mutate(record.id!)}
            okText="Yes"
            cancelText="No"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              Delete
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  if (tasksLoading) {
    return <Loading />;
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Tasks</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/tasks/new')}>
          Create Task
        </Button>
      </div>
      <div style={{ marginBottom: 16 }}>
        <Select
          placeholder="Filter by Feature"
          allowClear
          style={{ width: 300 }}
          value={featureFilter}
          onChange={setFeatureFilter}
          showSearch
          optionFilterProp="label"
          options={features.map((feature: Feature) => ({
            value: feature.id,
            label: formatFeatureLabel(feature),
          }))}
        />
      </div>
      <Table columns={columns} dataSource={filteredTasks} rowKey="id" />
    </div>
  );
};

export default TaskListPage;

