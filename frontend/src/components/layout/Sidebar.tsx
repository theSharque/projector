import { Layout, Menu } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  UserOutlined,
  TeamOutlined,
  ProjectOutlined,
  AppstoreOutlined,
  CheckSquareOutlined,
  FolderOutlined,
} from '@ant-design/icons';

const { Sider } = Layout;

const Sidebar = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = [
    {
      key: '/users',
      icon: <UserOutlined />,
      label: 'Users',
    },
    {
      key: '/roles',
      icon: <TeamOutlined />,
      label: 'Roles',
    },
    {
      key: '/roadmaps',
      icon: <ProjectOutlined />,
      label: 'Roadmaps',
    },
    {
      key: '/functional-areas',
      icon: <FolderOutlined />,
      label: 'Functional Areas',
    },
    {
      key: '/features',
      icon: <AppstoreOutlined />,
      label: 'Features',
    },
    {
      key: '/tasks',
      icon: <CheckSquareOutlined />,
      label: 'Tasks',
    },
  ];

  return (
    <Sider width={200} style={{ background: '#fff' }}>
      <Menu
        mode="inline"
        selectedKeys={[location.pathname]}
        style={{ height: '100%', borderRight: 0 }}
        items={menuItems}
        onClick={({ key }) => navigate(key)}
      />
    </Sider>
  );
};

export default Sidebar;

