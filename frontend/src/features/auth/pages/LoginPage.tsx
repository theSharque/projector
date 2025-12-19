import { Layout } from 'antd';
import LoginForm from '../components/LoginForm';

const { Content } = Layout;

const LoginPage = () => {
  return (
    <Layout style={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
      <Content style={{ maxWidth: 400, width: '100%', padding: 24 }}>
        <LoginForm />
      </Content>
    </Layout>
  );
};

export default LoginPage;

