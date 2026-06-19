import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store'
import Cookies from 'js-cookie'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'el-icon-s-home' }
      },
      {
        path: 'batch',
        name: 'Batch',
        component: () => import('@/views/batch/BatchList.vue'),
        meta: { title: '批次管理', icon: 'el-icon-s-order', roles: ['ADMIN', 'PASTURE'] }
      },
      {
        path: 'batch/submit',
        name: 'BatchSubmit',
        component: () => import('@/views/batch/BatchSubmit.vue'),
        meta: { title: '提交批次', icon: 'el-icon-plus', roles: ['ADMIN', 'PASTURE'] }
      },
      {
        path: 'test',
        name: 'Test',
        component: () => import('@/views/test/TestList.vue'),
        meta: { title: '检测管理', icon: 'el-icon-search', roles: ['ADMIN', 'LAB'] }
      },
      {
        path: 'test/submit',
        name: 'TestSubmit',
        component: () => import('@/views/test/TestSubmit.vue'),
        meta: { title: '录入检测', icon: 'el-icon-edit', roles: ['ADMIN', 'LAB'] }
      },
      {
        path: 'loading',
        name: 'Loading',
        component: () => import('@/views/loading/LoadingList.vue'),
        meta: { title: '装车管理', icon: 'el-icon-truck', roles: ['ADMIN', 'DRIVER'] }
      },
      {
        path: 'loading/confirm',
        name: 'LoadingConfirm',
        component: () => import('@/views/loading/LoadingConfirm.vue'),
        meta: { title: '确认装车', icon: 'el-icon-circle-check', roles: ['ADMIN', 'DRIVER'] }
      },
      {
        path: 'cleaning',
        name: 'Cleaning',
        component: () => import('@/views/cleaning/CleaningList.vue'),
        meta: { title: '清洗管理', icon: 'el-icon-brush', roles: ['ADMIN', 'PASTURE'] }
      },
      {
        path: 'reject',
        name: 'Reject',
        component: () => import('@/views/reject/RejectList.vue'),
        meta: { title: '拒收记录', icon: 'el-icon-close', roles: ['ADMIN'] }
      },
      {
        path: 'tank',
        name: 'Tank',
        component: () => import('@/views/tank/TankList.vue'),
        meta: { title: '奶罐管理', icon: 'el-icon-s-cooperation', roles: ['ADMIN'] }
      },
      {
        path: 'trace',
        name: 'Trace',
        component: () => import('@/views/trace/TraceView.vue'),
        meta: { title: '全链路追踪', icon: 'el-icon-connection' }
      },
      {
        path: 'approval/pending',
        name: 'ApprovalPending',
        component: () => import('@/views/approval/ApprovalPending.vue'),
        meta: { title: '我的待审批', icon: 'el-icon-s-claim', roles: ['ADMIN', 'LAB_SUPERVISOR', 'QUALITY_SUPERVISOR', 'PASTURE_SUPERVISOR', 'DEPARTMENT_MANAGER', 'QUALITY_DIRECTOR', 'ON_SITE_SUPERVISOR'] }
      },
      {
        path: 'approval/applied',
        name: 'ApprovalApplied',
        component: () => import('@/views/approval/ApprovalApplied.vue'),
        meta: { title: '我提交的审批', icon: 'el-icon-document' }
      },
      {
        path: 'approval/:id',
        name: 'ApprovalDetail',
        component: () => import('@/views/approval/ApprovalDetail.vue'),
        meta: { title: '审批详情', icon: 'el-icon-tickets', hidden: true }
      }
    ]
  },
  {
    path: '/m/login',
    name: 'MobileLogin',
    component: () => import('@/views/mobile/MobileLogin.vue'),
    meta: { title: '移动端登录', requiresAuth: false, isMobile: true }
  },
  {
    path: '/m',
    component: () => import('@/views/mobile/MobileHome.vue'),
    redirect: '/m/home',
    meta: { requiresAuth: true, isMobile: true },
    children: [
      {
        path: 'home',
        name: 'MobileHome',
        component: () => import('@/views/mobile/MobileHome.vue'),
        meta: { title: '移动端首页', isMobile: true }
      },
      {
        path: 'batch/submit',
        name: 'MobileBatchSubmit',
        component: () => import('@/views/mobile/MobileBatchSubmit.vue'),
        meta: { title: '提交批次', isMobile: true, roles: ['ADMIN', 'PASTURE'] }
      },
      {
        path: 'batch/list',
        name: 'MobileBatchList',
        component: () => import('@/views/mobile/MobileBatchList.vue'),
        meta: { title: '批次列表', isMobile: true }
      },
      {
        path: 'loading/confirm',
        name: 'MobileLoadingConfirm',
        component: () => import('@/views/mobile/MobileLoadingConfirm.vue'),
        meta: { title: '确认装车', isMobile: true, roles: ['ADMIN', 'DRIVER'] }
      },
      {
        path: 'loading/list',
        name: 'MobileLoadingList',
        component: () => import('@/views/mobile/MobileLoadingList.vue'),
        meta: { title: '装车记录', isMobile: true }
      },
      {
        path: 'cleaning/list',
        name: 'MobileCleaningList',
        component: () => import('@/views/mobile/MobileCleaningList.vue'),
        meta: { title: '清洗管理', isMobile: true, roles: ['ADMIN', 'PASTURE'] }
      },
      {
        path: 'test/list',
        name: 'MobileTestList',
        component: () => import('@/views/mobile/MobileTestList.vue'),
        meta: { title: '检测结果', isMobile: true }
      },
      {
        path: 'approval/pending',
        name: 'MobileApprovalPending',
        component: () => import('@/views/mobile/MobileApprovalPending.vue'),
        meta: { title: '我的待审批', isMobile: true, roles: ['ADMIN', 'LAB_SUPERVISOR', 'QUALITY_SUPERVISOR', 'PASTURE_SUPERVISOR', 'DEPARTMENT_MANAGER', 'QUALITY_DIRECTOR', 'ON_SITE_SUPERVISOR'] }
      },
      {
        path: 'approval/:id',
        name: 'MobileApprovalDetail',
        component: () => import('@/views/mobile/MobileApprovalDetail.vue'),
        meta: { title: '审批详情', isMobile: true }
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

router.beforeEach((to, from, next) => {
  const token = Cookies.get('token')
  const userInfo = store.state.user.userInfo

  if (to.meta.title) {
    document.title = to.meta.title + ' - 奶牛场生鲜乳检测系统'
  }

  if (to.meta.requiresAuth === false) {
    if (token && userInfo) {
      if (to.meta.isMobile) {
        next('/m/home')
      } else {
        next('/')
      }
    } else {
      next()
    }
    return
  }

  if (!token) {
    if (to.meta.isMobile) {
      next('/m/login')
    } else {
      next('/login')
    }
    return
  }

  if (to.meta.roles && userInfo) {
    if (!to.meta.roles.includes(userInfo.roleCode)) {
      next('/403')
      return
    }
  }

  next()
})

export default router
