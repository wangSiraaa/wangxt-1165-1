<template>
  <div class="mobile-container">
    <div class="mobile-header">
      <div class="header-back" @click="$router.back()">
        <i class="el-icon-arrow-left"></i>
      </div>
      <div class="header-title">审批详情</div>
      <div class="header-action" @click="refresh">
        <i class="el-icon-refresh"></i>
      </div>
    </div>

    <div class="mobile-content" v-loading="loading">
      <div class="mobile-card" v-if="approvalDetail">
        <div class="mobile-card-header">
          <div class="card-title">{{ approvalDetail.approvalNo }}</div>
          <span
            class="mobile-tag"
            :class="getStatusTagClass(approvalDetail.status)">
            {{ approvalDetail.status | formatStatus('approvalStatus') }}
          </span>
        </div>
        <div class="mobile-card-content">
          <div class="info-row">
            <span class="label">审批类型</span>
            <span class="value">
              <span class="mobile-tag" :class="getApprovalTypeTagClass(approvalDetail.approvalType)">
                {{ approvalDetail.approvalType | formatStatus('approvalType') }}
              </span>
            </span>
          </div>
          <div class="info-row">
            <span class="label">优先级</span>
            <span class="value">
              <span class="mobile-tag" :class="getPriorityTagClass(approvalDetail.priority)">
                {{ approvalDetail.priority | formatStatus('priority') }}
              </span>
            </span>
          </div>
          <div class="info-row">
            <span class="label">申请人</span>
            <span class="value">{{ approvalDetail.applicantName }}</span>
          </div>
          <div class="info-row">
            <span class="label">申请时间</span>
            <span class="value">{{ approvalDetail.applyTime | formatDate('YYYY-MM-DD HH:mm') }}</span>
          </div>
          <div class="info-row">
            <span class="label">当前节点</span>
            <span class="value">{{ approvalDetail.currentNodeName || '-' }}</span>
          </div>
          <div class="info-row" v-if="approvalDetail.applyReason">
            <span class="label">申请理由</span>
            <span class="value">{{ approvalDetail.applyReason }}</span>
          </div>
        </div>
      </div>

      <div class="mobile-card" v-if="businessData">
        <div class="mobile-card-header">
          <div class="card-title">关联业务单据</div>
        </div>
        <div class="mobile-card-content">
          <div class="info-row" v-if="businessData.batchNo">
            <span class="label">批次号</span>
            <span class="value">{{ businessData.batchNo }}</span>
          </div>
          <div class="info-row" v-if="businessData.milkTankName">
            <span class="label">奶罐号</span>
            <span class="value">{{ businessData.milkTankName }}</span>
          </div>
          <div class="info-row" v-if="businessData.milkVolume">
            <span class="label">奶量</span>
            <span class="value">{{ businessData.milkVolume }}kg</span>
          </div>
          <div class="info-row" v-if="businessData.milkTemperature">
            <span class="label">温度</span>
            <span class="value">{{ businessData.milkTemperature }}°C</span>
          </div>
          <div class="info-row" v-if="businessData.milker">
            <span class="label">挤奶员</span>
            <span class="value">{{ businessData.milker }}</span>
          </div>
          <div class="info-row" v-if="businessData.testResult">
            <span class="label">检测结果</span>
            <span class="value">
              <span
                class="mobile-tag"
                :class="businessData.testResult === 'PASSED' ? 'tag-success' : 'tag-danger'">
                {{ businessData.testResult | formatStatus('test') }}
              </span>
            </span>
          </div>
          <div class="info-row" v-if="businessData.antibioticTest">
            <span class="label">抗生素检测</span>
            <span class="value">
              <span
                class="mobile-tag"
                :class="businessData.antibioticTest === 'NEGATIVE' ? 'tag-success' : 'tag-danger'">
                {{ businessData.antibioticTest === 'NEGATIVE' ? '阴性' : '阳性' }}
              </span>
            </span>
          </div>
          <div class="info-row" v-if="businessData.testTime">
            <span class="label">检测时间</span>
            <span class="value">{{ businessData.testTime | formatDate('YYYY-MM-DD HH:mm') }}</span>
          </div>
        </div>
      </div>

      <div class="mobile-card" v-if="approvalNodes.length > 0">
        <div class="mobile-card-header">
          <div class="card-title">审批流程</div>
        </div>
        <div class="mobile-card-content">
          <div class="approval-flow-mobile">
            <div
              v-for="(node, index) in approvalNodes"
              :key="index"
              class="flow-item">
              <div class="flow-left">
                <div class="flow-dot" :class="getFlowDotClass(node.status)">
                  <i :class="getFlowIcon(node.status)"></i>
                </div>
                <div class="flow-line" v-if="index < approvalNodes.length - 1" :class="getFlowLineClass(node.status)"></div>
              </div>
              <div class="flow-right">
                <div class="flow-header">
                  <span class="flow-node-name">{{ node.nodeName }}</span>
                  <span class="mobile-tag" :class="getNodeStatusTagClass(node.status)">
                    {{ node.status | formatStatus('nodeStatus') }}
                  </span>
                </div>
                <div class="flow-time" v-if="node.handleTime">
                  {{ node.handleTime | formatDate('YYYY-MM-DD HH:mm') }}
                </div>
                <div class="flow-handler" v-if="node.handlerName && node.status !== 'PENDING'">
                  处理人：{{ node.handlerName }}
                </div>
                <div class="flow-comment" v-if="node.comment">
                  意见：{{ node.comment }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="mobile-card" v-if="approvalHistory.length > 0">
        <div class="mobile-card-header">
          <div class="card-title">审批历史</div>
        </div>
        <div class="mobile-card-content">
          <div
            v-for="(item, index) in approvalHistory"
            :key="index"
            class="history-item">
            <div class="history-header">
              <span class="history-node">{{ item.nodeName }}</span>
              <span class="mobile-tag" :class="getActionTagClass(item.action)">
                {{ item.action | formatStatus('approvalAction') }}
              </span>
            </div>
            <div class="history-meta">
              <span>{{ item.handlerName }}</span>
              <span>{{ item.handleTime | formatDate('YYYY-MM-DD HH:mm') }}</span>
            </div>
            <div class="history-comment" v-if="item.comment">
              {{ item.comment }}
            </div>
          </div>
        </div>
      </div>

      <div v-if="showApprovalAction">
        <div class="mobile-alert alert-warning" v-if="businessData?.testResult === 'FAILED'">
          <p><strong>业务规则提醒：</strong></p>
          <p>• 检测结果为不合格的批次，不能通过审批</p>
        </div>
        <div class="mobile-alert alert-danger" v-if="approvalDetail?.approvalType === 'LOADING_CONFIRM' && businessData?.testResult !== 'PASSED'">
          <p><strong>业务规则提醒：</strong></p>
          <p>• 检测未通过的批次不能装车运输</p>
        </div>

        <div class="mobile-form">
          <div class="form-item">
            <label class="form-label">审批意见</label>
            <textarea
              v-model="approvalForm.comment"
              class="form-textarea"
              rows="4"
              maxlength="500"
              placeholder="请输入审批意见（驳回时必填）"></textarea>
            <div class="char-count">{{ approvalForm.comment.length }}/500</div>
          </div>
        </div>

        <div class="mobile-btn-group">
          <button
            class="mobile-btn btn-success"
            :class="{ disabled: businessData?.testResult === 'FAILED' }"
            :disabled="submitting || businessData?.testResult === 'FAILED'"
            @click="handleApprove">
            同意
          </button>
          <button
            class="mobile-btn btn-danger"
            :disabled="submitting"
            @click="handleReject">
            驳回
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getApprovalDetail, getApprovalHistory, approve, reject } from '@/api/approval'

