import { useState } from 'react';
import { Table, Button, Space, message, Modal, Select, Alert } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { functionalAreaApi } from '@/api';
import type { FunctionalArea } from '@/types/api.types';
import Loading from '@/components/common/Loading';

const FunctionalAreaListPage = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [faToDelete, setFaToDelete] = useState<FunctionalArea | null>(null);
  const [replacementFaId, setReplacementFaId] = useState<number | undefined>();
  const [usageCount, setUsageCount] = useState<number>(0);

  const { data: functionalAreas = [], isLoading: fasLoading } = useQuery({
    queryKey: ['functional-areas'],
    queryFn: functionalAreaApi.getAll,
  });

  const deleteMutation = useMutation({
    mutationFn: ({ id, replacementId }: { id: number; replacementId: number }) =>
      functionalAreaApi.delete(id, replacementId),
    onSuccess: () => {
      message.success('Functional area deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['functional-areas'] });
      setDeleteModalOpen(false);
      setFaToDelete(null);
      setReplacementFaId(undefined);
      setUsageCount(0);
    },
    onError: () => {
      message.error('Failed to delete functional area');
    },
  });

  const handleDeleteClick = async (fa: FunctionalArea) => {
    try {
      const count = await functionalAreaApi.getUsage(fa.id);
      setUsageCount(count);
      setFaToDelete(fa);
      setDeleteModalOpen(true);
    } catch (error) {
      message.error('Failed to check functional area usage');
    }
  };

  const handleDeleteConfirm = () => {
    if (!faToDelete || !replacementFaId) {
      message.error('Please select a replacement functional area');
      return;
    }
    deleteMutation.mutate({ id: faToDelete.id, replacementId: replacementFaId });
  };

  const handleDeleteCancel = () => {
    setDeleteModalOpen(false);
    setFaToDelete(null);
    setReplacementFaId(undefined);
    setUsageCount(0);
  };

  const availableReplacements = functionalAreas.filter((fa) => fa.id !== faToDelete?.id);

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 150,
      render: (_: unknown, record: FunctionalArea) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => navigate(`/functional-areas/${record.id}/edit`)}
          >
            Edit
          </Button>
          <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDeleteClick(record)}>
            Delete
          </Button>
        </Space>
      ),
    },
  ];

  if (fasLoading) {
    return <Loading />;
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Functional Areas</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/functional-areas/new')}>
          Create Functional Area
        </Button>
      </div>
      <Table columns={columns} dataSource={functionalAreas} rowKey="id" />

      <Modal
        title="Delete Functional Area"
        open={deleteModalOpen}
        onOk={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
        confirmLoading={deleteMutation.isPending}
        okButtonProps={{ disabled: !replacementFaId }}
      >
        {faToDelete && (
          <Space direction="vertical" style={{ width: '100%' }}>
            {usageCount > 0 && (
              <Alert
                message="Warning"
                description={`This functional area is used by ${usageCount} feature(s). You must select a replacement functional area to migrate all features.`}
                type="warning"
                showIcon
              />
            )}
            {usageCount === 0 && (
              <Alert
                message="Info"
                description="This functional area is not currently used by any features, but you still need to select a replacement functional area for safety."
                type="info"
                showIcon
              />
            )}
            <p>
              You are about to delete: <strong>{faToDelete.name}</strong>
            </p>
            <div>
              <label htmlFor="replacement-fa">Select replacement functional area:</label>
              <Select
                id="replacement-fa"
                style={{ width: '100%', marginTop: 8 }}
                placeholder="Select replacement functional area"
                value={replacementFaId}
                onChange={setReplacementFaId}
                options={availableReplacements.map((fa) => ({
                  label: fa.name,
                  value: fa.id,
                }))}
              />
            </div>
          </Space>
        )}
      </Modal>
    </div>
  );
};

export default FunctionalAreaListPage;
