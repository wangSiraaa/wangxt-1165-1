<template>
  <div class="mobile-container">
    <div class="mobile-header">
      <div class="header-back" @click="$router.back()">
        <i class="el-icon-arrow-left"></i>
      </div>
      <div class="header-title">提交批次</div>
      <div class="header-action"></div>
    </div>

    <div class="mobile-content">
      <div class="mobile-alert alert-info">
        <p><strong>业务规则提醒：</strong></p>
        <p>• 同一奶罐未清洗合格不能接新批次</p>
        <p>• 请选择显示为"可用"状态的奶罐</p>
      </div>

      <div class="mobile-card">
        <form class="mobile-form" @submit.prevent="submitForm">
          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>选择奶罐
            </label>
            <div class="form-select-wrapper">
              <select
                v-model="form.milkTankId"
                class="form-input"
                :class="{ error: errors.milkTankId }"
                @change="onTankChange">
                <option value="">请选择奶罐</option>
                <option
                  v-for="tank in availableTanks"
                  :key="tank.id"
                  :value="tank.id"
                  :disabled="tank.tankStatus !== 'IDLE'">
                  {{ tank.tankCode }} - {{ tank.tankName }} ({{ tank.capacity }}kg) - {{ tank.tankStatus === 'IDLE' ? '可用' : '不可用' }}
                </option>
              </select>
            </div>
            <div v-if="errors.milkTankId" class="error-msg">{{ errors.milkTankId }}</div>
          </div>

          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>奶量(kg)
            </label>
            <input
              v-model.number="form.milkVolume"
              type="number"
              min="1"
              max="10000"
              class="form-input"
              placeholder="请输入奶量"
              :class="{ error: errors.milkVolume }" />
            <div v-if="errors.milkVolume" class="error-msg">{{ errors.milkVolume }}</div>
          </div>

          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>温度(°C)
            </label>
            <input
              v-model.number="form.milkTemperature"
              type="number"
              min="0"
              max="50"
              step="0.1"
              class="form-input"
              placeholder="请输入温度"
              :class="{ error: errors.milkTemperature }" />
            <div v-if="errors.milkTemperature" class="error-msg">{{ errors.milkTemperature }}</div>
          </div>

          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>挤奶员
            </label>
            <input
              v-model="form.milker"
              type="text"
              class="form-input"
              placeholder="请输入挤奶员姓名"
              :class="{ error: errors.milker }" />
            <div v-if="errors.milker" class="error-msg">{{ errors.milker }}</div>
          </div>

          <div class="form-item">
            <label class="form-label">牛群编号</label>
            <input
              v-model="form.cowGroupNo"
              type="text"
              class="form-input"
              placeholder="请输入牛群编号（选填）" />
          </div>

          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>挤奶开始时间
            </label>
            <input
              v-model="form.milkStartTime"
              type="datetime-local"
              class="form-input"
              :class="{ error: errors.milkStartTime }" />
            <div v-if="errors.milkStartTime" class="error-msg">{{ errors.milkStartTime }}</div>
          </div>

          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>挤奶结束时间
            </label>
            <input
              v-model="form.milkEndTime"
              type="datetime-local"
              class="form-input"
              :class="{ error: errors.milkEndTime }" />
            <div v-if="errors.milkEndTime" class="error-msg">{{ errors.milkEndTime }}</div>
          </div>

          <div class="form-item">
            <label class="form-label">备注</label>
            <textarea
              v-model="form.remark"
              class="form-textarea"
              placeholder="请输入备注信息（选填）"></textarea>
          </div>

          <button type="submit" class="mobile-btn btn-primary" :disabled="loading">
            {{ loading ? '提交中...' : '提交批次' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { submitBatch } from '@/api/batch'
import { getAvailableTanks, checkTankCleaned } from '@/api/tank'

export default {
  name: 'MobileBatchSubmit',
  data() {
    return {
      loading: false,
      availableTanks: [],
      form: {
        milkTankId: null,
        milkVolume: null,
        milkTemperature: null,
        milker: '',
        cowGroupNo: '',
        milkStartTime: '',
        milkEndTime: '',
        remark: ''
      },
      errors: {}
    }
  },
  mounted() {
    this.loadAvailableTanks()
    this.setDefaultTimes()
  },
  methods: {
    setDefaultTimes() {
      const now = new Date()
      const startTime = new Date(now.getTime() - 2 * 60 * 60 * 1000)
      this.form.milkStartTime = this.formatDateTimeLocal(startTime)
      this.form.milkEndTime = this.formatDateTimeLocal(now)
    },
    formatDateTimeLocal(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day}T${hours}:${minutes}`
    },
    async loadAvailableTanks() {
      try {
        const res = await getAvailableTanks()
        this.availableTanks = res.data || []
      } catch (error) {
        console.error('加载可用奶罐失败:', error)
      }
    },
    async onTankChange() {
      if (this.form.milkTankId) {
        try {
          const res = await checkTankCleaned(this.form.milkTankId)
          if (!res.data?.cleaned) {
            this.$message.warning('该奶罐未清洗合格，请选择其他奶罐')
            this.form.milkTankId = null
          }
        } catch (error) {
          console.error('检查奶罐清洗状态失败:', error)
        }
      }
    },
    validate() {
      this.errors = {}
      if (!this.form.milkTankId) {
        this.errors.milkTankId = '请选择奶罐'
      }
      if (!this.form.milkVolume || this.form.milkVolume <= 0) {
        this.errors.milkVolume = '请输入有效的奶量'
      }
      if (this.form.milkTemperature === null || this.form.milkTemperature < 0 || this.form.milkTemperature > 50) {
        this.errors.milkTemperature = '请输入有效的温度(0-50°C)'
      }
      if (!this.form.milker.trim()) {
        this.errors.milker = '请输入挤奶员'
      }
      if (!this.form.milkStartTime) {
        this.errors.milkStartTime = '请选择挤奶开始时间'
      }
      if (!this.form.milkEndTime) {
        this.errors.milkEndTime = '请选择挤奶结束时间'
      }
      if (this.form.milkStartTime && this.form.milkEndTime && 
          new Date(this.form.milkStartTime) >= new Date(this.form.milkEndTime)) {
        this.errors.milkEndTime = '结束时间必须晚于开始时间'
      }
      return Object.keys(this.errors).length === 0
    },
    async submitForm() {
      if (!this.validate()) return
      
      this.loading = true
      try {
        const submitData = {
          ...this.form,
          milkStartTime: new Date(this.form.milkStartTime).toISOString(),
          milkEndTime: new Date(this.form.milkEndTime).toISOString()
        }
        await submitBatch(submitData)
        this.$message.success('批次提交成功')
        this.$router.push('/m/batch/list')
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
