import { Select } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { featureApi } from '@/api/feature.api';
import type { Feature } from '@/types/api.types';

interface FeatureSelectorProps {
  value?: number;
  onChange?: (value: number) => void;
  placeholder?: string;
  disabled?: boolean;
}

const FeatureSelector = ({ value, onChange, placeholder = 'Select feature', disabled = false }: FeatureSelectorProps) => {
  const { data: features = [], isLoading } = useQuery({
    queryKey: ['features'],
    queryFn: featureApi.getAll,
  });

  const formatFeatureLabel = (feature: Feature) => {
    return `${feature.year} ${feature.quarter}${feature.summary ? ` - ${feature.summary}` : ''}`;
  };

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
      options={features.map((feature: Feature) => ({
        value: feature.id,
        label: formatFeatureLabel(feature),
      }))}
    />
  );
};

export default FeatureSelector;

