<template>
  <div class="mobile-container">
    <div class="mobile-header">
      <div class="header-back" @click="$router.back()">
        <i class="el-icon-arrow-left"></i>
      </div>
      <div class="header-title">确认装车</div>
      <div class="header-action"></div>
    </div>

    <div class="mobile-content">
      <div class="mobile-alert alert-danger">
        <p><strong>业务规则提醒：</strong></p>
        <p>• 快检未通过的批次不能装车</p>
        <p>• 装车确认后，检测结果将被锁定，不能修改为合格</p>
        <p>• 装车后奶罐状态将变为待清洗</p>
      </div>

      <div class="mobile-card">
        <form class="mobile-form" @submit.prevent="submitForm">
          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>选择批次
            </label>
            <div class="form-select-wrapper">
              <select
                v-model="form.batchId"
                class="form-input"
                :class="{ error: errors.batchId }"
                @change="onBatchChange">
                <option value="">请选择检测合格的批次</option>
                <option
                  v-for="batch in passedBatches"
                  :key="batch.id"
                  :value="batch.id">
                  {{ batch.batchNo }} - {{ batch.milkTankName }} - {{ batch.milkVolume }}kg
                </option>
              </select>
            </div>
            <div v-if="errors.batchId" class="error-msg">{{ errors.batchId }}</div>
          </div>

          <div v-if="selectedBatch" class="mobile-card" style="margin-top: 0; background: #f5f7fa;">
            <div class="mobile-card-header" style="border-bottom-color: #e4e7ed;">
              <div class="card-title">批次检测状态</div>
              <span
                class="mobile-tag"
                :class="selectedBatch.batchStatus === 'TEST_PASSED' ? 'tag-success' : 'tag-danger'">
                {{ selectedBatch.batchStatus | formatStatus('batch') }}
              </span>
            </div>
            <div class="mobile-card-content">
              <div class="info-row">
                <span class="label">批次号</span>
                <span class="value">{{ selectedBatch.batchNo }}</span>
              </div>
              <div class="info-row">
                <span class="label">奶罐</span>
                <span class="value">{{ selectedBatch.milkTankName }}</span>
              </div>
              <div class="info-row">
                <span class="label">总奶量</span>
                <span class="value">{{ selectedBatch.milkVolume }}kg</span>
              </div>
              <div v-if="selectedBatch.batchStatus !== 'TEST_PASSED'" class="info-row" style="color: #f56c6c;">
                <span>该批次检测未通过，不能装车！</span>
              </div>
            </div>
          </div>

          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>车牌号
            </label>
            <input
              v-model="form.plateNo"
              type="text"
              class="form-input"
              placeholder="请输入车牌号"
              :class="{ error: errors.plateNo }" />
            <div v-if="errors.plateNo" class="error-msg">{{ errors.plateNo }}</div>
          </div>

          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>司机姓名
            </label>
            <input
              v-model="form.driverName"
              type="text"
              class="form-input"
              placeholder="请输入司机姓名"
              :class="{ error: errors.driverName }" />
            <div v-if="errors.driverName" class="error-msg">{{ errors.driverName }}</div>
          </div>

          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>装车量(kg)
            </label>
            <input
              v-model.number="form.loadedVolume"
              type="number"
              min="1"
              :max="selectedBatch?.milkVolume || 10000"
              class="form-input"
              placeholder="请输入装车量"
              :class="{ error: errors.loadedVolume }" />
            <div v-if="selectedBatch" style="color: #909399; font-size: 12px; margin-top: 6px;">
              批次总奶量：{{ selectedBatch.milkVolume }}kg
            </div>
            <div v-if="errors.loadedVolume" class="error-msg">{{ errors.loadedVolume }}</div>
          </div>

          <div class="form-item">
            <label class="form-label">运输目的地</label>
            <input
              v-model="form.destination"
              type="text"
              class="form-input"
              placeholder="请输入运输目的地（选填）" />
          </div>

          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>装车时间
            </label>
            <input
              v-model="form.loadingTime"
              type="datetime-local"
              class="form-input"
              :class="{ error: errors.loadingTime }" />
            <div v-if="errors.loadingTime" class="error-msg">{{ errors.loadingTime }}</div>
          </div>

          <div class="form-item">
            <label class="form-label">封铅号</label>
            <input
              v-model="form.sealNo"
              type="text"
              class="form-input"
              placeholder="请输入封铅号（选填）" />
          </div>

          <div class="form-item">
            <label class="form-label">备注</label>
            <textarea
              v-model="form.remark"
              class="form-textarea"
              placeholder="请输入备注信息（选填）"></textarea>
          </div>

          <button
            type="submit"
            class="mobile-btn btn-primary"
            :disabled="loading || (selectedBatch && selectedBatch.batchStatus !== 'TEST_PASSED')">
            {{ loading ? '提交中...' : '确认装车' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { confirmLoading } from '@/api/loading'
import { getBatchList } from '@/api/batch'
import { mapGetters } from 'vuex'

export default {
  name: 'MobileLoadingConfirm',
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
        loadingTime: '',
        sealNo: '',
        remark: ''
      },
      errors: {}
    }
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  mounted() {
    this.loadPassedBatches()
    this.setDefaultTime()
    this.setDefaultDriver()
  },
  methods: {
    setDefaultTime() {
      const now = new Date()
      this.form.loadingTime = this.formatDateTimeLocal(now)
    },
    setDefaultDriver() {
      if (this.userInfo?.username) {
        this.form.driverName = this.userInfo.username
      }
    },
    formatDateTimeLocal(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day}T${hours}:${minutes}`
    },
    async loadPassedBatches() {
      try {
        const res = await getBatchList({ pageNum: 1, pageSize: 100, status: 'TEST_PASSED' })
        this.passedBatches = res.data?.records || []
      } catch (error) {
        console.error('加载合格批次失败:', error)
      }
    },
    onBatchChange() {
      this.selectedBatch = this.passedBatches.find(b => b.id === this.form.batchId) || null
      if (this.selectedBatch) {
        this.form.loadedVolume = this.selectedBatch.milkVolume
      }
    },
    validate() {
      this.errors = {}
      if (!this.form.batchId) {
        this.errors.batchId = '请选择批次'
      }
      if (this.selectedBatch && this.selectedBatch.batchStatus !== 'TEST_PASSED') {
        this.errors.batchId = '该批次检测未通过，不能装车'
      }
      if (!this.form.plateNo.trim()) {
        this.errors.plateNo = '请输入车牌号'
      }
      if (!this.form.driverName.trim()) {
        this.errors.driverName = '请输入司机姓名'
      }
      if (!this.form.loadedVolume || this.form.loadedVolume <= 0) {
        this.errors.loadedVolume = '请输入有效的装车量'
      }
      if (this.selectedBatch && this.form.loadedVolume > this.selectedBatch.milkVolume) {
        this.errors.loadedVolume = '装车量不能超过批次总奶量'
      }
      if (!this.form.loadingTime) {
        this.errors.loadingTime = '请选择装车时间'
      }
      return Object.keys(this.errors).length === 0
    },
    async submitForm() {
      if (!this.validate()) return
      
      if (this.selectedBatch && this.selectedBatch.batchStatus !== 'TEST_PASSED') {
        this.$message.error('该批次检测未通过，不能装车')
        return
      }
      
      this.loading = true
      try {
        const submitData = {
          ...this.form,
          loadingTime: new Date(this.form.loadingTime).toISOString()
        }
        await confirmLoading(submitData)
        this.$message.success('装车确认成功，检测结果已锁定')
        this.$router.push('/m/loading/list')
      } catch (error) {
        console.error('确认失败:', error)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
