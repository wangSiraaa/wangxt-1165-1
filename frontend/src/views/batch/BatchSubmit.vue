<template>
  <div class="container">
    <div class="page-header">
      <span class="title">提交挤奶批次</span>
      <el-button @click="$router.back()">
        <i class="el-icon-arrow-left"></i> 返回
      </el-button>
    </div>

    <div class="form-container">
      <el-form :model="form" :rules="rules" ref="batchForm" label-width="120px">
        <el-alert
          title="业务规则提醒"
          type="info"
          :closable="false"
          style="margin-bottom: 20px;">
          <p>• 同一奶罐未清洗合格不能接新批次</p>
          <p>• 请选择显示为"可用"状态的奶罐</p>
        </el-alert>

        <el-form-item label="选择奶罐" prop="milkTankId">
          <el-select v-model="form.milkTankId" placeholder="请选择奶罐" style="width: 100%;" @change="onTankChange">
            <el-option
              v-for="tank in availableTanks"
              :key="tank.id"
              :label="`${tank.tankCode} - ${tank.tankName} (${tank.capacity}kg)`"
              :value="tank.id"
              :disabled="tank.tankStatus !== 'IDLE'">
              <span>{{ tank.tankCode }} - {{ tank.tankName }}</span>
              <span style="float: right;">
                <el-tag size="mini" :type="tank.tankStatus === 'IDLE' ? 'success' : 'danger'">
                  {{ tank.tankStatus === 'IDLE' ? '可用' : '不可用' }}
                </el-tag>
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="奶量(kg)" prop="milkVolume">
          <el-input-number v-model="form.milkVolume" :min="1" :max="10000" style="width: 100%;" />
        </el-form-item>

        <el-form-item label="温度(°C)" prop="milkTemperature">
          <el-input-number v-model="form.milkTemperature" :min="0" :max="50" :precision="1" style="width: 100%;" />
        </el-form-item>

        <el-form-item label="挤奶员" prop="milker">
          <el-input v-model="form.milker" placeholder="请输入挤奶员姓名" />
        </el-form-item>

        <el-form-item label="牛群编号">
          <el-input v-model="form.cowGroupNo" placeholder="请输入牛群编号（选填）" />
        </el-form-item>

        <el-form-item label="挤奶开始时间" prop="milkStartTime">
          <el-date-picker
            v-model="form.milkStartTime"
            type="datetime"
            placeholder="选择挤奶开始时间"
            style="width: 100%;" />
        </el-form-item>

        <el-form-item label="挤奶结束时间" prop="milkEndTime">
          <el-date-picker
            v-model="form.milkEndTime"
            type="datetime"
            placeholder="选择挤奶结束时间"
            style="width: 100%;" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.remark" :rows="3" placeholder="请输入备注信息（选填）" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">提交批次</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { submitBatch } from '@/api/batch'
import { getAvailableTanks, checkTankCleaned } from '@/api/tank'

export default {
  name: 'BatchSubmit',
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
        milkStartTime: null,
        milkEndTime: null,
        remark: ''
      },
      rules: {
        milkTankId: [{ required: true, message: '请选择奶罐', trigger: 'change' }],
        milkVolume: [{ required: true, message: '请输入奶量', trigger: 'blur' }],
        milkTemperature: [{ required: true, message: '请输入温度', trigger: 'blur' }],
        milker: [{ required: true, message: '请输入挤奶员', trigger: 'blur' }],
        milkStartTime: [{ required: true, message: '请选择挤奶开始时间', trigger: 'change' }],
        milkEndTime: [{ required: true, message: '请选择挤奶结束时间', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.loadAvailableTanks()
  },
  methods: {
    async loadAvailableTanks() {
      try {
        const res = await getAvailableTanks()
        this.availableTanks = res.data || []
      } catch (error) {
        console.error('加载可用奶罐失败:', error)
      }
    },
    async onTankChange(tankId) {
      if (tankId) {
        try {
          const res = await checkTankCleaned(tankId)
          if (!res.data?.cleaned) {
            this.$message.warning('该奶罐未清洗合格，请选择其他奶罐')
            this.form.milkTankId = null
          }
        } catch (error) {
          console.error('检查奶罐清洗状态失败:', error)
        }
      }
    },
    async submitForm() {
      this.$refs.batchForm.validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            await submitBatch(this.form)
            this.$message.success('批次提交成功')
            this.$router.push('/batch')
          } catch (error) {
            console.error('提交失败:', error)
          } finally {
            this.loading = false
          }
        }
      })
    },
    resetForm() {
      this.$refs.batchForm.resetFields()
    }
  }
}
</script>
