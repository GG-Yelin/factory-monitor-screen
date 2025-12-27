<template>
  <div class="statistics-overview">
    <div class="overview-header">
      <h3>生产统计概览</h3>
      <div class="period-tabs">
        <button
          :class="{ active: activePeriod === 'today' }"
          @click="activePeriod = 'today'"
        >
          今日
        </button>
        <button
          :class="{ active: activePeriod === 'month' }"
          @click="activePeriod = 'month'"
        >
          本月
        </button>
      </div>
    </div>

    <div class="overview-content" v-if="activePeriod === 'today'">
      <div class="stat-item">
        <div class="stat-value">{{ todayData?.totalProduction ?? '-' }}</div>
        <div class="stat-label">今日产量</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ todayData?.totalPlan ?? '-' }}</div>
        <div class="stat-label">计划数量</div>
      </div>
      <div class="stat-item">
        <div class="stat-value success">
          {{ todayData?.completionRate?.toFixed(1) ?? '-' }}%
        </div>
        <div class="stat-label">完成率</div>
      </div>
      <div class="stat-item">
        <div class="stat-value info">
          {{ todayData?.avgOee?.toFixed(1) ?? '-' }}%
        </div>
        <div class="stat-label">设备效率</div>
      </div>
      <div class="stat-item">
        <div class="stat-value" :class="{ warning: (todayData?.alarmCount ?? 0) > 0 }">
          {{ todayData?.alarmCount ?? 0 }}
        </div>
        <div class="stat-label">报警次数</div>
      </div>
    </div>

    <div class="overview-content" v-else>
      <div class="stat-item">
        <div class="stat-value">{{ monthData?.totalProduction ?? '-' }}</div>
        <div class="stat-label">月度产量</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ monthData?.avgDailyProduction?.toFixed(0) ?? '-' }}</div>
        <div class="stat-label">日均产量</div>
      </div>
      <div class="stat-item">
        <div class="stat-value success">
          {{ monthData?.completionRate?.toFixed(1) ?? '-' }}%
        </div>
        <div class="stat-label">完成率</div>
      </div>
      <div class="stat-item">
        <div class="stat-value info">
          {{ monthData?.avgOee?.toFixed(1) ?? '-' }}%
        </div>
        <div class="stat-label">平均效率</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ monthData?.workDays ?? '-' }}</div>
        <div class="stat-label">工作天数</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { DailyStatistics, MonthlyStatistics } from '@/types'

defineProps<{
  todayData: DailyStatistics | null
  monthData: MonthlyStatistics | null
}>()

const activePeriod = ref<'today' | 'month'>('today')
</script>

<style scoped lang="scss">
.statistics-overview {
  background: linear-gradient(135deg, rgba(0, 20, 40, 0.9) 0%, rgba(0, 40, 80, 0.8) 100%);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    color: #00d4ff;
    font-size: 16px;
    font-weight: 500;
    margin: 0;
  }
}

.period-tabs {
  display: flex;
  gap: 8px;

  button {
    padding: 4px 12px;
    border: 1px solid rgba(0, 212, 255, 0.5);
    border-radius: 4px;
    background: transparent;
    color: #8ec6e8;
    font-size: 12px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #00d4ff;
      color: #00d4ff;
    }

    &.active {
      background: rgba(0, 212, 255, 0.2);
      border-color: #00d4ff;
      color: #00d4ff;
    }
  }
}

.overview-content {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  flex: 1;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
  background: rgba(0, 50, 100, 0.3);
  border-radius: 6px;
  border: 1px solid rgba(0, 212, 255, 0.1);

  .stat-value {
    font-size: 24px;
    font-weight: bold;
    color: #fff;
    margin-bottom: 4px;

    &.success {
      color: #52c41a;
    }

    &.warning {
      color: #faad14;
    }

    &.info {
      color: #00d4ff;
    }
  }

  .stat-label {
    font-size: 12px;
    color: #8ec6e8;
  }
}

@media (max-width: 1200px) {
  .overview-content {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
