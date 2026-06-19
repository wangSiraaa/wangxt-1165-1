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
              :label="`${batch.batchNo} - ${batch.milkTankName} - ${batch.milkVolume}kg`"
              :value="batch.id">
            </el-option>
          </el-select>
          <div v-if="selectedBatch" style="margin-top: 10px;">
            <el-alert
              title="批次检测状态"
              :type="selectedBatch.batchStatus === 'TEST_PASSED' ? 'success' : 'error'"
              :closable="false">
              <p>批次状态：{{ selectedBatch.batchStatus | formatStatus('batch') }}</p>
              <p v-if="selectedBatch.batchStatus !== 'TEST_PASSED'" style="color: #f56c6c;">
                该批次检测未通过，不能装车！
              </p>
            </el-alert>
          </div>
        </el-form-item>

        <el-form-item label="车牌号" prop="plateNo">
          <el-input v-model="form.plateNo" placeholder="请输入车牌号" />
        </el-form-item>

        <el-form-item label="司机姓名" prop="driverName">
          <el-input v-model="form.driverName" placeholder="请输入司机姓名" />
        </el-form-item>

        <el-form-item label="装车量(kg)" prop="loadedVolume">
          <el-input-number v-model="form.loadedVolume" :min="1" :max="selectedBatch?.milkVolume || 10000" style="width: 100%;" />
          <span v-if="selectedBatch" style="color: #909399; font-size: 12px;">
            批次总奶量：{{ selectedBatch.milkVolume }}kg
          </span>
        </el-form-item>

        <el-form-item label="运输目的地">
          <el-input v-model="form.destination" placeholder="请输入运输目的地（选填）" />
        </el-form-item>

        <el-form-item label="装车时间" prop="loadingTime">
          <el-date-picker
            v-model="form.loadingTime"
            type="datetime"
            placeholder="选择装车时间"
            style="width: 100%;" />
        </el-form-item>

        <el-form-item label="封铅号">
          <el-input v-model="form.sealNo" placeholder="请输入封铅号（选填）" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.remark" :rows="3" placeholder="请输入备注信息（选填）" />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="submitForm"
            :loading="loading"
            :disabled="selectedBatch && selectedBatch.batchStatus !== 'TEST_PASSED'">
            确认装车
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { confirmLoading } from '@/api/loading'
import { getBatchList } from '@/api/batch'

export default {
  name: 'LoadingConfirm',
  data() {
    return {
      loading: false,
      passedBatches: [],
      selectedBatch: null,
      form: {
        batchId: null,
        plateNo: '',
        driverName: '',
        loadedVolume: null,
        destination: '',
        loadingTime: null,
        sealNo: '',
        remark: ''
      },
      rules: {
        batchId: [{ required: true, message: '请选择批次', trigger: 'change' }],
        plateNo: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
        driverName: [{ required: true, message: '请输入司机姓名', trigger: 'blur' }],
        loadedVolume: [{ required: true, message: '请输入装车量', trigger: 'blur' }],
        loadingTime: [{ required: true, message: '请选择装车时间', trigger: 'change' }]
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
      }
    },
    onBatchChange(batchId) {
      this.selectedBatch = this.passedBatches.find(b => b.id === batchId) || null
      if (this.selectedBatch) {
        this.form.loadedVolume = this.selectedBatch.milkVolume
      }
    },
    async submitForm() {
      this.$refs.loadingForm.validate(async valid => {
        if (valid) {
          if (this.selectedBatch && this.selectedBatch.batchStatus !== 'TEST_PASSED') {
            this.$message.error('该批次检测未通过，不能装车')
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
      this.selectedBatch = null
    }
  }
}
</script>
