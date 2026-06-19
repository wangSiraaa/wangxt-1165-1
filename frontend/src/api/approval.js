import request from '@/utils/request'

export function submitApproval(data) {
  return request({
    url: '/approval/submit',
    method: 'post',
    data
  })
}

export function approve(data) {
  return request({
    url: '/approval/approve',
    method: 'post',
    data
  })
}

export function reject(data) {
  return request({
    url: '/approval/reject',
    method: 'post',
    data
  })
}

export function cancelApproval(id) {
  return request({
    url: '/approval/cancel/' + id,
    method: 'post'
  })
}

export function getApprovalDetail(id) {
  return request({
    url: '/approval/' + id,
    method: 'get'
  })
}

export function getMyPending(params) {
  return request({
    url: '/approval/myPending',
    method: 'get',
    params
  })
}

export function getMyApplied(params) {
  return request({
    url: '/approval/myApplied',
    method: 'get',
    params
  })
}

export function getApprovalHistory(params) {
  return request({
    url: '/approval/history',
    method: 'get',
    params
  })
}
