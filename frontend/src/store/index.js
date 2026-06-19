import Vue from 'vue'
import Vuex from 'vuex'
import user from './modules/user'

Vue.use(Vuex)

const store = new Vuex.Store({
  modules: {
    user
  },
  getters: {
    token: state => state.user.token,
    userInfo: state => state.user.userInfo,
    roleCode: state => state.user.userInfo?.roleCode
  }
})

export default store
