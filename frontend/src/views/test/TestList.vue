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
          <el-form-item label="检测类别">
            <el-select v-model="searchForm.testCategory" placeholder="全部" clearable style="width: 150px;">
              <el-option label="快检" value="QUICK" />
              <el-option label="复检" value="RECHECK" />
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
        <el-table-column prop="testItem" label="检测项目" width="120" />
        <el-table-column prop="testCategory" label="检测类别" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.testCategory === 'QUICK' ? 'primary' : 'warning'" size="small">
              {{ scope.row.testCategory === 'QUICK' ? '快检' : '复检' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="testResult" label="检测结果" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.testResult === 'PASSED' ? 'success' : 'danger'" size="small">
              {{ scope.row.testResult === 'PASSED' ? '合格' : '不合格' }}
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
        <el-table-column prop="testerName" label="化验员" width="120" />
        <el-table-column prop="testTime" label="检测时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.testTime | formatDate }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button
              type="text"
              size="small"
              :disabled="scope.row.isLocked"
              @click="editTest(scope.row)">
              {{ scope.row.isLocked ? '已锁定' : '修改' }}
            </el-button>
            <el-button
              v-if="scope.row.testCategory === 'QUICK'"
              type="text"
              size="small"
              @click="viewRechecks(scope.row)">
              复检记录
            </el-button>
            <el-button
              v-if="scope.row.testCategory === 'QUICK'"
              type="text"
              size="small"
              @click="addRecheck(scope.row)">
              追加复检
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

    <el-dialog title="检测详情" :visible.sync="detailVisible" width="650px">
      <el-descriptions :column="2" border v-if="currentTest">
        <el-descriptions-item label="检测单号">{{ currentTest.testNo }}</el-descriptions-item>
        <el-descriptions-item label="检测类别">
          <el-tag :type="currentTest.testCategory === 'QUICK' ? 'primary' : 'warning'" size="mini">
            {{ currentTest.testCategory === 'QUICK' ? '快检' : '复检' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="批次号">{{ currentTest.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="检测项目">{{ currentTest.testItem }}</el-descriptions-item>
        <el-descriptions-item label="检测结果">
          <el-tag :type="currentTest.testResult === 'PASSED' ? 'success' : 'danger'" size="mini">
            {{ currentTest.testResult === 'PASSED' ? '合格' : '不合格' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentTest.isLocked ? 'info' : 'warning'" size="mini">
            {{ currentTest.isLocked ? '已锁定' : '可修改' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检测值">{{ currentTest.testValue || '-' }}</el-descriptions-item>
        <el-descriptions-item label="参考值">{{ currentTest.referenceValue || '-' }}</el-descriptions-item>
        <el-descriptions-item label="化验员">{{ currentTest.testerName || currentTest.tester || '-' }}</el-descriptions-item>
        <el-descriptions-item label="检测时间">{{ currentTest.testTime | formatDate }}</el-descriptions-item>
        <el-descriptions-item label="检测备注" :span="2">{{ currentTest.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer">
        <el-button v-if="currentTest && !currentTest.isLocked" type="primary" @click="editTest(currentTest)">修改检测结果</el-button>
        <el-button v-if="currentTest && currentTest.testCategory === 'QUICK'" type="success" @click="addRecheck(currentTest)">追加复检</el-button>
        <el-button v-if="currentTest && currentTest.testCategory === 'QUICK'" @click="viewRechecks(currentTest)">查看复检记录</el-button>
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

    <el-dialog title="复检记录" :visible.sync="recheckVisible" width="750px">
      <div v-if="recheckList.length > 0">
        <el-table :data="recheckList" border stripe size="small">
          <el-table-column prop="testNo" label="复检单号" width="160" />
          <el-table-column prop="testItem" label="检测项目" width="120" />
          <el-table-column prop="testResult" label="检测结果" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.testResult === 'PASSED' ? 'success' : 'danger'" size="mini">
                {{ scope.row.testResult === 'PASSED' ? '合格' : '不合格' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="testValue" label="检测值" width="100" />
          <el-table-column prop="testerName" label="化验员" width="120" />
          <el-table-column prop="testTime" label="检测时间" width="160">
            <template slot-scope="scope">
              {{ scope.row.testTime | formatDate }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template slot-scope="scope">
              <el-button type="text" size="mini" @click="viewRecheckDetail(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-empty v-else description="暂无复检记录" />
      <div slot="footer">
        <el-button type="primary" @click="addRecheck(currentTest)">追加复检</el-button>
        <el-button @click="recheckVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="追加复检" :visible.sync="recheckAddVisible" width="500px">
      <el-alert
        v-if="currentTest?.isLocked"
        title="检测结果已锁定，复检仅追加记录，不会改变原检测结果"
        type="warning"
        :closable="false"
        style="margin-bottom: 15px;" />
      <el-alert
        v-else
        title="复检合格将自动更新批次状态并清理拒收记录"
        type="info"
        :closable="false"
        style="margin-bottom: 15px;" />
      <el-form :model="recheckForm" :rules="recheckRules" ref="recheckForm" label-width="100px">
        <el-form-item label="检测项目" prop="testItem">
          <el-input v-model="recheckForm.testItem" placeholder="请输入检测项目" />
        </el-form-item>
        <el-form-item label="检测结果" prop="testResult">
          <el-select v-model="recheckForm.testResult" style="width: 100%;">
            <el-option label="合格" value="PASSED" />
            <el-option label="不合格" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="检测值" prop="testValue">
          <el-input v-model="recheckForm.testValue" placeholder="请输入检测值（选填）" />
        </el-form-item>
        <el-form-item label="参考值">
          <el-input v-model="recheckForm.referenceValue" placeholder="请输入参考值（选填）" />
        </el-form-item>
        <el-form-item label="检测方法">
          <el-input v-model="recheckForm.testMethod" placeholder="请输入检测方法（选填）" />
        </el-form-item>
        <el-form-item label="检测时间" prop="testTime">
          <el-date-picker
            v-model="recheckForm.testTime"
            type="datetime"
            placeholder="选择检测时间"
            style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="recheckForm.remark" :rows="2" placeholder="请输入备注信息（选填）" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="recheckAddVisible = false">取消</el-button>
        <el-button type="primary" :loading="recheckLoading" @click="submitRecheck">提交复检</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getTestList, updateTest, submitRecheck, getRecheckList } from '@/api/test'

export default {
  name: 'TestList',
  data() {
    return {
      loading: false,
      editLoading: false,
      recheckLoading: false,
      tableData: [],
      searchForm: {
        result: '',
        testCategory: '',
        locked: ''
      },
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      detailVisible: false,
      editVisible: false,
      recheckVisible: false,
      recheckAddVisible: false,
      currentTest: null,
      recheckList: [],
      editForm: {
        testResult: '',
        testValue: '',
        remark: ''
      },
      editRules: {
        testResult: [{ required: true, message: '请选择检测结果', trigger: 'change' }],
        remark: [{ required: true, message: '请输入修改原因', trigger: 'blur' }]
      },
      recheckForm: {
        testItem: '',
        testResult: '',
        testValue: '',
        referenceValue: '',
        testMethod: '',
        testTime: null,
        remark: ''
      },
      recheckRules: {
        testItem: [{ required: true, message: '请输入检测项目', trigger: 'blur' }],
        testResult: [{ required: true, message: '请选择检测结果', trigger: 'change' }],
        testTime: [{ required: true, message: '请选择检测时间', trigger: 'change' }]
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
          result: this.searchForm.result || undefined,
          testCategory: this.searchForm.testCategory || undefined
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
      this.searchForm = { result: '', testCategory: '', locked: '' }
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
    },
    async viewRechecks(row) {
      this.currentTest = row
      this.detailVisible = false
      this.recheckVisible = true
      this.recheckList = []
      try {
        const res = await getRecheckList(row.id, { pageNum: 1, pageSize: 50 })
        this.recheckList = res.data?.records || res.data || []
      } catch (error) {
        console.error('加载复检记录失败:', error)
        this.$message.error('加载复检记录失败')
      }
    },
    viewRecheckDetail(row) {
      this.$message.info('复检详情功能开发中')
    },
    addRecheck(row) {
      this.currentTest = row
      this.detailVisible = false
      this.recheckVisible = false
      this.recheckForm = {
        testItem: row.testItem || '',
        testResult: '',
        testValue: '',
        referenceValue: row.referenceValue || '',
        testMethod: '',
        testTime: new Date(),
        remark: ''
      }
      this.recheckAddVisible = true
    },
    async submitRecheck() {
      this.$refs.recheckForm.validate(async valid => {
        if (valid) {
          this.recheckLoading = true
          try {
            await submitRecheck(this.currentTest.id, this.recheckForm)
            this.$message.success('复检提交成功')
            this.recheckAddVisible = false
            this.loadData()
          } catch (error) {
            console.error('提交复检失败:', error)
          } finally {
            this.recheckLoading = false
          }
        }
      })
    }
  }
}
</script>
