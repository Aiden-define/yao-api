// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** orderByTimesHasLimit GET /api/analysis/top/interface/invoke */
export async function orderByTimesHasLimitUsingGET(options?: { [key: string]: any }) {
  return request<API.ResultListInterfaceInfoVo>('/api/analysis/top/interface/invoke', {
    method: 'GET',
    ...(options || {}),
  });
}
