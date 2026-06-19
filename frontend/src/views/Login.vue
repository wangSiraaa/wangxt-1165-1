<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>奶牛场生鲜乳抗生素检测系统</h2>
        <p>Dairy Farm Milk Quality Control System</p>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginForm" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="el-icon-user" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" @click="handleLogin" :loading="loading">登 录</el-button>
      </el-form>
      <div class="login-footer">
        <p>默认账号：admin/pasture1/lab1/driver1</p>
        <p>默认密码：123456</p>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      loading: false
    }
  },
  methods: {
    ...mapActions('user', ['login']),
    async handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            await this.login(this.loginForm)
            this.$message.success('登录成功')
            this.$router.push('/')
          } catch (error) {
            console.error('登录失败:', error)
          } finally {
            this.loading = false
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100%;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    margin: 0 0 10px 0;
    color: #303133;
    font-size: 22px;
    font-weight: 600;
  }
  
  p {
    margin: 0;
    color: #909399;
    font-size: 14px;
  }
}

.login-form {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
}

.login-footer {
  text-align: center;
  color: #909399;
  font-size: 12px;
  
  p {
    margin: 5px 0;
  }
}
</style>
