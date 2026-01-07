<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="派工单号/产品名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待开工" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header><span>派工单列表</span></template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="workOrderNo" label="派工单号" width="160" />
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="productName" label="产品名称" width="180" />
        <el-table-column prop="planQuantity" label="计划数量" width="90" />
        <el-table-column prop="completedQuantity" label="已完成" width="80" />
        <el-table-column prop="teamName" label="班组" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="120">
          <template #default="{ row }">
            <el-progress :percentage="Math.round((row.completedQuantity / row.planQuantity) * 100)" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button type="primary" link @click="printQrCode(row)">打印</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" layout="total, prev, pager, next" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="派工单详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="派工单号">{{ currentOrder.workOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="产品">{{ currentOrder.productName }}</el-descriptions-item>
        <el-descriptions-item label="班组">{{ currentOrder.teamName }}</el-descriptions-item>
        <el-descriptions-item label="计划数量">{{ currentOrder.planQuantity }}</el-descriptions-item>
        <el-descriptions-item label="已完成">{{ currentOrder.completedQuantity }}</el-descriptions-item>
      </el-descriptions>
      <h4 style="margin: 20px 0 10px">工序列表</h4>
      <el-table :data="processes" border size="small">
        <el-table-column prop="sortOrder" label="序号" width="60" />
        <el-table-column prop="processName" label="工序名称" />
        <el-table-column prop="operatorName" label="操作工" width="100" />
        <el-table-column prop="planQuantity" label="计划" width="80" />
        <el-table-column prop="completedQuantity" label="完成" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const processes = ref([])
const detailDialogVisible = ref(false)
const currentOrder = ref<any>({})
const searchForm = reactive({ keyword: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const getStatusLabel = (s: string) => ({ PENDING: '待开工', IN_PROGRESS: '进行中', COMPLETED: '已完成', PAUSED: '暂停' }[s] || s)
const getStatusType = (s: string) => ({ PENDING: 'info', IN_PROGRESS: '', COMPLETED: 'success', PAUSED: 'warning' }[s] || '')

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/work-orders', { params: { ...searchForm, page: pagination.page - 1, size: pagination.size } })
    tableData.value = res.data.content
    pagination.total = res.data.totalElements
  } finally { loading.value = false }
}

function handleSearch() { pagination.page = 1; loadData() }

async function viewDetail(row: any) {
  currentOrder.value = row
  const res = await request.get(`/work-orders/${row.id}/processes`)
  processes.value = res.data
  detailDialogVisible.value = true
}

function printQrCode(row: any) {
  ElMessage.info('打印功能开发中...')
}

onMounted(() => loadData())
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
