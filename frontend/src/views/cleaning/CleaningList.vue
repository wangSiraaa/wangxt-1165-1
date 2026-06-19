<template>
  <div class="container">
    <div class="page-header">
      <span class="title">清洗管理</span>
      <el-button type="primary" style="float: right;" @click="showStartDialog = true">
        <i class="el-icon-plus"></i> 开始清洗
      </el-button>
    </div>

    <el-card>
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="奶罐">
            <el-select v-model="searchForm.tankId" placeholder="全部" clearable style="width: 150px;">
              <el-option
                v-for="tank in tankList"
                :key="tank.id"
                :label="tank.tankCode + ' - ' + tank.tankName"
                :value="tank.id">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="是否合格">
            <el-select v-model="searchForm.isQualified" placeholder="全部" clearable style="width: 150px;">
              <el-option label="合格" :value="1" />
              <el-option label="不合格" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="tankName" label="奶罐" width="150" />
        <el-table-column label="清洗状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isQualified === null ? 'warning' : (scope.row.isQualified === 1 ? 'success' : 'danger')" size="small">
              {{ scope.row.isQualified === null ? '清洗中' : (scope.row.isQualified === 1 ? '清洗合格' : '清洗不合格') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cleaningMethod" label="清洗方式" width="120" />
        <el-table-column prop="cleaningAgent" label="清洗剂" width="120" />
        <el-table-column prop="waterTemperature" label="水温(°C)" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.startTime | formatDate }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.endTime | formatDate }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="finishCleaning(scope.row)"
              :disabled="scope.row.isQualified !== null">
              {{ scope.row.isQualified === null ? '完成清洗' : '已完成' }}
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

    <el-dialog title="开始清洗奶罐" :visible.sync="showStartDialog" width="500px">
      <el-form :model="startForm" :rules="startRules" ref="startForm" label-width="100px">
        <el-form-item label="选择奶罐" prop="tankId">
          <el-select
            v-model="startForm.tankId"
            placeholder="请选择待清洗的奶罐"
            style="width: 100%;"
            filterable>
            <el-option
              v-for="tank in dirtyTanks"
              :key="tank.id"
              :label="`${tank.tankCode} - ${tank.tankName} (${tank.tankStatus | formatStatus('tank')})`"
              :value="tank.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="清洗方式">
          <el-select v-model="startForm.cleaningMethod" placeholder="请选择清洗方式" style="width: 100%;">
            <el-option label="高温消毒" value="高温消毒" />
            <el-option label="化学清洗" value="化学清洗" />
            <el-option label="CIP清洗" value="CIP清洗" />
            <el-option label="人工清洗" value="人工清洗" />
          </el-select>
        </el-form-item>
        <el-form-item label="清洗剂">
          <el-input v-model="startForm.cleaningAgent" placeholder="请输入清洗剂（选填）" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="startForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%;" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showStartDialog = false">取消</el-button>
        <el-button type="primary" :loading="startLoading" @click="submitStart">开始清洗</el-button>
      </div>
    </el-dialog>

    <el-dialog title="完成清洗" :visible.sync="showFinishDialog" width="500px">
      <el-form :model="finishForm" :rules="finishRules" ref="finishForm" label-width="100px">
        <el-form-item label="水温(°C)" prop="waterTemperature">
          <el-input-number v-model="finishForm.waterTemperature" :min="0" :max="100" :precision="1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="是否合格" prop="isQualified">
          <el-radio-group v-model="finishForm.isQualified">
            <el-radio :label="1">合格</el-radio>
            <el-radio :label="0">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="finishForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="finishForm.remark" :rows="2" placeholder="请输入备注（选填）" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showFinishDialog = false">取消</el-button>
        <el-button type="primary" :loading="finishLoading" @click="submitFinish">确认完成</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCleaningList, startCleaning, finishCleaning } from '@/api/cleaning'
import { getTankList } from '@/api/tank'

export default {
  name: 'CleaningList',
  data() {
    return {
      loading: false,
      startLoading: false,
      finishLoading: false,
      tableData: [],
      tankList: [],
      dirtyTanks: [],
      searchForm: {
        tankId: '',
        isQualified: ''
      },
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      showStartDialog: false,
      showFinishDialog: false,
      currentCleaning: null,
      startForm: {
        tankId: null,
        cleaningMethod: '',
        cleaningAgent: '',
        startTime: null
      },
      startRules: {
        tankId: [{ required: true, message: '请选择奶罐', trigger: 'change' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
      },
      finishForm: {
        waterTemperature: null,
        isQualified: null,
        endTime: null,
        remark: ''
      },
      finishRules: {
        waterTemperature: [{ required: true, message: '请输入水温', trigger: 'blur' }],
        isQualified: [{ required: true, message: '请选择是否合格', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.loadData()
    this.loadTanks()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pagination.pageNum,
          pageSize: this.pagination.pageSize,
          tankId: this.searchForm.tankId || undefined,
          isQualified: this.searchForm.isQualified !== '' ? this.searchForm.isQualified : undefined
        }
        const res = await getCleaningList(params)
        this.tableData = res.data?.records || []
        this.pagination.total = res.data?.total || 0
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    async loadTanks() {
      try {
        const res = await getTankList({ pageNum: 1, pageSize: 100 })
        this.tankList = res.data?.records || []
        this.dirtyTanks = this.tankList.filter(t => t.tankStatus === 'PENDING_CLEAN' || t.tankStatus === 'IDLE')
      } catch (error) {
        console.error('加载奶罐列表失败:', error)
      }
    },
    resetSearch() {
      this.searchForm = { tankId: '', isQualified: '' }
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
    finishCleaning(row) {
      this.currentCleaning = row
      this.finishForm = {
        waterTemperature: null,
        isQualified: null,
        endTime: null,
        remark: ''
      }
      this.showFinishDialog = true
    },
    async submitStart() {
      this.$refs.startForm.validate(async valid => {
        if (valid) {
          this.startLoading = true
          try {
            await startCleaning(this.startForm)
            this.$message.success('开始清洗成功')
            this.showStartDialog = false
            this.loadData()
            this.loadTanks()
          } catch (error) {
            console.error('开始清洗失败:', error)
          } finally {
            this.startLoading = false
          }
        }
      })
    },
    async submitFinish() {
      this.$refs.finishForm.validate(async valid => {
        if (valid) {
          this.finishLoading = true
          try {
            await finishCleaning(this.currentCleaning.id, this.finishForm)
            this.$message.success(this.finishForm.isQualified === 1 ? '清洗合格，奶罐已可用' : '清洗不合格，请重新清洗')
            this.showFinishDialog = false
            this.loadData()
            this.loadTanks()
          } catch (error) {
            console.error('完成清洗失败:', error)
          } finally {
            this.finishLoading = false
          }
        }
      })
    }
  }
}
</script>
