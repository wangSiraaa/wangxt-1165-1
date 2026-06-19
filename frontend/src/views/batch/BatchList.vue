<template>
  <div class="container">
    <div class="page-header">
      <span class="title">批次管理</span>
      <el-button type="primary" style="float: right;" @click="$router.push('/batch/submit')">
        <i class="el-icon-plus"></i> 提交新批次
      </el-button>
    </div>

    <el-card>
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="批次状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 150px;">
              <el-option label="挤奶中" value="MILKING" />
              <el-option label="待检测" value="PENDING_TEST" />
              <el-option label="检测通过" value="TEST_PASSED" />
              <el-option label="检测不合格" value="TEST_FAILED" />
              <el-option label="已装车" value="LOADED" />
              <el-option label="已拒收" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="batchNo" label="批次号" width="180" />
        <el-table-column prop="milkTankName" label="奶罐" width="120" />
        <el-table-column prop="milkVolume" label="奶量(kg)" width="120" />
        <el-table-column prop="milkTemperature" label="温度(°C)" width="120" />
        <el-table-column prop="batchStatus" label="状态" width="140">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.batchStatus)" size="small">
              {{ scope.row.batchStatus | formatStatus('batch') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.createTime | formatDate }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="viewDetail(scope.row)">详情</el-button>
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

    <el-dialog title="批次详情" :visible.sync="detailVisible" width="600px">
      <el-descriptions :column="2" border v-if="currentBatch">
        <el-descriptions-item label="批次号">{{ currentBatch.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentBatch.batchStatus)">
            {{ currentBatch.batchStatus | formatStatus('batch') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="奶罐">{{ currentBatch.milkTankName }}</el-descriptions-item>
        <el-descriptions-item label="挤奶员">{{ currentBatch.milker }}</el-descriptions-item>
        <el-descriptions-item label="奶量(kg)">{{ currentBatch.milkVolume }}</el-descriptions-item>
        <el-descriptions-item label="温度(°C)">{{ currentBatch.milkTemperature }}</el-descriptions-item>
        <el-descriptions-item label="牛群编号">{{ currentBatch.cowGroupNo }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentBatch.createTime | formatDate }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentBatch.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getBatchList } from '@/api/batch'

export default {
  name: 'BatchList',
  data() {
    return {
      loading: false,
      tableData: [],
      searchForm: {
        status: ''
      },
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      detailVisible: false,
      currentBatch: null
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
          status: this.searchForm.status || undefined
        }
        const res = await getBatchList(params)
        this.tableData = res.data?.records || []
        this.pagination.total = res.data?.total || 0
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    resetSearch() {
      this.searchForm = { status: '' }
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
    getStatusType(status) {
      const map = {
        MILKING: 'primary',
        PENDING_TEST: 'warning',
        TEST_PASSED: 'success',
        TEST_FAILED: 'danger',
        LOADED: 'success',
        REJECTED: 'danger'
      }
      return map[status] || 'info'
    },
    viewDetail(row) {
      this.currentBatch = row
      this.detailVisible = true
    }
  }
}
</script>
