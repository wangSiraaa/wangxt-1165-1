<template>
  <div class="container">
    <div class="page-header">
      <span class="title">我的待审批</span>
      <div style="float: right;">
        <el-button type="primary" @click="handleBatchApprove" :disabled="selectedIds.length === 0">
          <i class="el-icon-check"></i> 批量通过
        </el-button>
        <el-button type="danger" @click="handleBatchReject" :disabled="selectedIds.length === 0">
          <i class="el-icon-close"></i> 批量驳回
        </el-button>
      </div>
    </div>

    <el-card>
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="审批类型">
            <el-select v-model="searchForm.approvalType" placeholder="全部" clearable style="width: 180px;">
              <el-option label="批次检测审批" value="BATCH_TEST" />
              <el-option label="装车确认审批" value="LOADING_CONFIRM" />
              <el-option label="奶罐清洗审批" value="TANK_CLEANING" />
              <el-option label="异常处理审批" value="EXCEPTION_HANDLE" />
            </el-select>
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="searchForm.priority" placeholder="全部" clearable style="width: 150px;">
              <el-option label="紧急" value="URGENT" />
              <el-option label="高" value="HIGH" />
              <el-option label="普通" value="NORMAL" />
              <el-option label="低" value="LOW" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        :data="tableData"
        border
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="approvalNo" label="审批单号" width="180" />
        <el-table-column prop="approvalType" label="审批类型" width="140">
          <template slot-scope="scope">
            <el-tag :type="getApprovalTypeTag(scope.row.approvalType)" size="small">
              {{ scope.row.approvalType | formatStatus('approvalType') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="batchNo" label="关联批次" width="160" />
        <el-table-column prop="milkTankName" label="奶罐" width="100" />
        <el-table-column prop="testResult" label="检测结果" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.testResult === 'PASSED' ? 'success' : 'danger'" size="small" v-if="scope.row.testResult">
              {{ scope.row.testResult | formatStatus('test') }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="applyTime" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ scope.row.applyTime | formatDate }}
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template slot-scope="scope">
            <el-tag :type="getPriorityTag(scope.row.priority)" size="mini">
              {{ scope.row.priority | formatStatus('priority') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nodeName" label="当前节点" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button type="text" size="small" @click="handleSingleApprove(scope.row)" style="color: #67c23a;">通过</el-button>
            <el-button type="text" size="small" @click="handleSingleReject(scope.row)" style="color: #f56c6c;">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.pageNum"
          :page-sizes="[10, 20, 50]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
      </div>
    </el-card>

    <el-dialog title="审批意见" :visible.sync="opinionVisible" width="500px">
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
  name: 'ApprovalPending',
  data() {
    return {
      loading: false,
      tableData: [],
      selectedIds: [],
      searchForm: {
        approvalType: '',
        priority: ''
      },
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      opinionVisible: false,
      opinionForm: {
        type: '',
        ids: [],
        comment: ''
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pagination.pageNum,
          pageSize: this.pagination.pageSize,
          approvalType: this.searchForm.approvalType || undefined,
          priority: this.searchForm.priority || undefined
        }
        const res = await getMyPending(params)
        this.tableData = res.data?.records || []
        this.pagination.total = res.data?.total || 0
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    resetSearch() {
      this.searchForm = { approvalType: '', priority: '' }
      this.pagination.pageNum = 1
      this.loadData()
    },
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.pageNum = val
      this.loadData()
    },
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
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
    viewDetail(row) {
      this.$router.push('/approval/' + row.id)
    },
    handleSingleApprove(row) {
      this.$confirm('确定要通过该审批吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.opinionForm = { type: 'approve', ids: [row.id], comment: '' }
        this.opinionVisible = true
      }).catch(() => {})
    },
    handleSingleReject(row) {
      this.opinionForm = { type: 'reject', ids: [row.id], comment: '' }
      this.opinionVisible = true
    },
    handleBatchApprove() {
      this.$confirm('确定要通过选中的 ' + this.selectedIds.length + ' 条审批吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.opinionForm = { type: 'approve', ids: [...this.selectedIds], comment: '' }
        this.opinionVisible = true
      }).catch(() => {})
    },
    handleBatchReject() {
      this.opinionForm = { type: 'reject', ids: [...this.selectedIds], comment: '' }
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
        for (const id of this.opinionForm.ids) {
          await action({ id, comment: this.opinionForm.comment })
        }
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
.el-table {
  margin-bottom: 15px;
}
</style>
