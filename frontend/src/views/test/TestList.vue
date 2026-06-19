<template>
  <div class="container">
    <div class="page-header">
      <span class="title">检测管理</span>
      <el-button type="primary" style="float: right;" @click="$router.push('/test/submit')">
        <i class="el-icon-plus"></i> 录入检测结果
      </el-button>
    </div>

    <el-card>
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="检测结果">
            <el-select v-model="searchForm.result" placeholder="全部" clearable style="width: 150px;">
              <el-option label="合格" value="PASSED" />
              <el-option label="不合格" value="FAILED" />
            </el-select>
          </el-form-item>
          <el-form-item label="是否锁定">
            <el-select v-model="searchForm.locked" placeholder="全部" clearable style="width: 150px;">
              <el-option label="已锁定" :value="true" />
              <el-option label="未锁定" :value="false" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="testNo" label="检测单号" width="180" />
        <el-table-column prop="batchNo" label="批次号" width="180" />
        <el-table-column prop="testItem" label="检测项目" width="150" />
        <el-table-column prop="testResult" label="检测结果" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.testResult === 'PASSED' ? 'success' : 'danger'" size="small">
              {{ scope.row.testResult | formatStatus('test') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isLocked" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isLocked ? 'info' : 'warning'" size="small">
              {{ scope.row.isLocked ? '已锁定' : '可修改' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tester" label="化验员" width="120" />
        <el-table-column prop="testTime" label="检测时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.testTime | formatDate }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button
              type="text"
              size="small"
              :disabled="scope.row.isLocked"
              @click="editTest(scope.row)">
              {{ scope.row.isLocked ? '已锁定' : '修改' }}
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

    <el-dialog title="检测详情" :visible.sync="detailVisible" width="600px">
      <el-descriptions :column="2" border v-if="currentTest">
        <el-descriptions-item label="检测单号">{{ currentTest.testNo }}</el-descriptions-item>
        <el-descriptions-item label="批次号">{{ currentTest.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="检测项目">{{ currentTest.testItem }}</el-descriptions-item>
        <el-descriptions-item label="检测结果">
          <el-tag :type="currentTest.testResult === 'PASSED' ? 'success' : 'danger'">
            {{ currentTest.testResult | formatStatus('test') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检测值">{{ currentTest.testValue || '-' }}</el-descriptions-item>
        <el-descriptions-item label="参考值">{{ currentTest.referenceValue || '-' }}</el-descriptions-item>
        <el-descriptions-item label="化验员">{{ currentTest.tester }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentTest.isLocked ? 'info' : 'warning'">
            {{ currentTest.isLocked ? '已锁定' : '可修改' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检测时间" :span="2">{{ currentTest.testTime | formatDate }}</el-descriptions-item>
        <el-descriptions-item label="检测备注" :span="2">{{ currentTest.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" v-if="currentTest && !currentTest.isLocked">
        <el-button type="primary" @click="editTest(currentTest)">修改检测结果</el-button>
      </div>
    </el-dialog>

    <el-dialog title="修改检测结果" :visible.sync="editVisible" width="500px">
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="100px">
        <el-alert
          v-if="currentTest?.isLocked"
          title="装车后检测结果已锁定，不能修改为合格"
          type="warning"
          :closable="false"
          style="margin-bottom: 15px;" />
        <el-form-item label="检测结果" prop="testResult">
          <el-select v-model="editForm.testResult" style="width: 100%;">
            <el-option label="合格" value="PASSED" :disabled="currentTest?.isLocked" />
            <el-option label="不合格" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="检测值">
          <el-input v-model="editForm.testValue" placeholder="请输入检测值（选填）" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="editForm.remark" :rows="2" placeholder="请输入修改原因（必填）" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="saveEdit">确定修改</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getTestList, updateTest } from '@/api/test'

export default {
  name: 'TestList',
  data() {
    return {
      loading: false,
      editLoading: false,
      tableData: [],
      searchForm: {
        result: '',
        locked: ''
      },
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      detailVisible: false,
      editVisible: false,
      currentTest: null,
      editForm: {
        testResult: '',
        testValue: '',
        remark: ''
      },
      editRules: {
        testResult: [{ required: true, message: '请选择检测结果', trigger: 'change' }],
        remark: [{ required: true, message: '请输入修改原因', trigger: 'blur' }]
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
          result: this.searchForm.result || undefined
        }
        const res = await getTestList(params)
        this.tableData = res.data?.records || []
        this.pagination.total = res.data?.total || 0
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    resetSearch() {
      this.searchForm = { result: '', locked: '' }
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
      this.currentTest = row
      this.detailVisible = true
    },
    editTest(row) {
      if (row.isLocked) {
        this.$message.warning('该检测结果已锁定，装车后检测结果不能修改为合格')
        return
      }
      this.currentTest = row
      this.detailVisible = false
      this.editForm = {
        testResult: row.testResult,
        testValue: row.testValue || '',
        remark: ''
      }
      this.editVisible = true
    },
    async saveEdit() {
      this.$refs.editForm.validate(async valid => {
        if (valid) {
          this.editLoading = true
          try {
            await updateTest(this.currentTest.id, this.editForm)
            this.$message.success('修改成功')
            this.editVisible = false
            this.loadData()
          } catch (error) {
            console.error('修改失败:', error)
          } finally {
            this.editLoading = false
          }
        }
      })
    }
  }
}
</script>
