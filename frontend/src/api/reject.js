import request from '@/utils/request'

export function getRejectList(params) {
  return request({
    url: '/reject/list',
    method: 'get',
    params
  })
}

export function getRejectDetail(id) {
  return request({
    url: '/reject/' + id,
    method: 'get'
  })
}

export function handleReject(id, data) {
  return request({
    url: '/reject/handle/' + id,
    method: 'post',
    params: data
  })
}
