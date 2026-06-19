<template>
  <div class="mobile-container">
    <div class="mobile-header">
      <div class="header-back" @click="$router.back()">
        <i class="el-icon-arrow-left"></i>
      </div>
      <div class="header-title">清洗管理</div>
      <div class="header-action" @click="showStartDialog = true">
        <i class="el-icon-plus"></i>
      </div>
    </div>

    <div class="mobile-content">
      <div class="mobile-alert alert-info">
        <p><strong>业务规则提醒：</strong></p>
        <p>• 装车后的奶罐需要及时清洗</p>
        <p>• 清洗合格后奶罐才能接收新批次</p>
        <p>• 清洗不合格需要重新清洗</p>
      </div>

      <div class="mobile-tabs">
        <div
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: activeTab === tab.value }"
          @click="switchTab(tab.value)">
          {{ tab.label }}
        </div>
      </div>

      <div
        v-if="loading"
        class="mobile-loading">
        <div class="loading-spinner"></div>
        <div>加载中...</div>
      </div>

      <div
        v-else-if="filteredList.length === 0"
        class="mobile-empty">
        <div class="empty-icon">🧹</div>
        <div class="empty-text">暂无清洗记录</div>
      </div>

      <div v-else>
        <div
          v-for="item in filteredList"
          :key="item.id"
          class="mobile-card">
          <div class="mobile-card-header">
            <div class="card-title">{{ item.tankName }}</div>
            <span
              class="mobile-tag"
              :class="getCleaningStatusClass(item.isQualified)">
              {{ getCleaningStatusText(item.isQualified) }}
            </span>
          </div>
          <div class="mobile-card-content">
            <div class="info-row">
              <span class="label">清洗方式</span>
              <span class="value">{{ item.cleaningMethod || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">清洗剂</span>
              <span class="value">{{ item.cleaningAgent || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">水温</span>
              <span class="value">{{ item.waterTemperature !== null ? item.waterTemperature + '°C' : '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">开始时间</span>
              <span class="value">{{ item.startTime | formatDate('YYYY-MM-DD HH:mm') }}</span>
            </div>
            <div class="info-row" v-if="item.endTime">
              <span class="label">结束时间</span>
              <span class="value">{{ item.endTime | formatDate('YYYY-MM-DD HH:mm') }}</span>
            </div>
          </div>
          <div class="mobile-btn-group">
            <button
              class="mobile-btn btn-primary"
              :disabled="item.isQualified !== null"
              @click="finishCleaning(item)">
              {{ item.isQualified === null ? '完成清洗' : '已完成' }}
            </button>
          </div>
        </div>
      </div>

      <div
        v-if="!loading && hasMore"
        class="mobile-pull-refresh"
        @click="loadMore">
        <span>{{ loadingMore ? '加载中...' : '加载更多' }}</span>
      </div>

      <div
        v-if="!loading && !hasMore && list.length > 0"
        class="mobile-pull-refresh">
        <span>没有更多了</span>
      </div>
    </div>

    <div v-if="showStartDialog" class="mobile-container" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; z-index: 200; background: rgba(0,0,0,0.5);">
      <div style="position: absolute; bottom: 0; left: 0; right: 0; background: #fff; border-radius: 16px 16px 0 0; max-height: 90vh; overflow-y: auto;">
        <div class="mobile-header" style="border-radius: 16px 16px 0 0;">
          <div class="header-back" @click="showStartDialog = false">
            <i class="el-icon-close"></i>
          </div>
          <div class="header-title">开始清洗</div>
          <div class="header-action"></div>
        </div>
        <div class="mobile-content">
          <form class="mobile-form" @submit.prevent="submitStart">
            <div class="form-item">
              <label class="form-label">
                <span class="required">*</span>选择奶罐
              </label>
              <div class="form-select-wrapper">
                <select
                  v-model="startForm.tankId"
                  class="form-input"
                  :class="{ error: startErrors.tankId }">
                  <option value="">请选择待清洗的奶罐</option>
                  <option
                    v-for="tank in dirtyTanks"
                    :key="tank.id"
                    :value="tank.id">
                    {{ tank.tankCode }} - {{ tank.tankName }} - {{ tank.tankStatus | formatStatus('tank') }}
                  </option>
                </select>
              </div>
              <div v-if="startErrors.tankId" class="error-msg">{{ startErrors.tankId }}</div>
            </div>

            <div class="form-item">
              <label class="form-label">清洗方式</label>
              <div class="form-select-wrapper">
                <select
                  v-model="startForm.cleaningMethod"
                  class="form-input">
                  <option value="">请选择清洗方式</option>
                  <option value="高温消毒">高温消毒</option>
                  <option value="化学清洗">化学清洗</option>
                  <option value="CIP清洗">CIP清洗</option>
                  <option value="人工清洗">人工清洗</option>
                </select>
              </div>
            </div>

            <div class="form-item">
              <label class="form-label">清洗剂</label>
              <input
                v-model="startForm.cleaningAgent"
                type="text"
                class="form-input"
                placeholder="请输入清洗剂（选填）" />
            </div>

            <div class="form-item">
              <label class="form-label">
                <span class="required">*</span>开始时间
              </label>
              <input
                v-model="startForm.startTime"
                type="datetime-local"
                class="form-input"
                :class="{ error: startErrors.startTime }" />
              <div v-if="startErrors.startTime" class="error-msg">{{ startErrors.startTime }}</div>
            </div>

            <button type="submit" class="mobile-btn btn-primary" :disabled="startLoading">
              {{ startLoading ? '提交中...' : '开始清洗' }}
            </button>
          </form>
        </div>
      </div>
    </div>

    <div v-if="showFinishDialog" class="mobile-container" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; z-index: 200; background: rgba(0,0,0,0.5);">
      <div style="position: absolute; bottom: 0; left: 0; right: 0; background: #fff; border-radius: 16px 16px 0 0; max-height: 90vh; overflow-y: auto;">
        <div class="mobile-header" style="border-radius: 16px 16px 0 0;">
          <div class="header-back" @click="showFinishDialog = false">
            <i class="el-icon-close"></i>
          </div>
          <div class="header-title">完成清洗</div>
          <div class="header-action"></div>
        </div>
        <div class="mobile-content">
          <form class="mobile-form" @submit.prevent="submitFinish">
            <div class="form-item">
              <label class="form-label">
                <span class="required">*</span>水温(°C)
              </label>
              <input
                v-model.number="finishForm.waterTemperature"
                type="number"
                min="0"
                max="100"
                step="0.1"
                class="form-input"
                placeholder="请输入水温"
                :class="{ error: finishErrors.waterTemperature }" />
              <div v-if="finishErrors.waterTemperature" class="error-msg">{{ finishErrors.waterTemperature }}</div>
            </div>

            <div class="form-item">
              <label class="form-label">
                <span class="required">*</span>是否合格
              </label>
              <div style="display: flex; gap: 20px; padding: 8px 0;">
                <label style="display: flex; align-items: center; cursor: pointer;">
                  <input
                    type="radio"
                    v-model="finishForm.isQualified"
                    :value="1"
                    style="width: 20px; height: 20px; margin-right: 8px;" />
                  <span style="font-size: 16px;">合格</span>
                </label>
                <label style="display: flex; align-items: center; cursor: pointer;">
                  <input
                    type="radio"
                    v-model="finishForm.isQualified"
                    :value="0"
                    style="width: 20px; height: 20px; margin-right: 8px;" />
                  <span style="font-size: 16px;">不合格</span>
                </label>
              </div>
              <div v-if="finishErrors.isQualified" class="error-msg">{{ finishErrors.isQualified }}</div>
            </div>

            <div class="form-item">
              <label class="form-label">
                <span class="required">*</span>结束时间
              </label>
              <input
                v-model="finishForm.endTime"
                type="datetime-local"
                class="form-input"
                :class="{ error: finishErrors.endTime }" />
              <div v-if="finishErrors.endTime" class="error-msg">{{ finishErrors.endTime }}</div>
            </div>

            <div class="form-item">
              <label class="form-label">备注</label>
              <textarea
                v-model="finishForm.remark"
                class="form-textarea"
                placeholder="请输入备注（选填）"></textarea>
            </div>

            <button type="submit" class="mobile-btn btn-primary" :disabled="finishLoading">
              {{ finishLoading ? '提交中...' : '确认完成' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getCleaningList, startCleaning, finishCleaning } from '@/api/cleaning'
import { getTankList } from '@/api/tank'

export default {
  name: 'MobileCleaningList',
  data() {
    return {
      loading: false,
      loadingMore: false,
      startLoading: false,
      finishLoading: false,
      list: [],
      tankList: [],
      dirtyTanks: [],
      activeTab: 'all',
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      hasMore: true,
      showStartDialog: false,
      showFinishDialog: false,
      currentCleaning: null,
      tabs: [
        { label: '全部', value: 'all' },
        { label: '清洗中', value: 'cleaning' },
        { label: '已合格', value: 'qualified' },
        { label: '不合格', value: 'unqualified' }
      ],
      startForm: {
        tankId: null,
        cleaningMethod: '',
        cleaningAgent: '',
        startTime: ''
      },
      startErrors: {},
      finishForm: {
        waterTemperature: null,
        isQualified: null,
        endTime: '',
        remark: ''
      },
      finishErrors: {}
    }
  },
  computed: {
    filteredList() {
      let result = this.list
      
      if (this.activeTab === 'cleaning') {
        result = result.filter(item => item.isQualified === null)
      } else if (this.activeTab === 'qualified') {
        result = result.filter(item => item.isQualified === 1)
      } else if (this.activeTab === 'unqualified') {
        result = result.filter(item => item.isQualified === 0)
      }
      
      return result
    }
  },
  mounted() {
    this.loadData()
    this.loadTanks()
    this.setDefaultStartTime()
    this.setDefaultEndTime()
  },
  methods: {
    setDefaultStartTime() {
      const now = new Date()
      this.startForm.startTime = this.formatDateTimeLocal(now)
    },
    setDefaultEndTime() {
      const now = new Date()
      this.finishForm.endTime = this.formatDateTimeLocal(now)
    },
    formatDateTimeLocal(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day}T${hours}:${minutes}`
    },
    async loadData() {
      this.loading = true
      this.pagination.pageNum = 1
      this.hasMore = true
      try {
        const params = {
          pageNum: this.pagination.pageNum,
          pageSize: this.pagination.pageSize
        }
        const res = await getCleaningList(params)
        this.list = res.data?.records || []
        this.pagination.total = res.data?.total || 0
        this.hasMore = this.list.length < this.pagination.total
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    async loadMore() {
      if (this.loadingMore || !this.hasMore) return
      
      this.loadingMore = true
      this.pagination.pageNum++
      try {
        const params = {
          pageNum: this.pagination.pageNum,
          pageSize: this.pagination.pageSize
        }
        const res = await getCleaningList(params)
        const records = res.data?.records || []
        this.list = [...this.list, ...records]
        this.hasMore = this.list.length < this.pagination.total
      } catch (error) {
        console.error('加载更多失败:', error)
        this.pagination.pageNum--
      } finally {
        this.loadingMore = false
      }
    },
    async loadTanks() {
      try {
        const res = await getTankList({ pageNum: 1, pageSize: 100 })
        this.tankList = res.data?.records || []
        this.dirtyTanks = this.tankList.filter(t => t.tankStatus === 'PENDING_CLEAN' || t.tankStatus === 'IDLE')
      } catch (error) {
        console.error('加载奶罐列表失败:', error)
      }
    },
    switchTab(tab) {
      this.activeTab = tab
    },
    getCleaningStatusClass(isQualified) {
      if (isQualified === null) return 'tag-warning'
      if (isQualified === 1) return 'tag-success'
      return 'tag-danger'
    },
    getCleaningStatusText(isQualified) {
      if (isQualified === null) return '清洗中'
      if (isQualified === 1) return '清洗合格'
      return '清洗不合格'
    },
    validateStart() {
      this.startErrors = {}
      if (!this.startForm.tankId) {
        this.startErrors.tankId = '请选择奶罐'
      }
      if (!this.startForm.startTime) {
        this.startErrors.startTime = '请选择开始时间'
      }
      return Object.keys(this.startErrors).length === 0
    },
    async submitStart() {
      if (!this.validateStart()) return
      
      this.startLoading = true
      try {
        const submitData = {
          ...this.startForm,
          startTime: new Date(this.startForm.startTime).toISOString()
        }
        await startCleaning(submitData)
        this.$message.success('开始清洗成功')
        this.showStartDialog = false
        this.loadData()
        this.loadTanks()
      } catch (error) {
        console.error('开始清洗失败:', error)
      } finally {
        this.startLoading = false
      }
    },
    finishCleaning(row) {
      this.currentCleaning = row
      this.finishForm = {
        waterTemperature: null,
        isQualified: null,
        endTime: this.formatDateTimeLocal(new Date()),
        remark: ''
      }
      this.finishErrors = {}
      this.showFinishDialog = true
    },
    validateFinish() {
      this.finishErrors = {}
      if (this.finishForm.waterTemperature === null || this.finishForm.waterTemperature < 0 || this.finishForm.waterTemperature > 100) {
        this.finishErrors.waterTemperature = '请输入有效的水温(0-100°C)'
      }
      if (this.finishForm.isQualified === null) {
        this.finishErrors.isQualified = '请选择是否合格'
      }
      if (!this.finishForm.endTime) {
        this.finishErrors.endTime = '请选择结束时间'
      }
      return Object.keys(this.finishErrors).length === 0
    },
    async submitFinish() {
      if (!this.validateFinish()) return
      
      this.finishLoading = true
      try {
        const submitData = {
          ...this.finishForm,
          endTime: new Date(this.finishForm.endTime).toISOString()
        }
        await finishCleaning(this.currentCleaning.id, submitData)
        this.$message.success(this.finishForm.isQualified === 1 ? '清洗合格，奶罐已可用' : '清洗不合格，请重新清洗')
        this.showFinishDialog = false
        this.loadData()
        this.loadTanks()
      } catch (error) {
        console.error('完成清洗失败:', error)
      } finally {
        this.finishLoading = false
      }
    }
  }
}
</script>
