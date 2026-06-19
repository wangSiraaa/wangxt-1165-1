import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import './utils/request'
import './styles/index.scss'
import './styles/mobile.scss'
import moment from 'moment'

Vue.use(ElementUI)
Vue.config.productionTip = false

Vue.filter('formatDate', function(value, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!value) return ''
  return moment(value).format(format)
})

Vue.filter('formatStatus', function(value, type) {
  const statusMap = {
    batch: {
      MILKING: '挤奶中',
      PENDING_TEST: '待检测',
      TEST_PASSED: '检测通过',
      TEST_FAILED: '检测不合格',
      LOADED: '已装车',
      REJECTED: '已拒收'
    },
    tank: {
      IDLE: '空闲',
      LOADING: '接奶中',
      PENDING_TEST: '待检测',
      TESTED: '检测完成',
      LOADED: '已装车',
      PENDING_CLEAN: '待清洗'
    },
    test: {
      PASSED: '合格',
      FAILED: '不合格'
    },
    rejectType: {
      TEST_FAILED: '检测不合格',
      TANK_DIRTY: '奶罐未清洗',
      OTHER: '其他'
    },
    handleStatus: {
      PENDING: '待处理',
      HANDLED: '已处理',
      IGNORED: '已忽略'
    },
    role: {
      ADMIN: '管理员',
      PASTURE: '牧场',
      LAB: '化验室',
      DRIVER: '司机',
      LAB_SUPERVISOR: '化验主管',
      QUALITY_SUPERVISOR: '质量主管',
      PASTURE_SUPERVISOR: '牧场主管',
      DEPARTMENT_MANAGER: '部门经理',
      QUALITY_DIRECTOR: '质量总监',
      ON_SITE_SUPERVISOR: '现场主管'
    },
    approvalType: {
      BATCH_TEST: '批次检测审批',
      LOADING_CONFIRM: '装车确认审批',
      TANK_CLEANING: '奶罐清洗审批',
      EXCEPTION_HANDLE: '异常处理审批'
    },
    approvalStatus: {
      PENDING: '待审批',
      APPROVING: '审批中',
      APPROVED: '已通过',
      REJECTED: '已驳回',
      CANCELLED: '已取消'
    },
    nodeStatus: {
      PENDING: '待处理',
      PROCESSING: '处理中',
      APPROVED: '已通过',
      REJECTED: '已驳回',
      SKIPPED: '已跳过'
    },
    approvalAction: {
      APPROVE: '通过',
      REJECT: '驳回',
      SUBMIT: '提交',
      CANCEL: '取消'
    },
    priority: {
      URGENT: '紧急',
      HIGH: '高',
      NORMAL: '普通',
      LOW: '低'
    }
  }
  return statusMap[type] ? statusMap[type][value] || value : value
})

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
