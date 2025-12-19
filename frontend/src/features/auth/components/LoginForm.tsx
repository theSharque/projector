import { useState } from 'react';
import { Form, Input, Button, Card, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { authApi } from '@/api/auth.api';
import { toast } from 'react-hot-toast';

const { Title } = Typography;

const LoginForm = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { setAuthenticated } = useAuthStore();

  const onFinish = async (values: { email: string; password: string }) => {
    setLoading(true);
    try {
      const response = await authApi.login(values);
      if (response.status === 204) {
        try {
          const authorities = await authApi.getProfile();
          setAuthenticated(authorities);
          toast.success('Login successful');
          navigate('/users');
        } catch (profileError) {
          console.error('Profile fetch error:', profileError);
          // Even if profile fails, if login succeeded, we can still navigate
          // The cookie is set, so subsequent requests will work
          setAuthenticated(new Set());
          toast.success('Login successful');
          navigate('/users');
        }
      } else {
        toast.error('Invalid credentials');
      }
    } catch (error) {
      console.error('Login error:', error);
      toast.error('Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card>
      <Title level={2} style={{ textAlign: 'center', marginBottom: 24 }}>
        Projector Login
      </Title>
      <Form name="login" onFinish={onFinish} layout="vertical" autoComplete="off">
        <Form.Item
          label="Email"
          name="email"
          rules={[{ required: true, message: 'Please input your email!' }]}
        >
          <Input />
        </Form.Item>

        <Form.Item label="Password" name="password" rules={[{ required: true, message: 'Please input your password!' }]}>
          <Input.Password />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" block loading={loading}>
            Login
          </Button>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default LoginForm;

