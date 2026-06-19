<template>
  <div class="container">
    <div class="page-header">
      <span class="title">录入检测结果</span>
      <el-button @click="$router.back()">
        <i class="el-icon-arrow-left"></i> 返回
      </el-button>
    </div>

    <div class="form-container">
      <el-form :model="form" :rules="rules" ref="testForm" label-width="120px">
        <el-alert
          title="业务规则提醒"
          type="warning"
          :closable="false"
          style="margin-bottom: 20px;">
          <p>• 检测不合格的批次将自动生成拒收记录</p>
          <p>• 请认真核对批次号和检测结果</p>
        </el-alert>

        <el-form-item label="选择批次" prop="batchId">
          <el-select v-model="form.batchId" placeholder="请选择待检测批次" style="width: 100%;" filterable>
            <el-option
              v-for="batch in pendingBatches"
              :key="batch.id"
              :label="`${batch.batchNo} - ${batch.milkTankName} - ${batch.milkVolume}kg`"
              :value="batch.id">
            </el-option>
          </el-select>
          <div v-if="selectedBatch" style="margin-top: 10px; padding: 10px; background: #f5f7fa; border-radius: 4px;">
            <p><strong>批次信息：</strong></p>
            <p>批次号：{{ selectedBatch.batchNo }}</p>
            <p>奶罐：{{ selectedBatch.milkTankName }}</p>
            <p>奶量：{{ selectedBatch.milkVolume }}kg</p>
            <p>温度：{{ selectedBatch.milkTemperature }}°C</p>
            <p>状态：{{ selectedBatch.batchStatus | formatStatus('batch') }}</p>
          </div>
        </el-form-item>

        <el-form-item label="检测项目" prop="testItem">
          <el-select v-model="form.testItem" placeholder="请选择检测项目" style="width: 100%;">
            <el-option label="β-内酰胺类" value="β-内酰胺类" />
            <el-option label="四环素类" value="四环素类" />
            <el-option label="磺胺类" value="磺胺类" />
            <el-option label="氯霉素" value="氯霉素" />
            <el-option label="链霉素" value="链霉素" />
            <el-option label="综合快检" value="综合快检" />
          </el-select>
        </el-form-item>

        <el-form-item label="检测方法">
          <el-select v-model="form.testMethod" placeholder="请选择检测方法（选填）" style="width: 100%;">
            <el-option label="胶体金法" value="胶体金法" />
            <el-option label="ELISA法" value="ELISA法" />
            <el-option label="液相色谱法" value="液相色谱法" />
          </el-select>
        </el-form-item>

        <el-form-item label="检测结果" prop="testResult">
          <el-radio-group v-model="form.testResult">
            <el-radio label="PASSED">
              <span style="color: #67c23a; font-weight: bold;">合格</span>
            </el-radio>
            <el-radio label="FAILED">
              <span style="color: #f56c6c; font-weight: bold;">不合格</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="检测值">
          <el-input v-model="form.testValue" placeholder="请输入检测值（选填）" />
        </el-form-item>

        <el-form-item label="参考值">
          <el-input v-model="form.referenceValue" placeholder="请输入参考值（选填）" />
        </el-form-item>

        <el-form-item label="检测时间" prop="testTime">
          <el-date-picker
            v-model="form.testTime"
            type="datetime"
            placeholder="选择检测时间"
            style="width: 100%;" />
        </el-form-item>

        <el-form-item label="化验员" prop="tester">
          <el-input v-model="form.tester" placeholder="请输入化验员姓名" />
        </el-form-item>

        <el-form-item label="检测仪器">
          <el-input v-model="form.testEquipment" placeholder="请输入检测仪器（选填）" />
        </el-form-item>

        <el-form-item label="检测备注">
          <el-input type="textarea" v-model="form.remark" :rows="3" placeholder="请输入备注信息（选填）" />
        </el-form-item>

        <el-form-item v-if="form.testResult === 'FAILED'">
          <el-alert
            title="检测不合格将自动生成拒收记录，并将批次状态标记为已拒收"
            type="error"
            :closable="false" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">提交检测结果</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { submitTest } from '@/api/test'
import { getBatchList } from '@/api/batch'

export default {
  name: 'TestSubmit',
  data() {
    return {
      loading: false,
      pendingBatches: [],
      selectedBatch: null,
      form: {
        batchId: null,
        testItem: '',
        testMethod: '',
        testResult: '',
        testValue: '',
        referenceValue: '',
        testTime: null,
        tester: '',
        testEquipment: '',
        remark: ''
      },
      rules: {
        batchId: [{ required: true, message: '请选择批次', trigger: 'change' }],
        testItem: [{ required: true, message: '请选择检测项目', trigger: 'change' }],
        testResult: [{ required: true, message: '请选择检测结果', trigger: 'change' }],
        testTime: [{ required: true, message: '请选择检测时间', trigger: 'change' }],
        tester: [{ required: true, message: '请输入化验员', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadPendingBatches()
  },
  methods: {
    async loadPendingBatches() {
      try {
        const res = await getBatchList({ pageNum: 1, pageSize: 100, status: 'PENDING_TEST' })
        this.pendingBatches = res.data?.records || []
      } catch (error) {
        console.error('加载待检测批次失败:', error)
      }
    },
    onBatchChange(batchId) {
      this.selectedBatch = this.pendingBatches.find(b => b.id === batchId) || null
    },
    async submitForm() {
      this.$refs.testForm.validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            await submitTest(this.form)
            this.$message.success('检测结果提交成功')
            if (this.form.testResult === 'FAILED') {
              this.$message.info('检测不合格，已自动生成拒收记录')
            }
            this.$router.push('/test')
          } catch (error) {
            console.error('提交失败:', error)
          } finally {
            this.loading = false
          }
        }
      })
    },
    resetForm() {
      this.$refs.testForm.resetFields()
      this.selectedBatch = null
    }
  },
  watch: {
    'form.batchId': function(val) {
      this.onBatchChange(val)
    }
  }
}
</script>
