<template>
  <div class="container">
    <div class="page-header">
      <span class="title">拒收记录</span>
      <el-alert
        title="展示所有被拒收的批次及拒收原因"
        type="info"
        :closable="false"
        style="margin-top: 10px;" />
    </div>

    <el-card>
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="拒收类型">
            <el-select v-model="searchForm.rejectType" placeholder="全部" clearable style="width: 150px;">
              <el-option label="检测不合格" value="TEST_FAILED" />
              <el-option label="奶罐未清洗" value="TANK_DIRTY" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理状态">
            <el-select v-model="searchForm.handleStatus" placeholder="全部" clearable style="width: 150px;">
              <el-option label="待处理" value="PENDING" />
              <el-option label="已处理" value="HANDLED" />
              <el-option label="已忽略" value="IGNORED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="rejectNo" label="拒收单号" width="180" />
        <el-table-column prop="batchNo" label="批次号" width="180" />
        <el-table-column prop="milkTankName" label="奶罐" width="120" />
        <el-table-column prop="rejectType" label="拒收类型" width="140">
          <template slot-scope="scope">
            <el-tag :type="getRejectTypeTag(scope.row.rejectType)" size="small">
              {{ scope.row.rejectType | formatStatus('rejectType') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒收原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="handleStatus" label="处理状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getHandleStatusTag(scope.row.handleStatus)" size="small">
              {{ scope.row.handleStatus | formatStatus('handleStatus') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectTime" label="拒收时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.rejectTime | formatDate }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="viewDetail(scope.row)">
              详情
            </el-button>
            <el-button
              type="text"
              size="small"
              :disabled="scope.row.handleStatus !== 'PENDING'"
              @click="handleReject(scope.row)">
              处理
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

    <el-dialog title="拒收详情" :visible.sync="detailVisible" width="600px">
      <el-descriptions :column="2" border v-if="currentReject">
        <el-descriptions-item label="拒收单号">{{ currentReject.rejectNo }}</el-descriptions-item>
        <el-descriptions-item label="批次号">{{ currentReject.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="奶罐">{{ currentReject.milkTankName }}</el-descriptions-item>
        <el-descriptions-item label="拒收类型">
          <el-tag :type="getRejectTypeTag(currentReject.rejectType)">
            {{ currentReject.rejectType | formatStatus('rejectType') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getHandleStatusTag(currentReject.handleStatus)">
            {{ currentReject.handleStatus | formatStatus('handleStatus') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="拒收人">{{ currentReject.rejectOperator || '-' }}</el-descriptions-item>
        <el-descriptions-item label="拒收原因" :span="2">{{ currentReject.rejectReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="拒收时间" :span="2">{{ currentReject.rejectTime | formatDate }}</el-descriptions-item>
        <el-descriptions-item label="处理结果" :span="2">{{ currentReject.handleResult || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ currentReject.handleOperator || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ currentReject.handleTime ? (currentReject.handleTime | formatDate) : '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog title="处理拒收" :visible.sync="handleVisible" width="500px">
      <el-form :model="handleForm" :rules="handleRules" ref="handleForm" label-width="100px">
        <el-form-item label="处理状态" prop="handleStatus">
          <el-select v-model="handleForm.handleStatus" style="width: 100%;">
            <el-option label="已处理" value="HANDLED" />
            <el-option label="已忽略" value="IGNORED" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理结果" prop="handleResult">
          <el-input type="textarea" v-model="handleForm.handleResult" :rows="3" placeholder="请输入处理结果（必填）" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="handleLoading" @click="submitHandle">确认处理</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getRejectList, handleReject } from '@/api/reject'

export default {
  name: 'RejectList',
  data() {
    return {
      loading: false,
      handleLoading: false,
      tableData: [],
      searchForm: {
        rejectType: '',
        handleStatus: ''
      },
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      detailVisible: false,
      handleVisible: false,
      currentReject: null,
      handleForm: {
        handleStatus: '',
        handleResult: ''
      },
      handleRules: {
        handleStatus: [{ required: true, message: '请选择处理状态', trigger: 'change' }],
        handleResult: [{ required: true, message: '请输入处理结果', trigger: 'blur' }]
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
          rejectType: this.searchForm.rejectType || undefined,
          handleStatus: this.searchForm.handleStatus || undefined
        }
        const res = await getRejectList(params)
        this.tableData = res.data?.records || []
        this.pagination.total = res.data?.total || 0
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    resetSearch() {
      this.searchForm = { rejectType: '', handleStatus: '' }
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
    getRejectTypeTag(type) {
      const map = {
        TEST_FAILED: 'danger',
        TANK_DIRTY: 'warning',
        OTHER: 'info'
      }
      return map[type] || 'info'
    },
    getHandleStatusTag(status) {
      const map = {
        PENDING: 'warning',
        HANDLED: 'success',
        IGNORED: 'info'
      }
      return map[status] || 'info'
    },
    viewDetail(row) {
      this.currentReject = row
      this.detailVisible = true
    },
    handleReject(row) {
      this.currentReject = row
      this.handleForm = {
        handleStatus: '',
        handleResult: ''
      }
      this.handleVisible = true
    },
    async submitHandle() {
      this.$refs.handleForm.validate(async valid => {
        if (valid) {
          this.handleLoading = true
          try {
            await handleReject(this.currentReject.id, this.handleForm)
            this.$message.success('处理成功')
            this.handleVisible = false
            this.loadData()
          } catch (error) {
            console.error('处理失败:', error)
          } finally {
            this.handleLoading = false
          }
        }
      })
    }
  }
}
</script>
