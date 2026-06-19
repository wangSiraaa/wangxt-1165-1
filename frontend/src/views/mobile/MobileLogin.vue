<template>
  <div class="mobile-login">
    <div class="login-header">
      <div class="logo">🐄</div>
      <h1>生鲜乳检测系统</h1>
      <p>Dairy Farm Milk Quality Control</p>
    </div>

    <div class="login-form">
      <form class="mobile-form" @submit.prevent="handleLogin">
        <div class="form-item">
          <label class="form-label">
            <span class="required">*</span>用户名
          </label>
          <input
            v-model="loginForm.username"
            type="text"
            class="form-input"
            placeholder="请输入用户名"
            :class="{ error: errors.username }" />
          <div v-if="errors.username" class="error-msg">{{ errors.username }}</div>
        </div>

        <div class="form-item">
          <label class="form-label">
            <span class="required">*</span>密码
          </label>
          <input
            v-model="loginForm.password"
            type="password"
            class="form-input"
            placeholder="请输入密码"
            :class="{ error: errors.password }" />
          <div v-if="errors.password" class="error-msg">{{ errors.password }}</div>
        </div>

        <button type="submit" class="mobile-btn btn-primary" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <div class="login-tips">
        <p>牧场用户：pasture1 / 123456</p>
        <p>司机用户：driver1 / 123456</p>
        <p>化验室：lab1 / 123456</p>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'MobileLogin',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      errors: {},
      loading: false
    }
  },
  methods: {
    ...mapActions('user', ['login']),
    validate() {
      this.errors = {}
      if (!this.loginForm.username.trim()) {
        this.errors.username = '请输入用户名'
      }
      if (!this.loginForm.password) {
        this.errors.password = '请输入密码'
      }
      return Object.keys(this.errors).length === 0
    },
    async handleLogin() {
      if (!this.validate()) return
      
      this.loading = true
      try {
        await this.login(this.loginForm)
        this.$message.success('登录成功')
        this.$router.push('/m/home')
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
