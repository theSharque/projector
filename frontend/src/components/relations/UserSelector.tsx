import { Select } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { userApi } from '@/api/user.api';
import type { User } from '@/types/api.types';

interface UserSelectorProps {
  value?: number;
  onChange?: (value: number) => void;
  placeholder?: string;
  disabled?: boolean;
}

const UserSelector = ({ value, onChange, placeholder = 'Select user', disabled = false }: UserSelectorProps) => {
  const { data: users = [], isLoading } = useQuery({
    queryKey: ['users'],
    queryFn: userApi.getAll,
  });

  return (
    <Select
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      disabled={disabled}
      loading={isLoading}
      showSearch
      optionFilterProp="label"
      style={{ width: '100%' }}
      options={users.map((user: User) => ({
        value: user.id,
        label: user.email,
      }))}
    />
  );
};

export default UserSelector;

