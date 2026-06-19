import request from '@/utils/request'

export function getTraceByBatchId(batchId) {
  return request({
    url: '/trace/batch/' + batchId,
    method: 'get'
  })
}

export function getTraceByTankId(tankId) {
  return request({
    url: '/trace/tank/' + tankId,
    method: 'get'
  })
}
