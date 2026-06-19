<template>
  <div class="container">
    <div class="page-header">
      <span class="title">确认装车</span>
      <el-button @click="$router.back()">
        <i class="el-icon-arrow-left"></i> 返回
      </el-button>
    </div>

    <div class="form-container">
      <el-form :model="form" :rules="rules" ref="loadingForm" label-width="120px">
        <el-alert
          title="业务规则提醒"
          type="error"
          :closable="false"
          style="margin-bottom: 20px;">
          <p>• 快检未通过的批次不能装车</p>
          <p>• 装车确认后，检测结果将被锁定，不能修改为合格</p>
          <p>• 装车后奶罐状态将变为待清洗</p>
        </el-alert>

        <el-form-item label="选择批次" prop="batchId">
          <el-select v-model="form.batchId" placeholder="请选择检测合格的批次" style="width: 100%;" filterable @change="onBatchChange">
            <el-option
              v-for="batch in passedBatches"
              :key="batch.id"
              :label="`${batch.batchNo} - ${batch.milkVolume}升`"
              :value="batch.id">
            </el-option>
          </el-select>

          <div v-if="entranceStatus" style="margin-top: 15px;">
            <el-alert
              :title="entranceStatus.isLocked ? '装车入口已锁定' : '批次检测状态'"
              :type="entranceStatus.canLoad ? 'success' : 'error'"
              :closable="false">
              <p>批次状态：{{ entranceStatus.batchStatusDesc }}</p>
              <p v-if="entranceStatus.latestTestResult">
                最新检测结果：
                <el-tag :type="entranceStatus.latestTestResult === 'PASSED' ? 'success' : 'danger'" size="mini">
                  {{ entranceStatus.latestTestResultDesc }}
                </el-tag>
              </p>
              <p v-if="entranceStatus.testTime">检测时间：{{ entranceStatus.testTime | formatDate }}</p>
              <p v-if="entranceStatus.isLocked" style="color: #f56c6c; font-weight: bold; margin-top: 8px;">
                <i class="el-icon-lock"></i> 入口已锁定
              </p>
              <p v-if="entranceStatus.lockReason" style="margin-top: 5px; color: #f56c6c;">
                原因：{{ entranceStatus.lockReason }}
              </p>
              <p v-if="entranceStatus.rejectType" style="margin-top: 5px;">
                拒收类型：{{ entranceStatus.rejectTypeDesc }}
              </p>
            </el-alert>
          </div>
        </el-form-item>

        <el-form-item label="车牌号" prop="truckNo">
          <el-input v-model="form.truckNo" placeholder="请输入车牌号" />
        </el-form-item>

        <el-form-item label="运输责任人">
          <el-input v-model="form.transporterName" placeholder="请输入运输责任人姓名（选填）" />
        </el-form-item>

        <el-form-item label="装车量(升)" prop="loadVolume">
          <el-input-number v-model="form.loadVolume" :min="1" :max="entranceStatus?.milkVolume || 10000" style="width: 100%;" />
          <span v-if="entranceStatus" style="color: #909399; font-size: 12px;">
            批次总奶量：{{ entranceStatus.milkVolume }}升
          </span>
        </el-form-item>

        <el-form-item label="运输目的地">
          <el-input v-model="form.destination" placeholder="请输入运输目的地（选填）" />
        </el-form-item>

        <el-form-item label="装车时间" prop="loadTime">
          <el-date-picker
            v-model="form.loadTime"
            type="datetime"
            placeholder="选择装车时间"
            style="width: 100%;" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.remark" :rows="3" placeholder="请输入备注信息（选填）" />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="submitForm"
            :loading="loading"
            :disabled="!entranceStatus?.canLoad">
            <i v-if="entranceStatus?.isLocked" class="el-icon-lock"></i>
            确认装车
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { confirmLoading, getLoadingEntranceStatus } from '@/api/loading'
import { getBatchList } from '@/api/batch'

export default {
  name: 'LoadingConfirm',
  data() {
    return {
      loading: false,
      passedBatches: [],
      entranceStatus: null,
      form: {
        batchId: null,
        truckNo: '',
        transporterName: '',
        loadVolume: null,
        destination: '',
        loadTime: null,
        remark: ''
      },
      rules: {
        batchId: [{ required: true, message: '请选择批次', trigger: 'change' }],
        truckNo: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
        loadVolume: [{ required: true, message: '请输入装车量', trigger: 'blur' }],
        loadTime: [{ required: true, message: '请选择装车时间', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.loadPassedBatches()
  },
  methods: {
    async loadPassedBatches() {
      try {
        const res = await getBatchList({ pageNum: 1, pageSize: 100, status: 'TEST_PASSED' })
        this.passedBatches = res.data?.records || []
      } catch (error) {
        console.error('加载合格批次失败:', error)
        this.$message.error('加载合格批次失败')
      }
    },
    async onBatchChange(batchId) {
      this.entranceStatus = null
      if (!batchId) return

      try {
        const res = await getLoadingEntranceStatus(batchId)
        this.entranceStatus = res.data
        if (this.entranceStatus?.milkVolume) {
          this.form.loadVolume = this.entranceStatus.milkVolume
        }
      } catch (error) {
        console.error('获取装车入口状态失败:', error)
      }
    },
    async submitForm() {
      this.$refs.loadingForm.validate(async valid => {
        if (valid) {
          if (!this.entranceStatus?.canLoad) {
            this.$message.error(this.entranceStatus?.lockReason || '该批次不允许装车')
            return
          }
          this.loading = true
          try {
            await confirmLoading(this.form)
            this.$message.success('装车确认成功，检测结果已锁定')
            this.$router.push('/loading')
          } catch (error) {
            console.error('确认失败:', error)
          } finally {
            this.loading = false
          }
        }
      })
    },
    resetForm() {
      this.$refs.loadingForm.resetFields()
      this.entranceStatus = null
    }
  }
}
</script>
