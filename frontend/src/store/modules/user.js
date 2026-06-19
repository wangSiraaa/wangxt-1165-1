import Cookies from 'js-cookie'
import { login, logout } from '@/api/auth'

const state = {
  token: Cookies.get('token') || '',
  userInfo: JSON.parse(Cookies.get('userInfo') || 'null')
}

const mutations = {
  SET_TOKEN: (state, token) => {
    state.token = token
    Cookies.set('token', token, { expires: 7 })
  },
  SET_USER_INFO: (state, userInfo) => {
    state.userInfo = userInfo
    Cookies.set('userInfo', JSON.stringify(userInfo), { expires: 7 })
  },
  CLEAR_STATE: (state) => {
    state.token = ''
    state.userInfo = null
    Cookies.remove('token')
    Cookies.remove('userInfo')
  }
}

const actions = {
  async login({ commit }, loginData) {
    const res = await login(loginData)
    commit('SET_TOKEN', res.data.token)
    commit('SET_USER_INFO', res.data.userInfo)
    return res
  },
  async logout({ commit }) {
    try {
      await logout()
    } finally {
      commit('CLEAR_STATE')
    }
  },
  clearState({ commit }) {
    commit('CLEAR_STATE')
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
