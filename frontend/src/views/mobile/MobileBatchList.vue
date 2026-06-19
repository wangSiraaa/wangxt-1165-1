<template>
  <div class="mobile-container">
    <div class="mobile-header">
      <div class="header-back" @click="$router.back()">
        <i class="el-icon-arrow-left"></i>
      </div>
      <div class="header-title">批次列表</div>
      <div class="header-action" @click="refresh">
        <i class="el-icon-refresh"></i>
      </div>
    </div>

    <div class="mobile-content">
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

      <div class="mobile-search">
        <i class="el-icon-search search-icon"></i>
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          placeholder="搜索批次号、奶罐..."
          @input="handleSearch" />
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
        <div class="empty-icon">📋</div>
        <div class="empty-text">暂无数据</div>
      </div>

      <div v-else>
        <div
          v-for="item in filteredList"
          :key="item.id"
          class="mobile-card"
          @click="viewDetail(item)">
          <div class="mobile-card-header">
            <div class="card-title">{{ item.batchNo }}</div>
            <span
              class="mobile-tag"
              :class="getStatusTagClass(item.batchStatus)">
              {{ item.batchStatus | formatStatus('batch') }}
            </span>
          </div>
          <div class="mobile-card-content">
            <div class="info-row">
              <span class="label">奶罐</span>
              <span class="value">{{ item.milkTankName }}</span>
            </div>
            <div class="info-row">
              <span class="label">奶量</span>
              <span class="value">{{ item.milkVolume }}kg</span>
            </div>
            <div class="info-row">
              <span class="label">温度</span>
              <span class="value">{{ item.milkTemperature }}°C</span>
            </div>
            <div class="info-row">
              <span class="label">挤奶员</span>
              <span class="value">{{ item.milker }}</span>
            </div>
            <div class="info-row">
              <span class="label">创建时间</span>
              <span class="value">{{ item.createTime | formatDate('YYYY-MM-DD HH:mm') }}</span>
            </div>
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

    <div v-if="showDetail && currentBatch" class="mobile-container" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; z-index: 200; background: #f5f7fa;">
      <div class="mobile-header">
        <div class="header-back" @click="showDetail = false">
          <i class="el-icon-arrow-left"></i>
        </div>
        <div class="header-title">批次详情</div>
        <div class="header-action"></div>
      </div>
      <div class="mobile-content">
        <div class="mobile-card">
          <div class="mobile-card-header">
            <div class="card-title">{{ currentBatch.batchNo }}</div>
            <span
              class="mobile-tag"
              :class="getStatusTagClass(currentBatch.batchStatus)">
              {{ currentBatch.batchStatus | formatStatus('batch') }}
            </span>
          </div>
          <div class="mobile-card-content">
            <div class="info-row">
              <span class="label">奶罐</span>
              <span class="value">{{ currentBatch.milkTankName }}</span>
            </div>
            <div class="info-row">
              <span class="label">奶量</span>
              <span class="value">{{ currentBatch.milkVolume }}kg</span>
            </div>
            <div class="info-row">
              <span class="label">温度</span>
              <span class="value">{{ currentBatch.milkTemperature }}°C</span>
            </div>
            <div class="info-row">
              <span class="label">挤奶员</span>
              <span class="value">{{ currentBatch.milker }}</span>
            </div>
            <div class="info-row" v-if="currentBatch.cowGroupNo">
              <span class="label">牛群编号</span>
              <span class="value">{{ currentBatch.cowGroupNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">挤奶开始</span>
              <span class="value">{{ currentBatch.milkStartTime | formatDate('YYYY-MM-DD HH:mm') }}</span>
            </div>
            <div class="info-row">
              <span class="label">挤奶结束</span>
              <span class="value">{{ currentBatch.milkEndTime | formatDate('YYYY-MM-DD HH:mm') }}</span>
            </div>
            <div class="info-row">
              <span class="label">创建时间</span>
              <span class="value">{{ currentBatch.createTime | formatDate('YYYY-MM-DD HH:mm') }}</span>
            </div>
            <div class="info-row" v-if="currentBatch.remark">
              <span class="label">备注</span>
              <span class="value">{{ currentBatch.remark }}</span>
            </div>
          </div>
        </div>

        <div class="mobile-alert alert-info">
          <p><strong>业务规则：</strong></p>
          <p>• 检测合格的批次才能装车运输</p>
          <p>• 检测不合格的批次将被拒收</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getBatchList } from '@/api/batch'

export default {
  name: 'MobileBatchList',
  data() {
    return {
      loading: false,
      loadingMore: false,
      list: [],
      searchKeyword: '',
      activeTab: 'all',
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      hasMore: true,
      showDetail: false,
      currentBatch: null,
      tabs: [
        { label: '全部', value: 'all' },
        { label: '待检测', value: 'PENDING_TEST' },
        { label: '检测通过', value: 'TEST_PASSED' },
        { label: '检测不合格', value: 'TEST_FAILED' },
        { label: '已装车', value: 'LOADED' }
      ]
    }
  },
  computed: {
    filteredList() {
      let result = this.list
      
      if (this.activeTab !== 'all') {
        result = result.filter(item => item.batchStatus === this.activeTab)
      }
      
      if (this.searchKeyword.trim()) {
        const keyword = this.searchKeyword.trim().toLowerCase()
        result = result.filter(item =>
          item.batchNo?.toLowerCase().includes(keyword) ||
          item.milkTankName?.toLowerCase().includes(keyword) ||
          item.milker?.toLowerCase().includes(keyword)
        )
      }
      
      return result
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      this.pagination.pageNum = 1
      this.hasMore = true
      try {
        const params = {
          pageNum: this.pagination.pageNum,
          pageSize: this.pagination.pageSize
        }
        const res = await getBatchList(params)
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
        const res = await getBatchList(params)
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
    refresh() {
      this.loadData()
    },
    switchTab(tab) {
      this.activeTab = tab
    },
    handleSearch() {
    },
    getStatusTagClass(status) {
      const map = {
        MILKING: 'tag-primary',
        PENDING_TEST: 'tag-warning',
        TEST_PASSED: 'tag-success',
        TEST_FAILED: 'tag-danger',
        LOADED: 'tag-success',
        REJECTED: 'tag-danger'
      }
      return map[status] || 'tag-info'
    },
    viewDetail(item) {
      this.currentBatch = item
      this.showDetail = true
    }
  }
}
</script>
