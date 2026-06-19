<template>
  <div class="container">
    <div class="page-header">
      <span class="title">装车管理</span>
      <el-button type="primary" style="float: right;" @click="$router.push('/loading/confirm')">
        <i class="el-icon-circle-check"></i> 确认装车
      </el-button>
    </div>

    <el-card>
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="车牌号">
            <el-input v-model="searchForm.plateNo" placeholder="请输入车牌号" clearable style="width: 150px;" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="loadingNo" label="装车单号" width="180" />
        <el-table-column prop="batchNo" label="批次号" width="180" />
        <el-table-column prop="milkTankName" label="奶罐" width="120" />
        <el-table-column prop="plateNo" label="车牌号" width="120" />
        <el-table-column prop="driverName" label="司机" width="120" />
        <el-table-column prop="loadedVolume" label="装车量(kg)" width="130" />
        <el-table-column prop="testResult" label="检测结果" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.testResult === 'PASSED' ? 'success' : 'danger'" size="small">
              {{ scope.row.testResult | formatStatus('test') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loadingTime" label="装车时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.loadingTime | formatDate }}
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

    <el-dialog title="装车详情" :visible.sync="detailVisible" width="600px">
      <el-descriptions :column="2" border v-if="currentLoading">
        <el-descriptions-item label="装车单号">{{ currentLoading.loadingNo }}</el-descriptions-item>
        <el-descriptions-item label="批次号">{{ currentLoading.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="奶罐">{{ currentLoading.milkTankName }}</el-descriptions-item>
        <el-descriptions-item label="车牌号">{{ currentLoading.plateNo }}</el-descriptions-item>
        <el-descriptions-item label="司机">{{ currentLoading.driverName }}</el-descriptions-item>
        <el-descriptions-item label="装车量(kg)">{{ currentLoading.loadedVolume }}</el-descriptions-item>
        <el-descriptions-item label="检测结果">
          <el-tag :type="currentLoading.testResult === 'PASSED' ? 'success' : 'danger'">
            {{ currentLoading.testResult | formatStatus('test') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="运输目的地">{{ currentLoading.destination || '-' }}</el-descriptions-item>
        <el-descriptions-item label="装车时间" :span="2">{{ currentLoading.loadingTime | formatDate }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentLoading.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getLoadingList } from '@/api/loading'

export default {
  name: 'LoadingList',
  data() {
    return {
      loading: false,
      tableData: [],
      searchForm: {
        plateNo: ''
      },
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      detailVisible: false,
      currentLoading: null
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
          pageSize: this.pagination.pageSize
        }
        const res = await getLoadingList(params)
        this.tableData = res.data?.records || []
        this.pagination.total = res.data?.total || 0
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    resetSearch() {
      this.searchForm = { plateNo: '' }
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
    viewDetail(row) {
      this.currentLoading = row
      this.detailVisible = true
    }
  }
}
</script>
