import { roadmapApi } from '@/api/roadmap.api';
import type { Roadmap } from '@/types/api.types';
import { useQuery } from '@tanstack/react-query';
import { Select } from 'antd';

interface RoadmapSelectorProps {
  value?: number;
  onChange?: (value: number) => void;
  placeholder?: string;
  disabled?: boolean;
}

const RoadmapSelector = ({ value, onChange, placeholder = 'Select roadmap', disabled = false }: RoadmapSelectorProps) => {
  const { data: roadmaps = [], isLoading } = useQuery({
    queryKey: ['roadmaps'],
    queryFn: roadmapApi.getAll,
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
      options={roadmaps.map((roadmap: Roadmap) => ({
        value: roadmap.id,
        label: roadmap.projectName,
      }))}
    />
  );
};

export default RoadmapSelector;
