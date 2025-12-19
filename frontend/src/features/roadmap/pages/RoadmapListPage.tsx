import { Table, Button, Space, Tag, Popconfirm, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { roadmapApi, userApi } from '@/api';
import type { Roadmap, User } from '@/types/api.types';
import Loading from '@/components/common/Loading';

const RoadmapListPage = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: roadmaps = [], isLoading: roadmapsLoading } = useQuery({
    queryKey: ['roadmaps'],
    queryFn: roadmapApi.getAll,
  });

  const { data: users = [] } = useQuery({
    queryKey: ['users'],
    queryFn: userApi.getAll,
  });

  const deleteMutation = useMutation({
    mutationFn: roadmapApi.delete,
    onSuccess: () => {
      message.success('Roadmap deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['roadmaps'] });
    },
    onError: () => {
      message.error('Failed to delete roadmap');
    },
  });

  const userMap = new Map(users.map((user: User) => [user.id, user.email]));

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: 'Project Name',
      dataIndex: 'projectName',
      key: 'projectName',
    },
    {
      title: 'Author',
      key: 'author',
      render: (_: unknown, record: Roadmap) => userMap.get(record.authorId) || `User ${record.authorId}`,
    },
    {
      title: 'Participants',
      key: 'participants',
      render: (_: unknown, record: Roadmap) => {
        const count = record.participantIds?.length || 0;
        return count > 0 ? <Tag>{count} participant(s)</Tag> : <span>-</span>;
      },
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 150,
      render: (_: unknown, record: Roadmap) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => navigate(`/roadmaps/${record.id}/edit`)}
          >
            Edit
          </Button>
          <Popconfirm
            title="Are you sure you want to delete this roadmap?"
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

  if (roadmapsLoading) {
    return <Loading />;
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Roadmaps</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/roadmaps/new')}>
          Create Roadmap
        </Button>
      </div>
      <Table columns={columns} dataSource={roadmaps} rowKey="id" />
    </div>
  );
};

export default RoadmapListPage;

