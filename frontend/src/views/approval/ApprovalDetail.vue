<template>
  <div class="container">
    <div class="page-header">
      <el-button @click="$router.back()">
        <i class="el-icon-arrow-left"></i> 返回
      </el-button>
      <span class="title" style="margin-left: 10px;">审批详情</span>
    </div>

    <el-card v-loading="loading" class="detail-card">
      <div class="detail-header">
        <div class="header-left">
          <h3>{{ approvalDetail?.approvalNo || '审批详情' }}</h3>
          <el-tag :type="getStatusType(approvalDetail?.status)" size="medium">
            {{ approvalDetail?.status | formatStatus('approvalStatus') }}
          </el-tag>
        </div>
        <div class="header-right">
          <span>申请时间：{{ approvalDetail?.applyTime | formatDate }}</span>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="section-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-document"></i> 审批基本信息</span>
          </div>
          <el-descriptions :column="2" border v-if="approvalDetail">
            <el-descriptions-item label="审批类型">
              <el-tag :type="getApprovalTypeTag(approvalDetail.approvalType)">
                {{ approvalDetail.approvalType | formatStatus('approvalType') }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag :type="getPriorityTag(approvalDetail.priority)">
                {{ approvalDetail.priority | formatStatus('priority') }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="申请人">{{ approvalDetail.applicantName }}</el-descriptions-item>
            <el-descriptions-item label="申请部门">{{ approvalDetail.departmentName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="当前节点">{{ approvalDetail.currentNodeName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="审批单号">{{ approvalDetail.approvalNo }}</el-descriptions-item>
            <el-descriptions-item label="申请理由" :span="2">{{ approvalDetail.applyReason || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="section-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-s-order"></i> 关联业务单据</span>
          </div>
          <el-descriptions :column="2" border v-if="businessData">
            <el-descriptions-item label="批次号">{{ businessData.batchNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="奶罐号">{{ businessData.milkTankName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="奶量(kg)">{{ businessData.milkVolume || '-' }}</el-descriptions-item>
            <el-descriptions-item label="温度(°C)">{{ businessData.milkTemperature || '-' }}</el-descriptions-item>
            <el-descriptions-item label="挤奶员">{{ businessData.milker || '-' }}</el-descriptions-item>
            <el-descriptions-item label="检测结果">
              <el-tag :type="businessData.testResult === 'PASSED' ? 'success' : 'danger'" v-if="businessData.testResult">
                {{ businessData.testResult | formatStatus('test') }}
              </el-tag>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="检测时间" :span="2">{{ businessData.testTime | formatDate }}</el-descriptions-item>
            <el-descriptions-item label="抗生素检测" v-if="businessData.antibioticTest">
              <el-tag :type="businessData.antibioticTest === 'NEGATIVE' ? 'success' : 'danger'">
                {{ businessData.antibioticTest === 'NEGATIVE' ? '阴性' : '阳性' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="检测项目" v-if="businessData.testItems">{{ businessData.testItems || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ businessData.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="section-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-time"></i> 审批流程</span>
          </div>
          <div class="approval-flow">
            <el-timeline>
              <el-timeline-item
                v-for="(node, index) in approvalNodes"
                :key="index"
                :timestamp="node.handleTime | formatDate"
                :type="getTimelineType(node.status)"
                :icon="getTimelineIcon(node.status)">
                <el-card shadow="never" class="timeline-card">
                  <div class="timeline-header">
                    <span class="node-name">{{ node.nodeName }}</span>
                    <el-tag :type="getNodeStatusType(node.status)" size="mini">
                      {{ node.status | formatStatus('nodeStatus') }}
                    </el-tag>
                  </div>
                  <div class="timeline-content" v-if="node.status !== 'PENDING' && node.status !== 'PROCESSING'">
                    <div class="handler-info">
                      <span>处理人：{{ node.handlerName || '-' }}</span>
                    </div>
                    <div class="comment" v-if="node.comment">
                      <span>意见：{{ node.comment }}</span>
                    </div>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>

        <el-card class="section-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-notebook-2"></i> 审批历史记录</span>
          </div>
          <el-table :data="approvalHistory" border size="small" max-height="300">
            <el-table-column prop="nodeName" label="节点" width="120" />
            <el-table-column prop="handlerName" label="处理人" width="100" />
            <el-table-column prop="action" label="操作" width="80">
              <template slot-scope="scope">
                <el-tag :type="getActionType(scope.row.action)" size="mini">
                  {{ scope.row.action | formatStatus('approvalAction') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="handleTime" label="处理时间" width="160">
              <template slot-scope="scope">
                {{ scope.row.handleTime | formatDate('MM-DD HH:mm') }}
              </template>
            </el-table-column>
            <el-table-column prop="comment" label="意见" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="section-card" v-if="showApprovalAction">
      <el-alert
        v-if="businessData?.testResult === 'FAILED'"
        title="业务规则提醒"
        type="warning"
        :closable="false"
        show-icon>
        <p>• 检测结果为不合格的批次，不能通过审批</p>
        <p>• 如需通过请先处理异常后重新提交检测</p>
      </el-alert>
      <el-alert
        v-if="approvalDetail?.approvalType === 'LOADING_CONFIRM' && businessData?.testResult !== 'PASSED'"
        title="业务规则提醒"
        type="error"
        :closable="false"
        show-icon>
        <p>• 检测未通过的批次不能装车运输</p>
        <p>• 装车确认前必须确保检测结果合格</p>
      </el-alert>

      <el-form :model="approvalForm" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="审批意见">
          <el-input
            type="textarea"
            v-model="approvalForm.comment"
            :rows="4"
            :maxlength="500"
            show-word-limit
            placeholder="请输入审批意见（驳回时必填）">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            type="success"
            :loading="submitting"
            :disabled="businessData?.testResult === 'FAILED'"
            @click="handleApprove">
            <i class="el-icon-check"></i> 同意
          </el-button>
          <el-button
            type="danger"
            :loading="submitting"
            @click="handleReject">
            <i class="el-icon-close"></i> 驳回
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getApprovalDetail, getApprovalHistory, approve, reject } from '@/api/approval'

export default {
  name: 'ApprovalDetail',
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
    getStatusType(status) {
      const map = {
        PENDING: 'warning',
        APPROVING: 'primary',
        APPROVED: 'success',
        REJECTED: 'danger',
        CANCELLED: 'info'
      }
      return map[status] || 'info'
    },
    getApprovalTypeTag(type) {
      const map = {
        BATCH_TEST: 'primary',
        LOADING_CONFIRM: 'success',
        TANK_CLEANING: 'warning',
        EXCEPTION_HANDLE: 'danger'
      }
      return map[type] || 'info'
    },
    getPriorityTag(priority) {
      const map = {
        URGENT: 'danger',
        HIGH: 'warning',
        NORMAL: 'primary',
        LOW: 'info'
      }
      return map[priority] || 'info'
    },
    getTimelineType(status) {
      const map = {
        PENDING: '',
        PROCESSING: 'primary',
        APPROVED: 'success',
        REJECTED: 'danger',
        SKIPPED: 'info'
      }
      return map[status] || ''
    },
    getTimelineIcon(status) {
      const map = {
        PENDING: 'el-icon-time',
        PROCESSING: 'el-icon-loading',
        APPROVED: 'el-icon-check',
        REJECTED: 'el-icon-close',
        SKIPPED: 'el-icon-right'
      }
      return map[status] || 'el-icon-dot'
    },
    getNodeStatusType(status) {
      const map = {
        PENDING: 'info',
        PROCESSING: 'primary',
        APPROVED: 'success',
        REJECTED: 'danger',
        SKIPPED: 'info'
      }
      return map[status] || 'info'
    },
    getActionType(action) {
      const map = {
        APPROVE: 'success',
        REJECT: 'danger',
        SUBMIT: 'primary',
        CANCEL: 'info'
      }
      return map[action] || 'info'
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
.detail-card {
  margin-bottom: 20px;

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 15px;

      h3 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }

    .header-right {
      color: #909399;
      font-size: 14px;
    }
  }
}

.section-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: 600;
  font-size: 14px;

  i {
    margin-right: 8px;
    color: #409eff;
  }
}

.approval-flow {
  padding: 10px 0;
}

.timeline-card {
  background: #f5f7fa;
  border: none;
  padding: 12px 16px;

  .timeline-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .node-name {
      font-weight: 600;
      font-size: 14px;
      color: #303133;
    }
  }

  .timeline-content {
    font-size: 13px;
    color: #606266;

    .handler-info,
    .comment {
      margin-bottom: 4px;
    }

    .comment {
      color: #909399;
    }
  }
}

::v-deep .el-timeline-item__timestamp {
  color: #909399;
  font-size: 12px;
}

::v-deep .el-descriptions {
  .el-descriptions-item__label {
    width: 100px;
  }
}
</style>
