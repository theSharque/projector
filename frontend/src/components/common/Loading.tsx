import { Spin } from 'antd';

const Loading = () => {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '200px' }}>
      <Spin size="large" />
    </div>
  );
};

export default Loading;

