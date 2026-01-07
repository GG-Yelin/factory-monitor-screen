<template>
  <div class="page-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 员工产量报表 -->
      <el-tab-pane label="员工产量" name="employee">
        <div class="report-filter">
          <el-date-picker v-model="employeeFilter.dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" />
          <el-select v-model="employeeFilter.teamId" placeholder="班组" clearable style="margin-left: 10px">
            <el-option v-for="t in teams" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
          <el-button type="primary" @click="loadEmployeeReport" style="margin-left: 10px">查询</el-button>
          <el-button type="success" @click="exportReport('employee')"><el-icon><Download /></el-icon>导出</el-button>
        </div>
        <el-table :data="employeeData" stripe>
          <el-table-column prop="employeeName" label="员工" width="100" />
          <el-table-column prop="employeeNo" label="工号" width="100" />
          <el-table-column prop="teamName" label="班组" width="100" />
          <el-table-column prop="totalQuantity" label="总产量" width="100" />
          <el-table-column prop="qualifiedQuantity" label="合格数" width="100" />
          <el-table-column prop="qualifyRate" label="合格率" width="100">
            <template #default="{ row }">{{ (row.qualifyRate * 100).toFixed(1) }}%</template>
          </el-table-column>
          <el-table-column prop="totalWage" label="总工资" width="120">
            <template #default="{ row }">{{ row.totalWage?.toFixed(2) }}</template>
          </el-table-column>
        </el-table>
        <div ref="employeeChart" class="chart-container"></div>
      </el-tab-pane>

      <!-- 订单进度报表 -->
      <el-tab-pane label="订单进度" name="order">
        <div class="report-filter">
          <el-select v-model="orderFilter.status" placeholder="状态" clearable>
            <el-option label="待排产" value="PENDING" />
            <el-option label="生产中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
          <el-button type="primary" @click="loadOrderReport" style="margin-left: 10px">查询</el-button>
        </div>
        <el-table :data="orderData" stripe>
          <el-table-column prop="orderNo" label="订单号" width="160" />
          <el-table-column prop="productName" label="产品" width="150" />
          <el-table-column prop="quantity" label="订单数量" width="100" />
          <el-table-column prop="completedQuantity" label="已完成" width="100" />
          <el-table-column label="进度" width="150">
            <template #default="{ row }">
              <el-progress :percentage="Math.round((row.completedQuantity / row.quantity) * 100)" :stroke-width="10" />
            </template>
          </el-table-column>
          <el-table-column prop="deliveryDate" label="交货日期" width="110" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div ref="orderChart" class="chart-container"></div>
      </el-tab-pane>

      <!-- 产品质量报表 -->
      <el-tab-pane label="产品质量" name="quality">
        <div class="report-filter">
          <el-date-picker v-model="qualityFilter.dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" />
          <el-button type="primary" @click="loadQualityReport" style="margin-left: 10px">查询</el-button>
          <el-button type="success" @click="exportReport('quality')"><el-icon><Download /></el-icon>导出</el-button>
        </div>
        <el-table :data="qualityData" stripe>
          <el-table-column prop="productName" label="产品" width="180" />
          <el-table-column prop="inspectedQuantity" label="检验数量" width="100" />
          <el-table-column prop="qualifiedQuantity" label="合格数量" width="100" />
          <el-table-column prop="unqualifiedQuantity" label="不合格数量" width="100" />
          <el-table-column label="合格率" width="150">
            <template #default="{ row }">
              <el-progress :percentage="Math.round((row.qualifiedQuantity / row.inspectedQuantity) * 100)" :stroke-width="10" :color="getQualityColor(row)" />
            </template>
          </el-table-column>
        </el-table>
        <div ref="qualityChart" class="chart-container"></div>
      </el-tab-pane>

      <!-- 设备产量报表 -->
      <el-tab-pane label="设备产量" name="equipment">
        <div class="report-filter">
          <el-date-picker v-model="equipmentFilter.dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" />
          <el-button type="primary" @click="loadEquipmentReport" style="margin-left: 10px">查询</el-button>
        </div>
        <el-table :data="equipmentData" stripe>
          <el-table-column prop="equipmentName" label="设备" width="150" />
          <el-table-column prop="totalQuantity" label="总产量" width="100" />
          <el-table-column prop="runningHours" label="运行时长(h)" width="120" />
          <el-table-column prop="efficiency" label="效率" width="100">
            <template #default="{ row }">{{ (row.efficiency * 100).toFixed(1) }}%</template>
          </el-table-column>
        </el-table>
        <div ref="equipmentChart" class="chart-container"></div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const activeTab = ref('employee')
const teams = ref<any[]>([])

