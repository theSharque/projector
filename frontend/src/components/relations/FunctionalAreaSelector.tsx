import { Select } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { functionalAreaApi } from '@/api';

interface FunctionalAreaSelectorProps {
  value?: number[];
  onChange?: (value: number[]) => void;
  placeholder?: string;
  disabled?: boolean;
}

const FunctionalAreaSelector = ({ value, onChange, placeholder, disabled }: FunctionalAreaSelectorProps) => {
  const { data: functionalAreas = [], isLoading } = useQuery({
    queryKey: ['functional-areas'],
    queryFn: functionalAreaApi.getAll,
  });

  return (
    <Select
      mode="multiple"
      value={value}
      onChange={onChange}
      placeholder={placeholder || 'Select functional areas'}
      loading={isLoading}
      disabled={disabled}
      style={{ width: '100%' }}
      options={functionalAreas.map((fa) => ({
        label: fa.name,
        value: fa.id,
      }))}
    />
  );
};

export default FunctionalAreaSelector;
