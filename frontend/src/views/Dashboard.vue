<template>
  <div class="dashboard">
    <div class="card-row">
      <div class="dashboard-card" v-for="card in statsCards" :key="card.title">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <div class="card-header">{{ card.title }}</div>
            <div class="card-value" :style="{ color: card.color }">{{ card.value }}</div>
          </div>
          <i :class="card.icon" class="card-icon" :style="{ color: card.color }"></i>
        </div>
      </div>
    </div>

    <div class="chart-row">
      <el-card class="chart-card">
        <div slot="header">
          <span>批次状态统计</span>
        </div>
        <div ref="batchChart" style="height: 300px;"></div>
      </el-card>

      <el-card class="chart-card">
        <div slot="header">
          <span>近7日检测合格率</span>
        </div>
        <div ref="testChart" style="height: 300px;"></div>
      </el-card>
    </div>

    <el-card class="table-card">
      <div slot="header">
        <span>最近批次记录</span>
        <el-button type="primary" size="mini" style="float: right;" @click="$router.push('/batch')">查看全部</el-button>
      </div>
      <el-table :data="recentBatches" border stripe>
        <el-table-column prop="batchNo" label="批次号" width="150" />
        <el-table-column prop="milkVolume" label="奶量(kg)" width="120" />
        <el-table-column prop="milkTemperature" label="温度(°C)" width="120" />
        <el-table-column prop="batchStatus" label="状态" width="140">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.batchStatus)" size="small">
              {{ scope.row.batchStatus | formatStatus('batch') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.createTime | formatDate }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getBatchList } from '@/api/batch'

export default {
  name: 'Dashboard',
  data() {
    return {
      statsCards: [
        { title: '今日批次', value: 0, icon: 'el-icon-s-order', color: '#409EFF' },
        { title: '检测合格', value: 0, icon: 'el-icon-circle-check', color: '#67C23A' },
        { title: '待处理', value: 0, icon: 'el-icon-time', color: '#E6A23C' },
        { title: '拒收记录', value: 0, icon: 'el-icon-circle-close', color: '#F56C6C' }
      ],
      recentBatches: []
    }
  },
  mounted() {
    this.loadData()
    this.initCharts()
  },
  methods: {
    async loadData() {
      try {
        const res = await getBatchList({ pageNum: 1, pageSize: 5 })
        this.recentBatches = res.data?.records || []
        this.statsCards[0].value = res.data?.total || 0
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    },
    getStatusType(status) {
      const map = {
        TEST_PASSED: 'success',
        TEST_FAILED: 'danger',
        PENDING_TEST: 'warning',
        LOADED: 'success',
        REJECTED: 'danger'
      }
      return map[status] || 'info'
    },
    initCharts() {
      this.initBatchChart()
      this.initTestChart()
    },
    initBatchChart() {
      const chart = echarts.init(this.$refs.batchChart)
      chart.setOption({
        tooltip: { trigger: 'item' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          emphasis: { label: { show: true, fontSize: '16', fontWeight: 'bold' } },
          labelLine: { show: false },
          data: [
            { value: 15, name: '挤奶中', itemStyle: { color: '#409EFF' } },
            { value: 28, name: '待检测', itemStyle: { color: '#E6A23C' } },
            { value: 85, name: '检测通过', itemStyle: { color: '#67C23A' } },
            { value: 8, name: '检测不合格', itemStyle: { color: '#F56C6C' } },
            { value: 42, name: '已装车', itemStyle: { color: '#909399' } }
          ]
        }]
      })
    },
    initTestChart() {
      const chart = echarts.init(this.$refs.testChart)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['检测数', '合格数'] },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
        yAxis: { type: 'value' },
        series: [
          { name: '检测数', type: 'bar', data: [20, 25, 22, 28, 30, 15, 18], itemStyle: { color: '#409EFF' } },
          { name: '合格数', type: 'bar', data: [19, 24, 21, 27, 29, 14, 17], itemStyle: { color: '#67C23A' } }
        ]
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  .card-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 20px;
  }
  
  .chart-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    margin-bottom: 20px;
  }
  
  .table-card {
    margin-bottom: 20px;
  }
}

@media (max-width: 1200px) {
  .dashboard .card-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .dashboard .chart-row {
    grid-template-columns: 1fr;
  }
}
</style>
