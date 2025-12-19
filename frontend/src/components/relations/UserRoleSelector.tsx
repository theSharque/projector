import { Select } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { roleApi } from '@/api/role.api';
import type { Role } from '@/types/api.types';

interface UserRoleSelectorProps {
  value?: number[];
  onChange?: (value: number[]) => void;
  placeholder?: string;
  disabled?: boolean;
}

const UserRoleSelector = ({ value, onChange, placeholder = 'Select roles', disabled = false }: UserRoleSelectorProps) => {
  const { data: roles = [], isLoading } = useQuery({
    queryKey: ['roles'],
    queryFn: roleApi.getAll,
  });

  return (
    <Select
      mode="multiple"
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      disabled={disabled}
      loading={isLoading}
      showSearch
      optionFilterProp="label"
      style={{ width: '100%' }}
      options={roles.map((role: Role) => ({
        value: role.id,
        label: role.name,
      }))}
    />
  );
};

export default UserRoleSelector;

