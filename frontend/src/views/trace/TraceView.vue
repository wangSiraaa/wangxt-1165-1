<template>
  <div class="container">
    <div class="page-header">
      <span class="title">全链路追踪</span>
    </div>

    <el-card>
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="查询方式">
            <el-radio-group v-model="searchType">
              <el-radio label="batch">按批次查询</el-radio>
              <el-radio label="tank">按奶罐查询</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="searchType === 'batch'" label="批次号">
            <el-select
              v-model="searchForm.batchId"
              placeholder="请选择批次"
              filterable
              style="width: 280px;"
              @change="onBatchSelect">
              <el-option
                v-for="batch in batchOptions"
                :key="batch.id"
                :label="`${batch.batchNo} - ${batch.milkVolume}升`"
                :value="batch.id" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="searchType === 'tank'" label="奶罐">
            <el-select
              v-model="searchForm.tankId"
              placeholder="请选择奶罐"
              filterable
              style="width: 200px;"
              @change="onTankSelect">
              <el-option
                v-for="tank in tankOptions"
                :key="tank.id"
                :label="tank.tankName"
                :value="tank.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchTrace" :loading="loading">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <div v-if="traceData" style="margin-top: 20px;">
      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 30px;">
        <el-step title="挤奶批次" icon="el-icon-s-operation" />
        <el-step title="奶罐清洗" icon="el-icon-brush" />
        <el-step title="快检检测" icon="el-icon-s-check" />
        <el-step title="装车运输" icon="el-icon-truck" />
      </el-steps>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover">
            <div slot="header">
              <span><i class="el-icon-s-operation" style="color: #409EFF;"></i> 奶罐信息</span>
            </div>
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item label="奶罐名称">{{ traceData.tankInfo.tankName }}</el-descriptions-item>
              <el-descriptions-item label="奶罐编号">{{ traceData.tankInfo.tankCode }}</el-descriptions-item>
              <el-descriptions-item label="容量(升)">{{ traceData.tankInfo.capacity }}</el-descriptions-item>
              <el-descriptions-item label="当前状态">
                <el-tag :type="getTankStatusType(traceData.tankInfo.tankStatus)" size="mini">
                  {{ traceData.tankInfo.tankStatusDesc }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="所在牧场">{{ traceData.pastureName || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="hover">
            <div slot="header">
              <span><i class="el-icon-time" style="color: #67C23A;"></i> 批次信息</span>
            </div>
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item label="批次号">{{ traceData.batchNo }}</el-descriptions-item>
              <el-descriptions-item label="奶量(升)">{{ traceData.batchInfo.milkVolume }}</el-descriptions-item>
              <el-descriptions-item label="温度(°C)">{{ traceData.batchInfo.milkTemperature }}</el-descriptions-item>
              <el-descriptions-item label="牛群数量">{{ traceData.batchInfo.cowCount || '-' }}</el-descriptions-item>
              <el-descriptions-item label="批次状态">
                <el-tag :type="getBatchStatusType(traceData.batchInfo.batchStatus)" size="mini">
                  {{ traceData.batchInfo.batchStatusDesc }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="挤奶员">{{ traceData.batchInfo.milkerName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="挤奶开始时间">{{ traceData.batchInfo.milkingStartTime | formatDate }}</el-descriptions-item>
              <el-descriptions-item label="挤奶结束时间">{{ traceData.batchInfo.milkingEndTime | formatDate }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card shadow="hover">
            <div slot="header">
              <span><i class="el-icon-brush" style="color: #E6A23C;"></i> 上一次清洗记录</span>
              <el-tag v-if="traceData.cleaningInfo" size="mini" type="success">已清洗</el-tag>
              <el-tag v-else size="mini" type="warning">待清洗</el-tag>
            </div>
            <el-descriptions v-if="traceData.cleaningInfo" :column="1" size="small" border>
              <el-descriptions-item label="清洗单号">{{ traceData.cleaningInfo.cleaningNo }}</el-descriptions-item>
              <el-descriptions-item label="清洗方式">{{ traceData.cleaningInfo.cleaningMethod || '-' }}</el-descriptions-item>
              <el-descriptions-item label="清洗开始时间">{{ traceData.cleaningInfo.cleaningStartTime | formatDate }}</el-descriptions-item>
              <el-descriptions-item label="清洗结束时间">{{ traceData.cleaningInfo.cleaningEndTime | formatDate }}</el-descriptions-item>
              <el-descriptions-item label="清洗人">{{ traceData.cleaningInfo.operatorName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="确认状态">
                <el-tag :type="traceData.cleaningInfo.confirmStatus === 'CONFIRMED' ? 'success' : 'warning'" size="mini">
                  {{ traceData.cleaningInfo.confirmStatusDesc }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="检查人">{{ traceData.cleaningInfo.checkerName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="检查时间">{{ traceData.cleaningInfo.checkTime | formatDate }}</el-descriptions-item>
            </el-descriptions>
            <el-empty v-else description="暂无清洗记录" :image-size="80" />
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="hover">
            <div slot="header">
              <span><i class="el-icon-s-check" style="color: #F56C6C;"></i> 检测记录</span>
              <el-tag v-if="quickTest" size="mini" :type="quickTest.testResult === 'PASSED' ? 'success' : 'danger'">
                快检：{{ quickTest.testResultDesc }}
              </el-tag>
            </div>
            <el-descriptions v-if="quickTest" :column="1" size="small" border>
              <el-descriptions-item label="检测单号">{{ quickTest.testNo }}</el-descriptions-item>
              <el-descriptions-item label="检测类别">
                <el-tag :type="quickTest.testCategory === 'QUICK' ? 'primary' : 'warning'" size="mini">
                  {{ quickTest.testCategoryDesc }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="检测项目">{{ quickTest.testType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="检测结果">
                <el-tag :type="quickTest.testResult === 'PASSED' ? 'success' : 'danger'" size="mini">
                  {{ quickTest.testResultDesc }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="检测值">{{ quickTest.testValue || '-' }}</el-descriptions-item>
              <el-descriptions-item label="化验员">{{ quickTest.labName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="检测时间">{{ quickTest.testTime | formatDate }}</el-descriptions-item>
              <el-descriptions-item label="审批状态">
                <el-tag :type="quickTest.approveStatus === 'APPROVED' ? 'success' : 'warning'" size="mini">
                  {{ quickTest.approveStatusDesc }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="锁定状态">
                <el-tag :type="quickTest.isLocked === 1 ? 'info' : 'warning'" size="mini">
                  {{ quickTest.isLocked === 1 ? '已锁定' : '可修改' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
            <el-empty v-else description="暂无检测记录" :image-size="80" />

            <div v-if="recheckList && recheckList.length > 0" style="margin-top: 15px;">
              <div style="font-weight: bold; margin-bottom: 10px; color: #606266;">
                <i class="el-icon-document-copy"></i> 复检记录 ({{ recheckList.length }})
              </div>
              <el-table :data="recheckList" border size="mini" max-height="200">
                <el-table-column prop="testNo" label="复检单号" width="150" />
                <el-table-column prop="testType" label="检测项" width="100" />
                <el-table-column prop="testResult" label="结果" width="70">
                  <template slot-scope="scope">
                    <el-tag :type="scope.row.testResult === 'PASSED' ? 'success' : 'danger'" size="mini">
                      {{ scope.row.testResult === 'PASSED' ? '合格' : '不合格' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="testValue" label="检测值" width="80" />
                <el-table-column prop="labName" label="化验员" width="90" />
                <el-table-column prop="testTime" label="检测时间" width="150">
                  <template slot-scope="scope">
                    {{ scope.row.testTime | formatDate }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card shadow="hover">
            <div slot="header">
              <span><i class="el-icon-truck" style="color: #909399;"></i> 装车运输</span>
              <el-tag v-if="traceData.loadingInfo" size="mini" type="success">已装车</el-tag>
              <el-tag v-else size="mini" type="info">待装车</el-tag>
            </div>
            <el-descriptions v-if="traceData.loadingInfo" :column="1" size="small" border>
              <el-descriptions-item label="装车单号">{{ traceData.loadingInfo.loadingNo }}</el-descriptions-item>
              <el-descriptions-item label="车牌号">{{ traceData.loadingInfo.truckNo }}</el-descriptions-item>
              <el-descriptions-item label="司机">{{ traceData.loadingInfo.driverName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="运输责任人">{{ traceData.loadingInfo.transporterName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="装车量(升)">{{ traceData.loadingInfo.loadVolume }}</el-descriptions-item>
              <el-descriptions-item label="目的地">{{ traceData.loadingInfo.destination || '-' }}</el-descriptions-item>
              <el-descriptions-item label="装车时间">{{ traceData.loadingInfo.loadTime | formatDate }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ traceData.loadingInfo.loadingStatusDesc }}</el-descriptions-item>
            </el-descriptions>
            <el-empty v-else description="暂无装车记录" :image-size="80" />
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="hover">
            <div slot="header">
              <span><i class="el-icon-close" style="color: #F56C6C;"></i> 拒收记录</span>
              <el-tag v-if="traceData.rejectInfo" size="mini" type="danger">已拒收</el-tag>
              <el-tag v-else size="mini" type="success">正常</el-tag>
            </div>
            <el-descriptions v-if="traceData.rejectInfo" :column="1" size="small" border>
              <el-descriptions-item label="拒收单号">{{ traceData.rejectInfo.rejectNo }}</el-descriptions-item>
              <el-descriptions-item label="拒收类型">
                <el-tag type="danger" size="mini">{{ traceData.rejectInfo.rejectTypeDesc }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="拒收原因">{{ traceData.rejectInfo.rejectReason }}</el-descriptions-item>
              <el-descriptions-item label="拒收人">{{ traceData.rejectInfo.operatorName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="拒收时间">{{ traceData.rejectInfo.rejectTime | formatDate }}</el-descriptions-item>
              <el-descriptions-item label="处理状态">
                <el-tag :type="traceData.rejectInfo.handleStatus === 'RESOLVED' ? 'success' : 'warning'" size="mini">
                  {{ traceData.rejectInfo.handleStatusDesc }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="处理结果">{{ traceData.rejectInfo.handleResult || '-' }}</el-descriptions-item>
              <el-descriptions-item label="处理人">{{ traceData.rejectInfo.handlerName || '-' }}</el-descriptions-item>
            </el-descriptions>
            <el-empty v-else description="无拒收记录" :image-size="80" />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-empty v-else-if="!loading && !traceData" description="请选择批次或奶罐进行查询" style="margin-top: 80px;" />
  </div>
</template>

<script>
import { getTraceByBatchId, getTraceByTankId } from '@/api/trace'
import { getBatchList } from '@/api/batch'
import { getTankList } from '@/api/tank'

export default {
  name: 'TraceView',
  data() {
    return {
      loading: false,
      searchType: 'batch',
      searchForm: {
        batchId: null,
        tankId: null
      },
      batchOptions: [],
      tankOptions: [],
      traceData: null,
      quickTest: null,
      recheckList: [],
      currentStep: 0
    }
  },
  mounted() {
    this.loadBatchOptions()
    this.loadTankOptions()
  },
  methods: {
    async loadBatchOptions() {
      try {
        const res = await getBatchList({ pageNum: 1, pageSize: 100 })
        this.batchOptions = res.data?.records || []
      } catch (error) {
        console.error('加载批次列表失败:', error)
      }
    },
    async loadTankOptions() {
      try {
        const res = await getTankList({ pageNum: 1, pageSize: 100 })
        this.tankOptions = res.data?.records || []
      } catch (error) {
        console.error('加载奶罐列表失败:', error)
      }
    },
    onBatchSelect() {
      this.traceData = null
      this.quickTest = null
      this.recheckList = []
    },
    onTankSelect() {
      this.traceData = null
      this.quickTest = null
      this.recheckList = []
    },
    async searchTrace() {
      if (this.searchType === 'batch' && !this.searchForm.batchId) {
        this.$message.warning('请选择批次')
        return
      }
      if (this.searchType === 'tank' && !this.searchForm.tankId) {
        this.$message.warning('请选择奶罐')
        return
      }

      this.loading = true
      this.traceData = null
      this.quickTest = null
      this.recheckList = []
      try {
        let res
        if (this.searchType === 'batch') {
          res = await getTraceByBatchId(this.searchForm.batchId)
        } else {
          res = await getTraceByTankId(this.searchForm.tankId)
        }
        this.traceData = res.data
        this.processTestData()
        this.calculateStep()
      } catch (error) {
        console.error('查询追踪信息失败:', error)
        this.$message.error('查询追踪信息失败')
      } finally {
        this.loading = false
      }
    },
    processTestData() {
      if (!this.traceData?.testInfoList || this.traceData.testInfoList.length === 0) {
        return
      }
      const tests = this.traceData.testInfoList
      this.quickTest = tests.find(t => t.testCategory === 'QUICK') || tests[0]
      this.recheckList = tests.filter(t => t.testCategory === 'RECHECK')
    },
    calculateStep() {
      let step = 0
      if (this.traceData?.batchInfo) {
        step = 1
      }
      if (this.traceData?.cleaningInfo) {
        step = 2
      }
      if (this.quickTest) {
        step = 3
      }
      if (this.traceData?.loadingInfo) {
        step = 4
      }
      this.currentStep = Math.min(step, 4)
    },
    getTankStatusType(status) {
      const map = {
        IDLE: 'info',
        IN_USE: 'success',
        TO_CLEAN: 'warning',
        CLEANING: 'primary'
      }
      return map[status] || 'info'
    },
    getBatchStatusType(status) {
      const map = {
        MILKING: 'primary',
        PENDING_TEST: 'warning',
        TEST_PASSED: 'success',
        TEST_FAILED: 'danger',
        LOADED: 'success',
        REJECTED: 'danger'
      }
      return map[status] || 'info'
    }
  }
}
</script>

<style scoped>
.search-bar {
  padding: 10px 0;
}
</style>
