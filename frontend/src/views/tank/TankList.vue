<template>
  <div class="container">
    <div class="page-header">
      <span class="title">奶罐管理</span>
      <el-button type="primary" style="float: right;" @click="showEditDialog = true">
        <i class="el-icon-plus"></i> 新增奶罐
      </el-button>
    </div>

    <el-card>
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 150px;">
              <el-option label="空闲" value="IDLE" />
              <el-option label="接奶中" value="LOADING" />
              <el-option label="待检测" value="PENDING_TEST" />
              <el-option label="检测完成" value="TESTED" />
              <el-option label="已装车" value="LOADED" />
              <el-option label="待清洗" value="PENDING_CLEAN" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="tankCode" label="奶罐编号" width="120" />
        <el-table-column prop="tankName" label="奶罐名称" width="150" />
        <el-table-column prop="capacity" label="容量(kg)" width="120" />
        <el-table-column prop="tankStatus" label="状态" width="140">
          <template slot-scope="scope">
            <el-tag
              :type="getTankStatusTag(scope.row.tankStatus)"
              size="small">
              {{ scope.row.tankStatus | formatStatus('tank') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否已清洗" width="120">
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.isCleaned ? 'success' : 'danger'"
              size="small">
              {{ scope.row.isCleaned ? '已清洗' : '未清洗' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastCleanTime" label="上次清洗时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.lastCleanTime ? (scope.row.lastCleanTime | formatDate) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.createTime | formatDate }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="checkCleaned(scope.row)">检查清洗</el-button>
            <el-button type="text" size="small" @click="editTank(scope.row)">编辑</el-button>
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

    <el-dialog :title="isEdit ? '编辑奶罐' : '新增奶罐'" :visible.sync="showEditDialog" width="500px">
      <el-form :model="tankForm" :rules="tankRules" ref="tankForm" label-width="100px">
        <el-form-item label="奶罐编号" prop="tankCode">
          <el-input v-model="tankForm.tankCode" placeholder="请输入奶罐编号" />
        </el-form-item>
        <el-form-item label="奶罐名称" prop="tankName">
          <el-input v-model="tankForm.tankName" placeholder="请输入奶罐名称" />
        </el-form-item>
        <el-form-item label="容量(kg)" prop="capacity">
          <el-input-number v-model="tankForm.capacity" :min="1" :max="50000" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="所属牧场" prop="pastureId">
          <el-select v-model="tankForm.pastureId" placeholder="请选择所属牧场" style="width: 100%;">
            <el-option label="一号牧场" :value="1" />
            <el-option label="二号牧场" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="tankForm.location" placeholder="请输入位置（选填）" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="tankForm.remark" :rows="2" placeholder="请输入备注（选填）" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="saveTank">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getTankList, checkTankCleaned, createTank, updateTank } from '@/api/tank'

export default {
  name: 'TankList',
  data() {
    return {
      loading: false,
      saveLoading: false,
      tableData: [],
      searchForm: {
        status: ''
      },
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      showEditDialog: false,
      isEdit: false,
      currentTank: null,
      tankForm: {
        id: null,
        tankCode: '',
        tankName: '',
        capacity: null,
        pastureId: null,
        location: '',
        remark: ''
      },
      tankRules: {
        tankCode: [{ required: true, message: '请输入奶罐编号', trigger: 'blur' }],
        tankName: [{ required: true, message: '请输入奶罐名称', trigger: 'blur' }],
        capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
        pastureId: [{ required: true, message: '请选择所属牧场', trigger: 'change' }]
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
          status: this.searchForm.status || undefined
        }
        const res = await getTankList(params)
        this.tableData = res.data?.records || []
        this.pagination.total = res.data?.total || 0
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    resetSearch() {
      this.searchForm = { status: '' }
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
    getTankStatusTag(status) {
      const map = {
        IDLE: 'success',
        LOADING: 'primary',
        PENDING_TEST: 'warning',
        TESTED: 'info',
        LOADED: 'success',
        PENDING_CLEAN: 'danger'
      }
      return map[status] || 'info'
    },
    async checkCleaned(row) {
      try {
        const res = await checkTankCleaned(row.id)
        this.$message.info(res.data?.cleaned ? '该奶罐已清洗合格' : '该奶罐未清洗，不能接新批次')
      } catch (error) {
        console.error('检查失败:', error)
      }
    },
    editTank(row) {
      this.isEdit = true
      this.currentTank = row
      this.tankForm = {
        id: row.id,
        tankCode: row.tankCode,
        tankName: row.tankName,
        capacity: row.capacity,
        pastureId: row.pastureId,
        location: row.location || '',
        remark: row.remark || ''
      }
      this.showEditDialog = true
    },
    async saveTank() {
      this.$refs.tankForm.validate(async valid => {
        if (valid) {
          this.saveLoading = true
          try {
            if (this.isEdit) {
              await updateTank(this.tankForm)
              this.$message.success('更新成功')
            } else {
              await createTank(this.tankForm)
              this.$message.success('创建成功')
            }
            this.showEditDialog = false
            this.loadData()
          } catch (error) {
            console.error('保存失败:', error)
          } finally {
            this.saveLoading = false
          }
        }
      })
    }
  }
}
</script>
