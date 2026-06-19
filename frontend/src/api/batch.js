import request from '@/utils/request'

export function submitBatch(data) {
  return request({
    url: '/batch/submit',
    method: 'post',
    data
  })
}

export function getBatchList(params) {
  return request({
    url: '/batch/list',
    method: 'get',
    params
  })
}

export function getBatchDetail(id) {
  return request({
    url: '/batch/' + id,
    method: 'get'
  })
}
