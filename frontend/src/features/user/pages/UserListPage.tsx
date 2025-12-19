import { Table, Button, Space, Tag, Popconfirm, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { userApi, roleApi } from '@/api';
import type { User, Role } from '@/types/api.types';
import Loading from '@/components/common/Loading';

const UserListPage = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: users = [], isLoading: usersLoading } = useQuery({
    queryKey: ['users'],
    queryFn: userApi.getAll,
  });

  const { data: roles = [] } = useQuery({
    queryKey: ['roles'],
    queryFn: roleApi.getAll,
  });

  const deleteMutation = useMutation({
    mutationFn: userApi.delete,
    onSuccess: () => {
      message.success('User deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: () => {
      message.error('Failed to delete user');
    },
  });

  const roleMap = new Map(roles.map((role: Role) => [role.id, role.name]));

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: 'Roles',
      key: 'roles',
      render: (_: unknown, record: User) => {
        if (!record.roleIds || record.roleIds.length === 0) {
          return <span>-</span>;
        }
        return (
          <Space>
            {record.roleIds.map((roleId) => (
              <Tag key={roleId}>{roleMap.get(roleId) || `Role ${roleId}`}</Tag>
            ))}
          </Space>
        );
      },
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 150,
      render: (_: unknown, record: User) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => navigate(`/users/${record.id}/edit`)}
          >
            Edit
          </Button>
          <Popconfirm
            title="Are you sure you want to delete this user?"
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

  if (usersLoading) {
    return <Loading />;
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Users</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/users/new')}>
          Create User
        </Button>
      </div>
      <Table columns={columns} dataSource={users} rowKey="id" />
    </div>
  );
};

export default UserListPage;

