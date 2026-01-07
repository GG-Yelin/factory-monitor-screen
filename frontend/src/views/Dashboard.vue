<template>
  <div class="dashboard-page">
    <!-- 顶部状态栏 -->
    <div class="dashboard-header">
      <div class="title-section">
        <h2>生产监控看板</h2>
        <span class="status" :class="{ connected }">
          <span class="dot"></span>
          {{ connected ? '实时数据' : '连接中...' }}
        </span>
      </div>
      <div class="time-section">
        <span>{{ currentTime }}</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <el-card class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
          <el-icon :size="28"><Monitor /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ data?.totalDevices ?? 0 }}</div>
          <div class="stat-label">设备总数</div>
          <div class="stat-sub">在线: {{ data?.onlineDevices ?? 0 }}</div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%)">
          <el-icon :size="28"><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ data?.todayProduction ?? 0 }}</div>
          <div class="stat-label">今日产量</div>
          <div class="stat-sub">计划: {{ data?.planProduction ?? 0 }}</div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
          <el-icon :size="28"><Finished /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ (data?.productionRate ?? 0).toFixed(1) }}%</div>
          <div class="stat-label">完成率</div>
          <el-progress
            :percentage="data?.productionRate ?? 0"
            :stroke-width="6"
            :show-text="false"
            :color="getProgressColor(data?.productionRate ?? 0)"
          />
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-icon" :style="{ background: (data?.alarmDevices ?? 0) > 0 ? 'linear-gradient(135deg, #ff416c 0%, #ff4b2b 100%)' : 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)' }">
          <el-icon :size="28"><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value" :class="{ 'text-danger': (data?.alarmDevices ?? 0) > 0 }">{{ data?.alarmDevices ?? 0 }}</div>
          <div class="stat-label">报警设备</div>
          <div class="stat-sub">{{ (data?.alarmDevices ?? 0) > 0 ? '请及时处理' : '运行正常' }}</div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="chart-section">
      <el-card class="chart-card chart-wide">
        <template #header>
          <span>生产趋势（近7天）</span>
        </template>
        <ProductionChart :data="data?.productionTrend ?? []" />
      </el-card>

      <el-card class="chart-card">
        <template #header>
          <span>设备状态</span>
        </template>
        <DeviceStatusChart
          :online="data?.onlineDevices ?? 0"
          :offline="data?.offlineDevices ?? 0"
          :alarm="data?.alarmDevices ?? 0"
        />
      </el-card>

      <el-card class="chart-card">
        <template #header>
          <span>设备效率 OEE</span>
        </template>
        <EfficiencyGauge :value="data?.equipmentEfficiency ?? 0" />
      </el-card>
    </div>

    <!-- 列表区域 -->
    <div class="list-section">
      <el-card class="list-card list-wide">
        <template #header>
          <span>设备列表</span>
        </template>
        <DeviceList :devices="data?.devices ?? []" />
      </el-card>

      <el-card class="list-card">
        <template #header>
          <span>报警信息</span>
        </template>
        <AlarmList :alarms="data?.alarms ?? []" />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useWebSocket } from '@/composables/useWebSocket'
import ProductionChart from '@/components/ProductionChart.vue'
import DeviceStatusChart from '@/components/DeviceStatusChart.vue'
import EfficiencyGauge from '@/components/EfficiencyGauge.vue'
import DeviceList from '@/components/DeviceList.vue'
import AlarmList from '@/components/AlarmList.vue'
import dayjs from 'dayjs'

const { data, connected } = useWebSocket()

const currentTime = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'))
let timeTimer: number | null = null

const updateTime = () => {
  currentTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
}

const getProgressColor = (percent: number) => {
  if (percent >= 90) return '#67c23a'
  if (percent >= 70) return '#409eff'
  if (percent >= 50) return '#e6a23c'
  return '#f56c6c'
}

onMounted(() => {
  timeTimer = window.setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timeTimer) {
    clearInterval(timeTimer)
  }
})
</script>

<style scoped lang="scss">
.dashboard-page {
  min-height: calc(100vh - 120px);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .title-section {
    display: flex;
    align-items: center;
    gap: 16px;

    h2 {
      margin: 0;
      font-size: 20px;
      color: #303133;
    }

    .status {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #909399;

      .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #909399;
      }

      &.connected {
        color: #67c23a;
        .dot {
          background: #67c23a;
        }
      }
    }
  }

  .time-section {
    font-size: 14px;
    color: #606266;
  }
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;

  .stat-card {
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 20px;
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }

    .stat-info {
      flex: 1;

      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #303133;
        line-height: 1.2;

        &.text-danger {
          color: #f56c6c;
        }
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-top: 4px;
      }

      .stat-sub {
        font-size: 12px;
        color: #c0c4cc;
        margin-top: 4px;
      }

      .el-progress {
        margin-top: 8px;
      }
    }
  }
}

.chart-section {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;

  .chart-card {
    height: 320px;

    :deep(.el-card__header) {
      padding: 12px 20px;
      font-size: 14px;
      font-weight: 500;
    }

    :deep(.el-card__body) {
      height: calc(100% - 48px);
      padding: 16px;
    }
  }

  .chart-wide {
    grid-column: span 1;
  }
}

.list-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;

  .list-card {
    height: 360px;

    :deep(.el-card__header) {
      padding: 12px 20px;
      font-size: 14px;
      font-weight: 500;
    }

    :deep(.el-card__body) {
      height: calc(100% - 48px);
      padding: 0;
      overflow: auto;
    }
  }

  .list-wide {
    grid-column: span 1;
  }
}

@media (max-width: 1400px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .chart-section {
    grid-template-columns: 1fr 1fr;

    .chart-card:first-child {
      grid-column: span 2;
    }
  }

  .list-section {
    grid-template-columns: 1fr;
  }
}
</style>
