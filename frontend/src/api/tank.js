import request from '@/utils/request'

export function getTankList(params) {
  return request({
    url: '/tank/list',
    method: 'get',
    params
  })
}

export function getAvailableTanks() {
  return request({
    url: '/tank/available',
    method: 'get'
  })
}

export function getTankDetail(id) {
  return request({
    url: '/tank/' + id,
    method: 'get'
  })
}

export function checkTankCleaned(tankId) {
  return request({
    url: '/tank/check-cleaned/' + tankId,
    method: 'get'
  })
}

export function createTank(data) {
  return request({
    url: '/tank',
    method: 'post',
    data
  })
}

export function updateTank(data) {
  return request({
    url: '/tank',
    method: 'put',
    data
  })
}
