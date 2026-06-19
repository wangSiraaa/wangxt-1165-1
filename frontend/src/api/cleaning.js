import request from '@/utils/request'

export function startCleaning(data) {
  return request({
    url: '/cleaning/start',
    method: 'post',
    params: data
  })
}

export function finishCleaning(id, data) {
  return request({
    url: '/cleaning/finish/' + id,
    method: 'post',
    params: data
  })
}

export function getCleaningList(params) {
  return request({
    url: '/cleaning/list',
    method: 'get',
    params
  })
}

export function getCleaningDetail(id) {
  return request({
    url: '/cleaning/' + id,
    method: 'get'
  })
}
