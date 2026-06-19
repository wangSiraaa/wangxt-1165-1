<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <h3>生鲜乳检测系统</h3>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        mode="vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        :collapse="false"
      >
        <template v-for="route in menuRoutes">
          <el-menu-item :key="route.path" :index="'/' + route.path">
            <i :class="route.meta.icon"></i>
            <span slot="title">{{ route.meta.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="left">
          <span class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item v-for="(item, index) in $route.matched" :key="index" :to="index === $route.matched.length - 1 ? '' : item.path">
                {{ item.meta.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </span>
        </div>
        <div class="right">
          <span class="user-info">
            <i class="el-icon-user"></i>
            {{ userInfo?.realName || '用户' }}
            <el-tag size="mini" style="margin-left: 10px;">{{ userInfo?.roleCode | formatStatus('role') }}</el-tag>
          </span>
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <i class="el-icon-setting"></i>
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'Layout',
  computed: {
    ...mapGetters(['userInfo', 'roleCode']),
    menuRoutes() {
      const routes = this.$route.matched[0]?.children || []
      return routes.filter(route => {
        if (!route.meta?.title) return false
        if (route.meta?.hidden) return false
        if (route.meta?.roles && !route.meta.roles.includes(this.roleCode)) return false
        return true
      })
    }
  },
  methods: {
    ...mapActions('user', ['logout']),
    async handleCommand(command) {
      if (command === 'logout') {
        this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          await this.logout()
          this.$message.success('已退出登录')
          this.$router.push('/login')
        }).catch(() => {})
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100%;
}

.sidebar {
  background: #304156;
  transition: width 0.3s;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #2b2f3a;
    
    h3 {
      color: #fff;
      margin: 0;
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  .el-menu {
    border-right: none;
  }
}

.header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #e4e7ed;
  
  .left {
    .breadcrumb {
      font-size: 14px;
    }
  }
  
  .right {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .user-info {
      color: #606266;
    }
    
    .el-dropdown-link {
      cursor: pointer;
      color: #606266;
    }
  }
}

.main {
  background: #f0f2f5;
  padding: 20px;
  overflow: auto;
}
</style>