export default {
  name: 'MobileApprovalDetail',
  data() {
    return {
      loading: false,
      submitting: false,
      approvalDetail: null,
      businessData: null,
      approvalNodes: [],
      approvalHistory: [],
      approvalForm: {
        comment: ''
      }
    }
  },
  computed: {
    showApprovalAction() {
      if (!this.approvalDetail) return false
      if (this.approvalDetail.status !== 'PENDING' && this.approvalDetail.status !== 'APPROVING') return false
      return this.approvalDetail.isCurrentNode === true
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const id = this.$route.params.id
        const [detailRes, historyRes] = await Promise.all([
          getApprovalDetail(id),
          getApprovalHistory({ businessId: id })
        ])
        this.approvalDetail = detailRes.data
        this.businessData = detailRes.data?.businessData || {}
        this.approvalNodes = detailRes.data?.approvalNodes || []
        this.approvalHistory = historyRes.data?.records || []
      } catch (error) {
        console.error('加载详情失败:', error)
      } finally {
        this.loading = false
      }
    },
    refresh() {
      this.loadData()
    },
    getStatusTagClass(status) {
      const map = {
        PENDING: 'tag-warning',
        APPROVING: 'tag-primary',
        APPROVED: 'tag-success',
        REJECTED: 'tag-danger',
        CANCELLED: 'tag-info'
      }
      return map[status] || 'tag-info'
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
    getPriorityTagClass(priority) {
      const map = {
        URGENT: 'tag-danger',
        HIGH: 'tag-warning',
        NORMAL: 'tag-primary',
        LOW: 'tag-info'
      }
      return map[priority] || 'tag-info'
    },
    getNodeStatusTagClass(status) {
      const map = {
        PENDING: 'tag-info',
        PROCESSING: 'tag-primary',
        APPROVED: 'tag-success',
        REJECTED: 'tag-danger',
        SKIPPED: 'tag-info'
      }
      return map[status] || 'tag-info'
    },
    getActionTagClass(action) {
      const map = {
        APPROVE: 'tag-success',
        REJECT: 'tag-danger',
        SUBMIT: 'tag-primary',
        CANCEL: 'tag-info'
      }
      return map[action] || 'tag-info'
    },
    getFlowDotClass(status) {
      const map = {
        PENDING: 'dot-pending',
        PROCESSING: 'dot-processing',
        APPROVED: 'dot-approved',
        REJECTED: 'dot-rejected',
        SKIPPED: 'dot-skipped'
      }
      return map[status] || 'dot-pending'
    },
    getFlowLineClass(status) {
      const map = {
        PENDING: 'line-pending',
        PROCESSING: 'line-processing',
        APPROVED: 'line-approved',
        REJECTED: 'line-rejected',
        SKIPPED: 'line-skipped'
      }
      return map[status] || 'line-pending'
    },
    getFlowIcon(status) {
      const map = {
        PENDING: 'el-icon-time',
        PROCESSING: 'el-icon-loading',
        APPROVED: 'el-icon-check',
        REJECTED: 'el-icon-close',
        SKIPPED: 'el-icon-right'
      }
      return map[status] || 'el-icon-dot'
    },
    handleApprove() {
      this.$confirm('确定要通过该审批吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.doApprove()
      }).catch(() => {})
    },
    async doApprove() {
      try {
        this.submitting = true
        await approve({
          id: this.$route.params.id,
          comment: this.approvalForm.comment
        })
        this.$message.success('审批通过成功')
        this.loadData()
      } catch (error) {
        console.error('审批失败:', error)
      } finally {
        this.submitting = false
      }
    },
    handleReject() {
      if (!this.approvalForm.comment.trim()) {
        this.$message.warning('请输入驳回意见')
        return
      }
      this.$confirm('确定要驳回该审批吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.doReject()
      }).catch(() => {})
    },
    async doReject() {
      try {
        this.submitting = true
        await reject({
          id: this.$route.params.id,
          comment: this.approvalForm.comment
        })
        this.$message.success('审批驳回成功')
        this.loadData()
      } catch (error) {
        console.error('驳回失败:', error)
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.approval-flow-mobile {
  padding: 10px 0;
}

.flow-item {
  display: flex;
  margin-bottom: 0;

  &:last-child {
    .flow-line {
      display: none;
    }
  }
}

.flow-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 16px;
}

.flow-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  flex-shrink: 0;

  &.dot-pending {
    background: #c0c4cc;
  }

  &.dot-processing {
    background: #409eff;
    animation: pulse 1.5s ease-in-out infinite;
  }

  &.dot-approved {
    background: #67c23a;
  }

  &.dot-rejected {
    background: #f56c6c;
  }

  &.dot-skipped {
    background: #909399;
  }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(64, 158, 255, 0);
  }
}

.flow-line {
  width: 2px;
  flex: 1;
  min-height: 40px;
  margin: 4px 0;

  &.line-pending {
    background: #e4e7ed;
  }

  &.line-processing {
    background: linear-gradient(to bottom, #409eff, #e4e7ed);
  }

  &.line-approved {
    background: #67c23a;
  }

  &.line-rejected {
    background: #f56c6c;
  }

  &.line-skipped {
    background: #909399;
  }
}

.flow-right {
  flex: 1;
  padding-bottom: 20px;
}

.flow-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;

  .flow-node-name {
    font-weight: 600;
    font-size: 14px;
    color: #303133;
  }
}

.flow-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.flow-handler,
.flow-comment {
  font-size: 13px;
  color: #606266;
  margin-bottom: 2px;
}

.flow-comment {
  color: #909399;
}

.history-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;

  &:last-child {
    border-bottom: none;
  }
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;

  .history-node {
    font-weight: 500;
    font-size: 14px;
    color: #303133;
  }
}

.history-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.history-comment {
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 6px;
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}

.mobile-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
