<template>
  <div class="mobile-container">
    <div class="mobile-header">
      <div class="header-back" @click="$router.back()">
        <i class="el-icon-arrow-left"></i>
      </div>
      <div class="header-title">检测结果</div>
      <div class="header-action" @click="refresh">
        <i class="el-icon-refresh"></i>
      </div>
    </div>

    <div class="mobile-content">
      <div class="mobile-alert alert-info">
        <p><strong>业务规则提醒：</strong></p>
        <p>• 快检不合格的批次不能装车</p>
        <p>• 检测结果录入后2小时内可修改，超时自动锁定</p>
        <p>• 装车确认后，检测结果将被锁定，不能修改为合格</p>
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

      <div class="mobile-search">
        <i class="el-icon-search search-icon"></i>
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          placeholder="搜索批次号、检测编号..."
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
        <div class="empty-icon">🧪</div>
        <div class="empty-text">暂无检测记录</div>
      </div>

      <div v-else>
        <div
          v-for="item in filteredList"
          :key="item.id"
          class="mobile-card"
          @click="viewDetail(item)">
          <div class="mobile-card-header">
            <div class="card-title">{{ item.testNo || ('检测-' + (item.batchNo || '')) }}</div>
            <span
              class="mobile-tag"
              :class="item.testResult === 'PASSED' ? 'tag-success' : item.testResult === 'FAILED' ? 'tag-danger' : 'tag-warning'">
              {{ item.testResult | formatStatus('test') }}
            </span>
          </div>
          <div class="mobile-card-content">
            <div class="info-row">
              <span class="label">批次号</span>
              <span class="value">{{ item.batchNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">奶罐</span>
              <span class="value">{{ item.milkTankName || item.tankName }}</span>
            </div>
            <div class="info-row">
              <span class="label">检测项目</span>
              <span class="value">{{ item.testItem || '抗生素检测' }}</span>
            </div>
            <div class="info-row">
              <span class="label">检测方法</span>
              <span class="value">{{ item.testMethod || '-' }}</span>
            </div>
            <div class="info-row" v-if="item.antibioticType">
              <span class="label">抗生素类型</span>
              <span class="value">{{ item.antibioticType }}</span>
            </div>
            <div class="info-row" v-if="item.testValue !== null && item.testValue !== undefined">
              <span class="label">检测值</span>
              <span class="value">{{ item.testValue }}{{ item.testUnit || 'ng/mL' }}</span>
            </div>
            <div class="info-row">
              <span class="label">检测时间</span>
              <span class="value">{{ item.testTime | formatDate('YYYY-MM-DD HH:mm') }}</span>
            </div>
          </div>
          <div class="mobile-card-footer" v-if="item.testResult === 'FAILED'">
            <span style="color: #f56c6c; font-size: 14px;">⚠️ 检测不合格，禁止装车</span>
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

    <div v-if="showDetail && currentTest" class="mobile-container" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; z-index: 200; background: #f5f7fa;">
      <div class="mobile-header">
        <div class="header-back" @click="showDetail = false">
          <i class="el-icon-arrow-left"></i>
        </div>
        <div class="header-title">检测详情</div>
        <div class="header-action"></div>
      </div>
      <div class="mobile-content">
        <div class="mobile-card">
          <div class="mobile-card-header">
            <div class="card-title">{{ currentTest.testNo || ('检测-' + (currentTest.batchNo || '')) }}</div>
            <span
              class="mobile-tag"
              :class="currentTest.testResult === 'PASSED' ? 'tag-success' : currentTest.testResult === 'FAILED' ? 'tag-danger' : 'tag-warning'">
              {{ currentTest.testResult | formatStatus('test') }}
            </span>
          </div>
          <div class="mobile-card-content">
            <div class="info-row">
              <span class="label">批次号</span>
              <span class="value">{{ currentTest.batchNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">奶罐</span>
              <span class="value">{{ currentTest.milkTankName || currentTest.tankName }}</span>
            </div>
            <div class="info-row">
              <span class="label">检测项目</span>
              <span class="value">{{ currentTest.testItem || '抗生素检测' }}</span>
            </div>
            <div class="info-row">
              <span class="label">检测方法</span>
              <span class="value">{{ currentTest.testMethod || '-' }}</span>
            </div>
            <div class="info-row" v-if="currentTest.antibioticType">
              <span class="label">抗生素类型</span>
              <span class="value">{{ currentTest.antibioticType }}</span>
            </div>
            <div class="info-row" v-if="currentTest.testValue !== null && currentTest.testValue !== undefined">
              <span class="label">检测值</span>
              <span class="value">{{ currentTest.testValue }}{{ currentTest.testUnit || 'ng/mL' }}</span>
            </div>
            <div class="info-row" v-if="currentTest.standardValue">
              <span class="label">标准值</span>
              <span class="value">≤{{ currentTest.standardValue }}{{ currentTest.testUnit || 'ng/mL' }}</span>
            </div>
            <div class="info-row">
              <span class="label">检测员</span>
              <span class="value">{{ currentTest.testerName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">检测时间</span>
              <span class="value">{{ currentTest.testTime | formatDate('YYYY-MM-DD HH:mm') }}</span>
            </div>
            <div class="info-row" v-if="currentTest.testResult === 'FAILED'">
              <span class="label">是否锁定</span>
              <span class="value">
                <span class="mobile-tag" :class="currentTest.isLocked ? 'tag-danger' : 'tag-success'">
                  {{ currentTest.isLocked ? '已锁定' : '未锁定' }}
                </span>
              </span>
            </div>
            <div class="info-row" v-if="currentTest.equipmentNo">
              <span class="label">设备编号</span>
              <span class="value">{{ currentTest.equipmentNo }}</span>
            </div>
            <div class="info-row" v-if="currentTest.remark">
              <span class="label">备注</span>
              <span class="value">{{ currentTest.remark }}</span>
            </div>
          </div>
        </div>

        <div class="mobile-alert alert-info">
          <p><strong>业务规则：</strong></p>
          <p>• 快检不合格的批次不能装车</p>
          <p>• 装车确认后，检测结果将被锁定</p>
          <p>• 检测结果2小时内可修改，超时自动锁定</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getTestList } from '@/api/test'

export default {
  name: 'MobileTestList',
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
      currentTest: null,
      tabs: [
        { label: '全部', value: 'all' },
        { label: '待检测', value: 'PENDING' },
        { label: '检测合格', value: 'PASSED' },
        { label: '检测不合格', value: 'FAILED' }
      ]
    }
  },
  computed: {
    filteredList() {
      let result = this.list
      
      if (this.activeTab !== 'all') {
        result = result.filter(item => item.testResult === this.activeTab || item.status === this.activeTab)
      }
      
      if (this.searchKeyword.trim()) {
        const keyword = this.searchKeyword.trim().toLowerCase()
        result = result.filter(item =>
          item.batchNo?.toLowerCase().includes(keyword) ||
          item.testNo?.toLowerCase().includes(keyword) ||
          item.milkTankName?.toLowerCase().includes(keyword)
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
        const res = await getTestList(params)
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
        const res = await getTestList(params)
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
    handleSearch() {
    },
    switchTab(tab) {
      this.activeTab = tab
    },
    viewDetail(item) {
      this.currentTest = item
      this.showDetail = true
    }
  }
}
</script>
