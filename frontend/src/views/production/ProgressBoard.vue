<template>
  <div class="progress-board">
    <div class="board-header">
      <h2>订单进度看板</h2>
      <el-button type="primary" @click="loadData" :loading="loading">
        <el-icon><Refresh /></el-icon>刷新
      </el-button>
    </div>

    <div class="order-cards" v-loading="loading">
      <el-card v-for="order in orders" :key="order.id" class="order-card" :class="getOrderClass(order)">
        <div class="order-header">
          <span class="order-no">{{ order.orderNo }}</span>
          <el-tag :type="getStatusType(order.status)" size="small">{{ getStatusLabel(order.status) }}</el-tag>
        </div>
        <div class="order-info">
          <div class="info-row">
            <span class="label">产品:</span>
            <span class="value">{{ order.productName }}</span>
          </div>
          <div class="info-row">
            <span class="label">交期:</span>
            <span class="value" :class="{ overdue: isOverdue(order) }">{{ order.deliveryDate }}</span>
          </div>
          <div class="info-row">
            <span class="label">进度:</span>
            <span class="value">{{ order.completedQuantity }} / {{ order.quantity }}</span>
          </div>
        </div>
        <el-progress :percentage="getProgress(order)" :stroke-width="10" :color="getProgressColor(order)" />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import dayjs from 'dayjs'

const loading = ref(false)
const orders = ref<any[]>([])

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/sales-orders', { params: { page: 0, size: 100 } })
    orders.value = res.data.content.filter((o: any) => !['CANCELLED', 'COMPLETED'].includes(o.status))
  } finally { loading.value = false }
}

const getStatusLabel = (s: string) => ({ DRAFT: '草稿', PENDING: '待排产', SCHEDULED: '已排产', IN_PROGRESS: '生产中', COMPLETED: '已完成' }[s] || s)
const getStatusType = (s: string) => ({ DRAFT: 'info', PENDING: 'warning', SCHEDULED: 'primary', IN_PROGRESS: '', COMPLETED: 'success' }[s] || '')
const getProgress = (o: any) => o.quantity > 0 ? Math.round((o.completedQuantity / o.quantity) * 100) : 0
const isOverdue = (o: any) => dayjs(o.deliveryDate).isBefore(dayjs(), 'day') && o.status !== 'COMPLETED'
const getOrderClass = (o: any) => ({ overdue: isOverdue(o) })
const getProgressColor = (o: any) => isOverdue(o) ? '#f56c6c' : '#409eff'

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.progress-board { padding: 0; }
.board-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
  h2 { margin: 0; font-size: 18px; color: #303133; }
}
.order-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.order-card {
  &.overdue { border-color: #f56c6c; }
  .order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
    .order-no { font-weight: 600; color: #303133; }
  }
  .order-info { margin-bottom: 12px;
    .info-row { display: flex; margin-bottom: 6px; font-size: 13px;
      .label { color: #909399; width: 50px; }
      .value { color: #606266;
        &.overdue { color: #f56c6c; font-weight: 600; }
      }
    }
  }
}
</style>
