import { useParams, useNavigate } from 'react-router-dom';
import { Form, Input, Button, Card, Checkbox, Space, message } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { roleApi } from '@/api';
import type { Role } from '@/types/api.types';
import { AUTHORITIES } from '@/utils/constants';
import Loading from '@/components/common/Loading';

const RoleFormPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const isEdit = id && id !== 'new';

  const { data: role, isLoading } = useQuery({
    queryKey: ['role', id],
    queryFn: () => roleApi.getById(Number(id!)),
    enabled: !!isEdit,
  });

  const createMutation = useMutation({
    mutationFn: roleApi.create,
    onSuccess: () => {
      message.success('Role created successfully');
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      navigate('/roles');
    },
    onError: () => {
      message.error('Failed to create role');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, role }: { id: number; role: Role }) => roleApi.update(id, role),
    onSuccess: () => {
      message.success('Role updated successfully');
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      navigate('/roles');
    },
    onError: () => {
      message.error('Failed to update role');
    },
  });

  const onFinish = (values: { name: string; authorities: string[] }) => {
    const roleData: Role = {
      name: values.name,
      authorities: values.authorities || [],
    };

    if (isEdit && id) {
      roleData.id = Number(id);
      updateMutation.mutate({ id: Number(id), role: roleData });
    } else {
      createMutation.mutate(roleData);
    }
  };

  if (isEdit && isLoading) {
    return <Loading />;
  }

  if (isEdit && role && 'name' in role) {
    form.setFieldsValue({
      name: role.name,
      authorities: Array.from(role.authorities || []),
    });
  }

  const authorityOptions = Object.values(AUTHORITIES);

  return (
    <Card title={isEdit ? 'Edit Role' : 'Create Role'}>
      <Form form={form} onFinish={onFinish} layout="vertical">
        <Form.Item
          label="Name"
          name="name"
          rules={[{ required: true, message: 'Please input role name!' }]}
        >
          <Input />
        </Form.Item>

        <Form.Item label="Authorities" name="authorities">
          <Checkbox.Group>
            <Space direction="vertical">
              {authorityOptions.map((auth) => (
                <Checkbox key={auth} value={auth}>
                  {auth}
                </Checkbox>
              ))}
            </Space>
          </Checkbox.Group>
        </Form.Item>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={createMutation.isPending || updateMutation.isPending}>
              {isEdit ? 'Update' : 'Create'}
            </Button>
            <Button onClick={() => navigate('/roles')}>Cancel</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default RoleFormPage;

