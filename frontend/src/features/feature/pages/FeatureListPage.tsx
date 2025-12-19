import { useState } from 'react';
import { Table, Button, Space, Popconfirm, message, Select, Row, Col } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { featureApi, userApi } from '@/api';
import type { Feature, User } from '@/types/api.types';
import { QUARTERS } from '@/utils/constants';
import Loading from '@/components/common/Loading';

const FeatureListPage = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [yearFilter, setYearFilter] = useState<number | undefined>();
  const [quarterFilter, setQuarterFilter] = useState<string | undefined>();

  const { data: features = [], isLoading: featuresLoading } = useQuery({
    queryKey: ['features'],
    queryFn: featureApi.getAll,
  });

  const { data: users = [] } = useQuery({
    queryKey: ['users'],
    queryFn: userApi.getAll,
  });

  const deleteMutation = useMutation({
    mutationFn: featureApi.delete,
    onSuccess: () => {
      message.success('Feature deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['features'] });
    },
    onError: () => {
      message.error('Failed to delete feature');
    },
  });

  const userMap = new Map(users.map((user: User) => [user.id, user.email]));

  const filteredFeatures = features.filter((feature: Feature) => {
    if (yearFilter && feature.year !== yearFilter) return false;
    if (quarterFilter && feature.quarter !== quarterFilter) return false;
    return true;
  });

  const years = Array.from(new Set(features.map((f: Feature) => f.year))).sort((a, b) => b - a);

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: 'Year',
      dataIndex: 'year',
      key: 'year',
      width: 100,
    },
    {
      title: 'Quarter',
      dataIndex: 'quarter',
      key: 'quarter',
      width: 100,
    },
    {
      title: 'Summary',
      dataIndex: 'summary',
      key: 'summary',
    },
    {
      title: 'Author',
      key: 'author',
      render: (_: unknown, record: Feature) => userMap.get(record.authorId) || `User ${record.authorId}`,
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 150,
      render: (_: unknown, record: Feature) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => navigate(`/features/${record.id}/edit`)}
          >
            Edit
          </Button>
          <Popconfirm
            title="Are you sure you want to delete this feature?"
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

  if (featuresLoading) {
    return <Loading />;
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Features</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/features/new')}>
          Create Feature
        </Button>
      </div>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col>
          <Select
            placeholder="Filter by Year"
            allowClear
            style={{ width: 150 }}
            value={yearFilter}
            onChange={setYearFilter}
            options={years.map((year) => ({ value: year, label: year }))}
          />
        </Col>
        <Col>
          <Select
            placeholder="Filter by Quarter"
            allowClear
            style={{ width: 150 }}
            value={quarterFilter}
            onChange={setQuarterFilter}
            options={QUARTERS.map((q) => ({ value: q, label: q }))}
          />
        </Col>
      </Row>
      <Table columns={columns} dataSource={filteredFeatures} rowKey="id" />
    </div>
  );
};

export default FeatureListPage;

