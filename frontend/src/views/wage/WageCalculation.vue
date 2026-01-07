<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="员工">
          <el-select v-model="searchForm.userId" placeholder="全部员工" clearable filterable>
            <el-option v-for="u in users" :key="u.id" :label="`${u.name} (${u.employeeNo})`" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班组">
          <el-select v-model="searchForm.teamId" placeholder="全部班组" clearable>
            <el-option v-for="t in teams" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>导出Excel
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 汇总统计 -->
    <el-row :gutter="16" class="summary-row">
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="summary-value">{{ summary.totalAmount.toFixed(2) }}</div>
          <div class="summary-label">总工资(元)</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="summary-value">{{ summary.totalQuantity }}</div>
          <div class="summary-label">总完成数量</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="summary-value">{{ summary.recordCount }}</div>
          <div class="summary-label">记录条数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="summary-value">{{ summary.employeeCount }}</div>
          <div class="summary-label">涉及员工</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header><span>工资明细</span></template>
      <el-table :data="tableData" v-loading="loading" stripe show-summary :summary-method="getSummaries">
        <el-table-column prop="employeeName" label="员工" width="100" />
        <el-table-column prop="employeeNo" label="工号" width="100" />
        <el-table-column prop="teamName" label="班组" width="100" />
        <el-table-column prop="workDate" label="日期" width="110" />
        <el-table-column prop="productName" label="产品" width="150" />
        <el-table-column prop="processName" label="工序" width="120" />
        <el-table-column prop="completedQuantity" label="完成数量" width="90" />
        <el-table-column prop="unitPrice" label="单价(元)" width="90">
          <template #default="{ row }">{{ row.unitPrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="100">
          <template #default="{ row }">
            <span class="amount">{{ row.amount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 员工工资汇总 -->
    <el-card class="employee-summary-card">
      <template #header><span>员工工资汇总</span></template>
      <el-table :data="employeeSummary" stripe>
        <el-table-column prop="employeeName" label="员工" width="100" />
        <el-table-column prop="employeeNo" label="工号" width="100" />
        <el-table-column prop="teamName" label="班组" width="100" />
        <el-table-column prop="totalQuantity" label="总完成数" width="100" />
        <el-table-column prop="totalAmount" label="总工资(元)" width="120">
          <template #default="{ row }">
            <span class="amount">{{ row.totalAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="recordCount" label="记录数" width="80" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewEmployeeDetail(row)">查看明细</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref<any[]>([])
const users = ref<any[]>([])
const teams = ref<any[]>([])
const employeeSummary = ref<any[]>([])

const searchForm = reactive({
  dateRange: [dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')] as string[],
  userId: null as number | null,
  teamId: null as number | null
})
const pagination = reactive({ page: 1, size: 20, total: 0 })

const summary = reactive({ totalAmount: 0, totalQuantity: 0, recordCount: 0, employeeCount: 0 })

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page - 1,
      size: pagination.size
    }
    if (searchForm.dateRange?.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    if (searchForm.userId) params.userId = searchForm.userId
    if (searchForm.teamId) params.teamId = searchForm.teamId

    const res = await request.get('/wages', { params })
    tableData.value = res.data.content || []
    pagination.total = res.data.totalElements || 0

    // 计算汇总
    summary.totalAmount = tableData.value.reduce((sum, r) => sum + (r.amount || 0), 0)
    summary.totalQuantity = tableData.value.reduce((sum, r) => sum + (r.completedQuantity || 0), 0)
    summary.recordCount = pagination.total

    // 加载员工汇总
    loadEmployeeSummary(params)
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

async function loadEmployeeSummary(params: any) {
  try {
    const res = await request.get('/wages/summary', { params })
    employeeSummary.value = res.data || []
    summary.employeeCount = employeeSummary.value.length
  } catch {
    employeeSummary.value = []
  }
}

async function loadUsers() {
  try {
    const res = await request.get('/users', { params: { size: 1000 } })
    users.value = res.data.content || []
  } catch {}
}

async function loadTeams() {
  try {
    const res = await request.get('/teams/active')
    teams.value = res.data || []
  } catch {}
}

function handleSearch() { pagination.page = 1; loadData() }

function resetSearch() {
  searchForm.dateRange = [dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]
  searchForm.userId = null
  searchForm.teamId = null
  handleSearch()
}

function viewEmployeeDetail(row: any) {
  searchForm.userId = row.userId
  handleSearch()
}

function getSummaries({ columns, data }: any) {
  const sums: string[] = []
  columns.forEach((column: any, index: number) => {
    if (index === 0) { sums[index] = '合计'; return }
    if (column.property === 'completedQuantity') {
      sums[index] = data.reduce((sum: number, row: any) => sum + (row.completedQuantity || 0), 0).toString()
    } else if (column.property === 'amount') {
      sums[index] = data.reduce((sum: number, row: any) => sum + (row.amount || 0), 0).toFixed(2)
    } else {
      sums[index] = ''
    }
  })
  return sums
}

async function handleExport() {
  try {
    const params: any = {}
    if (searchForm.dateRange?.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    if (searchForm.userId) params.userId = searchForm.userId
    if (searchForm.teamId) params.teamId = searchForm.teamId

    const res = await request.get('/wages/export', { params, responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `工资表_${searchForm.dateRange?.[0]}_${searchForm.dateRange?.[1]}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(() => { loadData(); loadUsers(); loadTeams() })
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.summary-row { margin-bottom: 16px; }
.summary-card { text-align: center; }
.summary-value { font-size: 28px; font-weight: 600; color: #409eff; }
.summary-label { font-size: 14px; color: #909399; margin-top: 8px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
.employee-summary-card { margin-top: 16px; }
.amount { color: #67c23a; font-weight: 600; }
</style>
