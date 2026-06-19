import axios from 'axios'
import { Message } from 'element-ui'
import store from '@/store'
import router from '@/router'
import Cookies from 'js-cookie'

const service = axios.create({
  baseURL: process.env.NODE_ENV === 'production' ? '' : '/api',
  timeout: 30000
})

service.interceptors.request.use(
  config => {
    const token = Cookies.get('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200 && res.code !== 0) {
      Message.error(res.message || '请求失败')
      if (res.code === 401 || res.code === 403) {
        store.dispatch('user/clearState')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)
    if (error.response?.status === 401) {
      Message.error('登录已过期，请重新登录')
      store.dispatch('user/clearState')
      router.push('/login')
    } else if (error.response?.status === 403) {
      Message.error('没有访问权限')
    } else {
      Message.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
