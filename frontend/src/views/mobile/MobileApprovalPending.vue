<template>
  <div class="mobile-container">
    <div class="mobile-header">
      <div class="header-back" @click="$router.back()">
        <i class="el-icon-arrow-left"></i>
      </div>
      <div class="header-title">我的待审批</div>
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
          placeholder="搜索审批单号、批次号..."
          @input="handleSearch" />
      </div>

      <div v-if="loading" class="mobile-loading">
        <div class="loading-spinner"></div>
        <div>加载中...</div>
      </div>

      <div v-else-if="filteredList.length === 0" class="mobile-empty">
        <div class="empty-icon">📋</div>
        <div class="empty-text">暂无待审批</div>
      </div>

      <div v-else>
        <div
          v-for="item in filteredList"
          :key="item.id"
          class="mobile-card"
          @click="viewDetail(item)">
          <div class="mobile-card-header">
            <div class="card-title">{{ item.approvalNo }}</div>
            <span
              class="mobile-tag"
              :class="getPriorityTagClass(item.priority)">
              {{ item.priority | formatStatus('priority') }}
            </span>
          </div>
          <div class="mobile-card-content">
            <div class="info-row">
              <span class="label">审批类型</span>
              <span class="value">
                <span class="mobile-tag" :class="getApprovalTypeTagClass(item.approvalType)">
                  {{ item.approvalType | formatStatus('approvalType') }}
                </span>
              </span>
            </div>
            <div class="info-row">
              <span class="label">关联批次</span>
              <span class="value">{{ item.batchNo || '-' }}</span>
            </div>
            <div class="info-row" v-if="item.milkTankName">
              <span class="label">奶罐</span>
              <span class="value">{{ item.milkTankName }}</span>
            </div>
            <div class="info-row">
              <span class="label">检测结果</span>
              <span class="value">
                <span
                  v-if="item.testResult"
                  class="mobile-tag"
                  :class="item.testResult === 'PASSED' ? 'tag-success' : 'tag-danger'">
                  {{ item.testResult | formatStatus('test') }}
                </span>
                <span v-else>-</span>
              </span>
            </div>
            <div class="info-row">
              <span class="label">申请人</span>
              <span class="value">{{ item.applicantName }}</span>
            </div>
            <div class="info-row">
              <span class="label">申请时间</span>
              <span class="value">{{ item.applyTime | formatDate('MM-DD HH:mm') }}</span>
            </div>
          </div>
          <div class="mobile-card-footer">
            <el-button
              size="mini"
              type="success"
              plain
              @click.stop="handleSingleApprove(item)">
              通过
            </el-button>
            <el-button
              size="mini"
              type="danger"
              plain
              @click.stop="handleSingleReject(item)">
              驳回
            </el-button>
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

    <el-dialog title="审批意见" :visible.sync="opinionVisible" width="90%">
      <el-form :model="opinionForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input
            type="textarea"
            v-model="opinionForm.comment"
            :rows="4"
            :maxlength="500"
            show-word-limit
            placeholder="请输入审批意见（驳回时必填）">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="opinionVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmOpinion">确认</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyPending, approve, reject } from '@/api/approval'

export default {
  name: 'MobileApprovalPending',
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
      opinionVisible: false,
      opinionForm: {
        type: '',
        id: '',
        comment: ''
      },
      tabs: [
        { label: '全部', value: 'all' },
        { label: '紧急', value: 'URGENT' },
        { label: '批次检测', value: 'BATCH_TEST' },
        { label: '装车确认', value: 'LOADING_CONFIRM' },
        { label: '奶罐清洗', value: 'TANK_CLEANING' }
      ]
    }
  },
  computed: {
    filteredList() {
      let result = this.list
      
      if (this.activeTab !== 'all') {
        if (this.activeTab === 'URGENT') {
          result = result.filter(item => item.priority === 'URGENT')
        } else {
          result = result.filter(item => item.approvalType === this.activeTab)
        }
      }
      
      if (this.searchKeyword.trim()) {
        const keyword = this.searchKeyword.trim().toLowerCase()
        result = result.filter(item =>
          item.approvalNo?.toLowerCase().includes(keyword) ||
          item.batchNo?.toLowerCase().includes(keyword) ||
          item.applicantName?.toLowerCase().includes(keyword)
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
        const res = await getMyPending(params)
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
        const res = await getMyPending(params)
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
    getPriorityTagClass(priority) {
      const map = {
        URGENT: 'tag-danger',
        HIGH: 'tag-warning',
        NORMAL: 'tag-primary',
        LOW: 'tag-info'
      }
      return map[priority] || 'tag-info'
    },
    getApprovalTypeTagClass(type) {
      const map = {
        BATCH_TEST: 'tag-primary',
        LOADING_CONFIRM: 'tag-success',
        TANK_CLEANING: 'tag-warning',
        EXCEPTION_HANDLE: 'tag-danger'
      }
      return map[type] || 'tag-info'
    },
    viewDetail(item) {
      this.$router.push('/m/approval/' + item.id)
    },
    handleSingleApprove(item) {
      this.$confirm('确定要通过该审批吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.opinionForm = { type: 'approve', id: item.id, comment: '' }
        this.opinionVisible = true
      }).catch(() => {})
    },
    handleSingleReject(item) {
      this.opinionForm = { type: 'reject', id: item.id, comment: '' }
      this.opinionVisible = true
    },
    async confirmOpinion() {
      if (this.opinionForm.type === 'reject' && !this.opinionForm.comment.trim()) {
        this.$message.warning('请输入驳回意见')
        return
      }

      const action = this.opinionForm.type === 'approve' ? approve : reject
      const actionText = this.opinionForm.type === 'approve' ? '通过' : '驳回'

      try {
        this.loading = true
        await action({
          id: this.opinionForm.id,
          comment: this.opinionForm.comment
        })
        this.$message.success('审批' + actionText + '成功')
        this.opinionVisible = false
        this.loadData()
      } catch (error) {
        console.error('审批失败:', error)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.mobile-card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

::v-deep .el-dialog {
  margin-top: 30vh !important;
}
</style>
