<template>
  <div class="mobile-container">
    <div class="mobile-header">
      <div class="header-back" @click="$router.back()">
        <i class="el-icon-arrow-left"></i>
      </div>
      <div class="header-title">装车记录</div>
      <div class="header-action" @click="refresh">
        <i class="el-icon-refresh"></i>
      </div>
    </div>

    <div class="mobile-content">
      <div class="mobile-search">
        <i class="el-icon-search search-icon"></i>
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          placeholder="搜索车牌号、批次号..."
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
        <div class="empty-icon">🚚</div>
        <div class="empty-text">暂无装车记录</div>
      </div>

      <div v-else>
        <div
          v-for="item in filteredList"
          :key="item.id"
          class="mobile-card"
          @click="viewDetail(item)">
          <div class="mobile-card-header">
            <div class="card-title">{{ item.loadingNo }}</div>
            <span
              class="mobile-tag"
              :class="item.testResult === 'PASSED' ? 'tag-success' : 'tag-danger'">
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
              <span class="value">{{ item.milkTankName }}</span>
            </div>
            <div class="info-row">
              <span class="label">车牌号</span>
              <span class="value">{{ item.plateNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">司机</span>
              <span class="value">{{ item.driverName }}</span>
            </div>
            <div class="info-row">
              <span class="label">装车量</span>
              <span class="value">{{ item.loadedVolume }}kg</span>
            </div>
            <div class="info-row">
              <span class="label">装车时间</span>
              <span class="value">{{ item.loadingTime | formatDate('YYYY-MM-DD HH:mm') }}</span>
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

    <div v-if="showDetail && currentLoading" class="mobile-container" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; z-index: 200; background: #f5f7fa;">
      <div class="mobile-header">
        <div class="header-back" @click="showDetail = false">
          <i class="el-icon-arrow-left"></i>
        </div>
        <div class="header-title">装车详情</div>
        <div class="header-action"></div>
      </div>
      <div class="mobile-content">
        <div class="mobile-card">
          <div class="mobile-card-header">
            <div class="card-title">{{ currentLoading.loadingNo }}</div>
            <span
              class="mobile-tag"
              :class="currentLoading.testResult === 'PASSED' ? 'tag-success' : 'tag-danger'">
              {{ currentLoading.testResult | formatStatus('test') }}
            </span>
          </div>
          <div class="mobile-card-content">
            <div class="info-row">
              <span class="label">批次号</span>
              <span class="value">{{ currentLoading.batchNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">奶罐</span>
              <span class="value">{{ currentLoading.milkTankName }}</span>
            </div>
            <div class="info-row">
              <span class="label">车牌号</span>
              <span class="value">{{ currentLoading.plateNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">司机</span>
              <span class="value">{{ currentLoading.driverName }}</span>
            </div>
            <div class="info-row">
              <span class="label">装车量</span>
              <span class="value">{{ currentLoading.loadedVolume }}kg</span>
            </div>
            <div class="info-row">
              <span class="label">检测结果</span>
              <span class="value">
                <span
                  class="mobile-tag"
                  :class="currentLoading.testResult === 'PASSED' ? 'tag-success' : 'tag-danger'">
                  {{ currentLoading.testResult | formatStatus('test') }}
                </span>
              </span>
            </div>
            <div class="info-row" v-if="currentLoading.destination">
              <span class="label">运输目的地</span>
              <span class="value">{{ currentLoading.destination }}</span>
            </div>
            <div class="info-row" v-if="currentLoading.sealNo">
              <span class="label">封铅号</span>
              <span class="value">{{ currentLoading.sealNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">装车时间</span>
              <span class="value">{{ currentLoading.loadingTime | formatDate('YYYY-MM-DD HH:mm') }}</span>
            </div>
            <div class="info-row" v-if="currentLoading.remark">
              <span class="label">备注</span>
              <span class="value">{{ currentLoading.remark }}</span>
            </div>
          </div>
        </div>

        <div class="mobile-alert alert-info">
          <p><strong>业务规则：</strong></p>
          <p>• 装车确认后，检测结果已锁定，不能修改为合格</p>
          <p>• 装车后奶罐状态自动变为待清洗</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getLoadingList } from '@/api/loading'

export default {
  name: 'MobileLoadingList',
  data() {
    return {
      loading: false,
      loadingMore: false,
      list: [],
      searchKeyword: '',
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      hasMore: true,
      showDetail: false,
      currentLoading: null
    }
  },
  computed: {
    filteredList() {
      if (!this.searchKeyword.trim()) {
        return this.list
      }
      
      const keyword = this.searchKeyword.trim().toLowerCase()
      return this.list.filter(item =>
        item.loadingNo?.toLowerCase().includes(keyword) ||
        item.batchNo?.toLowerCase().includes(keyword) ||
        item.plateNo?.toLowerCase().includes(keyword) ||
        item.driverName?.toLowerCase().includes(keyword)
      )
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
        const res = await getLoadingList(params)
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
        const res = await getLoadingList(params)
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
    viewDetail(item) {
      this.currentLoading = item
      this.showDetail = true
    }
  }
}
</script>
