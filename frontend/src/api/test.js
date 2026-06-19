import request from '@/utils/request'

export function submitTest(data) {
  return request({
    url: '/test/submit',
    method: 'post',
    data
  })
}

export function updateTest(id, data) {
  return request({
    url: '/test/' + id,
    method: 'put',
    data
  })
}

export function getTestList(params) {
  return request({
    url: '/test/list',
    method: 'get',
    params
  })
}

export function getTestDetail(id) {
  return request({
    url: '/test/' + id,
    method: 'get'
  })
}
