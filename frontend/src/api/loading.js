import request from '@/utils/request'

export function confirmLoading(data) {
  return request({
    url: '/loading/confirm',
    method: 'post',
    data
  })
}

export function getLoadingList(params) {
  return request({
    url: '/loading/list',
    method: 'get',
    params
  })
}

export function getLoadingDetail(id) {
  return request({
    url: '/loading/' + id,
    method: 'get'
  })
}
