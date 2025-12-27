<template>
  <div class="dashboard">
    <!-- 头部 -->
    <header class="dashboard-header">
      <div class="status" :class="{ disconnected: !connected }">
        <span class="dot"></span>
        <span>{{ connected ? '已连接' : '未连接' }}</span>
      </div>
      <h1 class="title">工厂设备监控大屏</h1>
      <div class="time">{{ currentTime }}</div>
    </header>

    <!-- 内容区域 -->
    <main class="dashboard-content">
      <!-- 第一行：统计卡片 -->
      <StatCard
        :value="data?.totalDevices ?? 0"
        label="设备总数"
        :sub-info="`在线: ${data?.onlineDevices ?? 0}`"
      />
      <StatCard
        :value="data?.todayProduction ?? 0"
        label="今日产量"
        :sub-info="`计划: ${data?.planProduction ?? 0}`"
        type="success"
      />
      <StatCard
        :value="(data?.productionRate ?? 0).toFixed(1)"
        suffix="%"
        label="完成率"
        :show-progress="true"
        :progress-percent="data?.productionRate ?? 0"
        :type="getProductionRateType()"
      />
      <StatCard
        :value="data?.alarmDevices ?? 0"
        label="报警设备"
        :type="(data?.alarmDevices ?? 0) > 0 ? 'danger' : 'success'"
      />

      <!-- 第二行：图表 -->
      <ProductionChart :data="data?.productionTrend ?? []" />
      <DeviceStatusChart
        :online="data?.onlineDevices ?? 0"
        :offline="data?.offlineDevices ?? 0"
        :alarm="data?.alarmDevices ?? 0"
      />
      <EfficiencyGauge title="设备效率 OEE" :value="data?.equipmentEfficiency ?? 0" />

      <!-- 第三行：统计概览和月度图表 -->
      <StatisticsOverview
        :today-data="overview?.today ?? null"
        :month-data="overview?.currentMonth ?? null"
      />
      <MonthlyChart :data="monthlyStats" />

      <!-- 第四行：设备和报警 -->
      <DeviceList :devices="data?.devices ?? []" />
      <AlarmList :alarms="data?.alarms ?? []" />
      <DataPointList :data-points="data?.dataPoints ?? []" />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useWebSocket } from '@/composables/useWebSocket'
import { useStatistics } from '@/composables/useStatistics'
import StatCard from '@/components/StatCard.vue'
import ProductionChart from '@/components/ProductionChart.vue'
import DeviceStatusChart from '@/components/DeviceStatusChart.vue'
import EfficiencyGauge from '@/components/EfficiencyGauge.vue'
import DeviceList from '@/components/DeviceList.vue'
import AlarmList from '@/components/AlarmList.vue'
import DataPointList from '@/components/DataPointList.vue'
import StatisticsOverview from '@/components/StatisticsOverview.vue'
import MonthlyChart from '@/components/MonthlyChart.vue'
import dayjs from 'dayjs'

const { data, connected } = useWebSocket()
const { overview, monthlyStats, fetchOverview, fetchRecentMonthlyStats } = useStatistics()

const currentTime = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'))

let timeTimer: number | null = null
let statsTimer: number | null = null

const updateTime = () => {
  currentTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
}

const getProductionRateType = () => {
  const rate = data.value?.productionRate ?? 0
  if (rate >= 90) return 'success'
  if (rate >= 70) return 'default'
  if (rate >= 50) return 'warning'
  return 'danger'
}

const loadStatistics = async () => {
  await Promise.all([
    fetchOverview(),
    fetchRecentMonthlyStats(12)
  ])
}

onMounted(() => {
  timeTimer = window.setInterval(updateTime, 1000)

  // 加载统计数据
  loadStatistics()

  // 每5分钟刷新统计数据
  statsTimer = window.setInterval(loadStatistics, 5 * 60 * 1000)
})

onUnmounted(() => {
  if (timeTimer) {
    clearInterval(timeTimer)
  }
  if (statsTimer) {
    clearInterval(statsTimer)
  }
})
</script>

<style scoped lang="scss">
.dashboard-content {
  height: calc(100% - 80px);
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: 120px 280px 160px 280px;
  gap: 20px;
  overflow-y: auto;

  // 第一行：4个统计卡片
  > :nth-child(1),
  > :nth-child(2),
  > :nth-child(3),
  > :nth-child(4) {
    grid-column: span 1;
  }

  // 第二行：生产趋势图(2列) + 设备状态(1列) + 效率仪表盘(1列)
  > :nth-child(5) {
    grid-column: span 2;
  }
  > :nth-child(6),
  > :nth-child(7) {
    grid-column: span 1;
  }

  // 第三行：统计概览(2列) + 月度图表(2列)
  > :nth-child(8) {
    grid-column: span 2;
  }
  > :nth-child(9) {
    grid-column: span 2;
  }

  // 第四行：设备列表(2列) + 报警列表(1列) + 数据点(1列)
  > :nth-child(10) {
    grid-column: span 2;
  }
  > :nth-child(11),
  > :nth-child(12) {
    grid-column: span 1;
  }
}

// 响应式调整
@media (max-width: 1600px) {
  .dashboard-content {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1200px) {
  .dashboard-content {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: auto;

    > :nth-child(n) {
      grid-column: span 1;
    }

    > :nth-child(5),
    > :nth-child(8),
    > :nth-child(9),
    > :nth-child(10) {
      grid-column: span 2;
    }
  }
}
</style>
