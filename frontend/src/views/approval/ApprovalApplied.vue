<template>
  <div class="container">
    <div class="page-header">
      <span class="title">我提交的审批</span>
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
          <el-form-item label="审批状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 150px;">
              <el-option label="待审批" value="PENDING" />
              <el-option label="审批中" value="APPROVING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="申请时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 260px;">
            </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
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
        <el-table-column prop="status" label="审批状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ scope.row.status | formatStatus('approvalStatus') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentNodeName" label="当前节点" width="120" />
        <el-table-column prop="applyTime" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ scope.row.applyTime | formatDate }}
          </template>
        </el-table-column>
        <el-table-column label="审批进度" width="200">
          <template slot-scope="scope">
            <el-steps :active="getProgressStep(scope.row)" finish-status="success" size="mini" :status="getStepStatus(scope.row)" style="margin-top: 5px;">
              <el-step title="提交" />
              <el-step :title="scope.row.currentNodeName || '审批'" />
              <el-step title="完成" />
            </el-steps>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button
              type="text"
              size="small"
              @click="handleCancel(scope.row)"
              style="color: #f56c6c;"
              :disabled="scope.row.status !== 'PENDING'">
              取消
            </el-button>
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
  </div>
</template>

<script>
import { getMyApplied, cancelApproval } from '@/api/approval'

export default {
  name: 'ApprovalApplied',
  data() {
    return {
      loading: false,
      tableData: [],
      searchForm: {
        approvalType: '',
        status: '',
        dateRange: []
      },
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
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
          status: this.searchForm.status || undefined,
          startTime: this.searchForm.dateRange?.[0] || undefined,
          endTime: this.searchForm.dateRange?.[1] || undefined
        }
        const res = await getMyApplied(params)
        this.tableData = res.data?.records || []
        this.pagination.total = res.data?.total || 0
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    resetSearch() {
      this.searchForm = { approvalType: '', status: '', dateRange: [] }
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
    getApprovalTypeTag(type) {
      const map = {
        BATCH_TEST: 'primary',
        LOADING_CONFIRM: 'success',
        TANK_CLEANING: 'warning',
        EXCEPTION_HANDLE: 'danger'
      }
      return map[type] || 'info'
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
    getProgressStep(row) {
      if (row.status === 'PENDING') return 1
      if (row.status === 'APPROVING') return 1
      if (row.status === 'APPROVED') return 3
      if (row.status === 'REJECTED') return 1
      if (row.status === 'CANCELLED') return 0
      return 0
    },
    getStepStatus(row) {
      if (row.status === 'REJECTED') return 'error'
      if (row.status === 'CANCELLED') return 'wait'
      return ''
    },
    viewDetail(row) {
      this.$router.push('/approval/' + row.id)
    },
    handleCancel(row) {
      this.$confirm('确定要取消该审批吗？取消后无法恢复。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          this.loading = true
          await cancelApproval(row.id)
          this.$message.success('取消成功')
          this.loadData()
        } catch (error) {
          console.error('取消失败:', error)
        } finally {
          this.loading = false
        }
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.el-table {
  margin-bottom: 15px;
}

::v-deep .el-steps--horizontal {
  margin: 0;
}

::v-deep .el-step {
  flex: 1;
}
</style>
