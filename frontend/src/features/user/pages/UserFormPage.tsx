import { useParams, useNavigate } from 'react-router-dom';
import { Form, Input, Button, Card, Space, message } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userApi } from '@/api';
import type { User } from '@/types/api.types';
import UserRoleSelector from '@/components/relations/UserRoleSelector';
import Loading from '@/components/common/Loading';

const UserFormPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const isEdit = id && id !== 'new';

  const { data: user, isLoading } = useQuery({
    queryKey: ['user', id],
    queryFn: () => userApi.getById(Number(id!)),
    enabled: !!isEdit,
  });

  const createMutation = useMutation({
    mutationFn: userApi.create,
    onSuccess: () => {
      message.success('User created successfully');
      queryClient.invalidateQueries({ queryKey: ['users'] });
      navigate('/users');
    },
    onError: () => {
      message.error('Failed to create user');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, user }: { id: number; user: User }) => userApi.update(id, user),
    onSuccess: () => {
      message.success('User updated successfully');
      queryClient.invalidateQueries({ queryKey: ['users'] });
      navigate('/users');
    },
    onError: () => {
      message.error('Failed to update user');
    },
  });

  const onFinish = (values: User) => {
    if (isEdit) {
      updateMutation.mutate({ id: Number(id), user: values });
    } else {
      createMutation.mutate(values);
    }
  };

  if (isEdit && isLoading) {
    return <Loading />;
  }

  if (isEdit && user) {
    form.setFieldsValue(user);
  }

  return (
    <Card title={isEdit ? 'Edit User' : 'Create User'}>
      <Form form={form} onFinish={onFinish} layout="vertical">
        <Form.Item
          label="Email"
          name="email"
          rules={[
            { required: true, message: 'Please input email!' },
            { type: 'email', message: 'Invalid email format' },
          ]}
        >
          <Input />
        </Form.Item>

        <Form.Item
          label="Password"
          name="password"
          rules={isEdit ? [] : [{ required: true, message: 'Please input password!' }]}
        >
          <Input.Password placeholder={isEdit ? 'Leave empty to keep current password' : ''} />
        </Form.Item>

        <Form.Item label="Roles" name="roleIds">
          <UserRoleSelector />
        </Form.Item>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={createMutation.isPending || updateMutation.isPending}>
              {isEdit ? 'Update' : 'Create'}
            </Button>
            <Button onClick={() => navigate('/users')}>Cancel</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default UserFormPage;