const employeeFilter = reactive({ dateRange: [dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')], teamId: null })
const orderFilter = reactive({ status: '' })
const qualityFilter = reactive({ dateRange: [dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')] })
const equipmentFilter = reactive({ dateRange: [dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')] })

const employeeData = ref<any[]>([])
const orderData = ref<any[]>([])
const qualityData = ref<any[]>([])
const equipmentData = ref<any[]>([])

const employeeChart = ref<HTMLElement>()
const orderChart = ref<HTMLElement>()
const qualityChart = ref<HTMLElement>()
const equipmentChart = ref<HTMLElement>()

let employeeChartInstance: echarts.ECharts | null = null
let orderChartInstance: echarts.ECharts | null = null
let qualityChartInstance: echarts.ECharts | null = null
let equipmentChartInstance: echarts.ECharts | null = null

const getStatusLabel = (s: string) => ({ DRAFT: '草稿', PENDING: '待排产', SCHEDULED: '已排产', IN_PROGRESS: '生产中', COMPLETED: '已完成' }[s] || s)
const getStatusType = (s: string) => ({ DRAFT: 'info', PENDING: 'warning', SCHEDULED: 'primary', IN_PROGRESS: '', COMPLETED: 'success' }[s] || '')
const getQualityColor = (row: any) => {
  const rate = row.qualifiedQuantity / row.inspectedQuantity
  return rate >= 0.95 ? '#67c23a' : rate >= 0.9 ? '#e6a23c' : '#f56c6c'
}

async function loadTeams() {
  try {
    const res = await request.get('/teams/active')
    teams.value = res.data || []
  } catch {}
}

async function loadEmployeeReport() {
  try {
    const res = await request.get('/reports/employee-production', {
      params: { startDate: employeeFilter.dateRange?.[0], endDate: employeeFilter.dateRange?.[1], teamId: employeeFilter.teamId }
    })
    employeeData.value = res.data || []
    renderEmployeeChart()
  } catch { employeeData.value = [] }
}

async function loadOrderReport() {
  try {
    const res = await request.get('/reports/order-progress', { params: { status: orderFilter.status } })
    orderData.value = res.data || []
    renderOrderChart()
  } catch { orderData.value = [] }
}

async function loadQualityReport() {
  try {
    const res = await request.get('/reports/quality', {
      params: { startDate: qualityFilter.dateRange?.[0], endDate: qualityFilter.dateRange?.[1] }
    })
    qualityData.value = res.data || []
    renderQualityChart()
  } catch { qualityData.value = [] }
}

async function loadEquipmentReport() {
  try {
    const res = await request.get('/reports/equipment', {
      params: { startDate: equipmentFilter.dateRange?.[0], endDate: equipmentFilter.dateRange?.[1] }
    })
    equipmentData.value = res.data || []
    renderEquipmentChart()
  } catch { equipmentData.value = [] }
}

function renderEmployeeChart() {
  nextTick(() => {
    if (!employeeChart.value) return
    if (!employeeChartInstance) employeeChartInstance = echarts.init(employeeChart.value)
    employeeChartInstance.setOption({
      title: { text: '员工产量对比', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: employeeData.value.map(d => d.employeeName) },
      yAxis: { type: 'value' },
      series: [{ name: '产量', type: 'bar', data: employeeData.value.map(d => d.totalQuantity), itemStyle: { color: '#409eff' } }]
    })
  })
}

function renderOrderChart() {
  nextTick(() => {
    if (!orderChart.value) return
    if (!orderChartInstance) orderChartInstance = echarts.init(orderChart.value)
    const statusCount = { PENDING: 0, IN_PROGRESS: 0, COMPLETED: 0 }
    orderData.value.forEach(o => { if (statusCount[o.status as keyof typeof statusCount] !== undefined) statusCount[o.status as keyof typeof statusCount]++ })
    orderChartInstance.setOption({
      title: { text: '订单状态分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie', radius: '50%',
        data: [
          { name: '待排产', value: statusCount.PENDING },
          { name: '生产中', value: statusCount.IN_PROGRESS },
          { name: '已完成', value: statusCount.COMPLETED }
        ]
      }]
    })
  })
}

function renderQualityChart() {
  nextTick(() => {
    if (!qualityChart.value) return
    if (!qualityChartInstance) qualityChartInstance = echarts.init(qualityChart.value)
    qualityChartInstance.setOption({
      title: { text: '产品质量合格率', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: qualityData.value.map(d => d.productName) },
      yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
      series: [{
        name: '合格率', type: 'bar',
        data: qualityData.value.map(d => Math.round((d.qualifiedQuantity / d.inspectedQuantity) * 100)),
        itemStyle: { color: '#67c23a' }
      }]
    })
  })
}

function renderEquipmentChart() {
  nextTick(() => {
    if (!equipmentChart.value) return
    if (!equipmentChartInstance) equipmentChartInstance = echarts.init(equipmentChart.value)
    equipmentChartInstance.setOption({
      title: { text: '设备产量对比', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: equipmentData.value.map(d => d.equipmentName) },
      yAxis: { type: 'value' },
      series: [{ name: '产量', type: 'bar', data: equipmentData.value.map(d => d.totalQuantity), itemStyle: { color: '#e6a23c' } }]
    })
  })
}

async function exportReport(type: string) {
  try {
    let params: any = {}
    if (type === 'employee') {
      params = { type: 'employee', startDate: employeeFilter.dateRange?.[0], endDate: employeeFilter.dateRange?.[1], teamId: employeeFilter.teamId }
    } else if (type === 'quality') {
      params = { type: 'quality', startDate: qualityFilter.dateRange?.[0], endDate: qualityFilter.dateRange?.[1] }
    }
    const res = await request.get('/reports/export', { params, responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `${type}_report_${dayjs().format('YYYYMMDD')}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

watch(activeTab, (tab) => {
  if (tab === 'employee' && employeeData.value.length === 0) loadEmployeeReport()
  else if (tab === 'order' && orderData.value.length === 0) loadOrderReport()
  else if (tab === 'quality' && qualityData.value.length === 0) loadQualityReport()
  else if (tab === 'equipment' && equipmentData.value.length === 0) loadEquipmentReport()
})

onMounted(() => {
  loadTeams()
  loadEmployeeReport()
})
</script>

<style scoped>
.report-filter { margin-bottom: 16px; display: flex; align-items: center; flex-wrap: wrap; gap: 10px; }
.chart-container { height: 350px; margin-top: 20px; }
</style>
