import dayjs from 'dayjs';

export const formatDate = (date: string | undefined): string => {
  if (!date) return '';
  return dayjs(date).format('YYYY-MM-DD HH:mm');
};

export const hasAuthority = (authorities: Set<string>, authority: string): boolean => {
  return authorities.has(authority);
};

export const hasAnyAuthority = (authorities: Set<string>, requiredAuthorities: string[]): boolean => {
  return requiredAuthorities.some((auth) => authorities.has(auth));
};

export const hasAllAuthorities = (authorities: Set<string>, requiredAuthorities: string[]): boolean => {
  return requiredAuthorities.every((auth) => authorities.has(auth));
};

