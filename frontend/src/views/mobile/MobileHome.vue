<template>
  <div class="mobile-container">
    <div class="mobile-header">
      <div class="header-title">首页</div>
      <div class="header-action" @click="handleLogout">
        <i class="el-icon-switch-button"></i>
      </div>
    </div>

    <div class="mobile-content">
      <div class="mobile-user-info">
        <div class="avatar">
          {{ userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
        </div>
        <div class="info">
          <div class="name">{{ userInfo?.username || '用户' }}</div>
          <div class="role">{{ userInfo?.roleCode | formatStatus('role') }}</div>
        </div>
        <div class="logout-btn" @click="handleLogout">退出</div>
      </div>

      <div class="mobile-status-bar">
        <div class="status-item success">
          <div class="count">{{ stats.passed || 0 }}</div>
          <div class="label">检测合格</div>
        </div>
        <div class="status-item warning">
          <div class="count">{{ stats.pending || 0 }}</div>
          <div class="label">待检测</div>
        </div>
        <div class="status-item danger">
          <div class="count">{{ stats.failed || 0 }}</div>
          <div class="label">不合格</div>
        </div>
        <div class="status-item info">
          <div class="count">{{ stats.loaded || 0 }}</div>
          <div class="label">已装车</div>
        </div>
      </div>

      <div class="mobile-grid">
        <div
          v-for="item in menuItems"
          :key="item.path"
          class="mobile-grid-item"
          @click="navigateTo(item.path)">
          <div class="grid-icon" :class="item.iconClass">
            <i :class="item.icon"></i>
          </div>
          <div class="grid-title">{{ item.title }}</div>
        </div>
      </div>

      <div class="mobile-alert alert-info" style="margin-top: 20px;">
        <p><strong>业务规则提醒：</strong></p>
        <p>• 快检不合格的批次不能装车运输</p>
        <p>• 奶罐未清洗合格不能接新批次</p>
        <p>• 装车确认后检测结果将被锁定</p>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import { getBatchList } from '@/api/batch'

export default {
  name: 'MobileHome',
  data() {
    return {
      stats: {
        passed: 0,
        pending: 0,
        failed: 0,
        loaded: 0
      }
    }
  },
  computed: {
    ...mapGetters(['userInfo', 'roleCode']),
    menuItems() {
      const role = this.roleCode
      const menus = []

      if (role === 'ADMIN' || role === 'PASTURE') {
        menus.push(
          { title: '提交批次', path: '/m/batch/submit', icon: 'el-icon-plus', iconClass: 'icon-green' },
          { title: '批次列表', path: '/m/batch/list', icon: 'el-icon-s-order', iconClass: 'icon-blue' },
          { title: '清洗管理', path: '/m/cleaning/list', icon: 'el-icon-brush', iconClass: 'icon-cyan' }
        )
      }

      if (role === 'ADMIN' || role === 'DRIVER') {
        menus.push(
          { title: '确认装车', path: '/m/loading/confirm', icon: 'el-icon-circle-check', iconClass: 'icon-orange' },
          { title: '装车记录', path: '/m/loading/list', icon: 'el-icon-truck', iconClass: 'icon-blue' }
        )
      }

      if (role === 'ADMIN' || role === 'LAB') {
        menus.push(
          { title: '检测结果', path: '/m/test/list', icon: 'el-icon-search', iconClass: 'icon-red' }
        )
      }

      if (['ADMIN', 'LAB_SUPERVISOR', 'QUALITY_SUPERVISOR', 'PASTURE_SUPERVISOR', 'DEPARTMENT_MANAGER', 'QUALITY_DIRECTOR', 'ON_SITE_SUPERVISOR'].includes(role)) {
        menus.push(
          { title: '待我审批', path: '/m/approval/pending', icon: 'el-icon-s-claim', iconClass: 'icon-orange' }
        )
      }

      if (role === 'ADMIN') {
        menus.push(
          { title: '批次列表', path: '/m/batch/list', icon: 'el-icon-s-order', iconClass: 'icon-blue' },
          { title: '检测结果', path: '/m/test/list', icon: 'el-icon-search', iconClass: 'icon-red' }
        )
      }

      const uniqueMenus = []
      const paths = new Set()
      for (const menu of menus) {
        if (!paths.has(menu.path)) {
          paths.add(menu.path)
          uniqueMenus.push(menu)
        }
      }
      return uniqueMenus
    }
  },
  mounted() {
    this.loadStats()
  },
  methods: {
    ...mapActions('user', ['logout']),
    async loadStats() {
      try {
        const res = await getBatchList({ pageNum: 1, pageSize: 100 })
        const records = res.data?.records || []
        this.stats = {
          passed: records.filter(r => r.batchStatus === 'TEST_PASSED').length,
          pending: records.filter(r => r.batchStatus === 'PENDING_TEST').length,
          failed: records.filter(r => r.batchStatus === 'TEST_FAILED').length,
          loaded: records.filter(r => r.batchStatus === 'LOADED').length
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },
    navigateTo(path) {
      this.$router.push(path)
    },
    async handleLogout() {
      this.$confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await this.logout()
        this.$message.success('已退出登录')
        this.$router.push('/m/login')
      }).catch(() => {})
    }
  }
}
</script>
