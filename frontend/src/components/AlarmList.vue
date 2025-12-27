<template>
  <div class="card alarm-list-card">
    <div class="card-title">报警信息</div>
    <div class="alarm-wrapper">
      <div v-if="alarms.length === 0" class="no-alarm">
        <span class="icon">&#10003;</span>
        <span>暂无报警</span>
      </div>
      <div v-else class="alarm-list">
        <div
          v-for="alarm in alarms"
          :key="alarm.id"
          class="alarm-item"
          :class="'level-' + alarm.level"
        >
          <div class="alarm-header">
            <span class="alarm-device">{{ alarm.deviceName }}</span>
            <span class="alarm-level">{{ getLevelText(alarm.level) }}</span>
          </div>
          <div class="alarm-content">{{ alarm.alarmContent }}</div>
          <div class="alarm-time">{{ alarm.alarmTime }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AlarmInfo } from '@/types'

defineProps<{
  alarms: AlarmInfo[]
}>()

const getLevelText = (level: number) => {
  switch (level) {
    case 1: return '一般'
    case 2: return '重要'
    case 3: return '紧急'
    default: return '未知'
  }
}
</script>

<style scoped lang="scss">
.alarm-list-card {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.alarm-wrapper {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.no-alarm {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #00ff88;

  .icon {
    font-size: 36px;
    margin-bottom: 8px;
  }
}

.alarm-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.alarm-item {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
  padding: 8px;
  border-left: 3px solid #ffaa00;

  &.level-1 {
    border-left-color: #ffaa00;
  }

  &.level-2 {
    border-left-color: #ff6600;
  }

  &.level-3 {
    border-left-color: #ff0000;
    animation: blink 1s infinite;
  }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.alarm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.alarm-device {
  font-weight: bold;
  font-size: 12px;
  color: #fff;
}

.alarm-level {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 3px;
  background: rgba(255, 100, 0, 0.3);
  color: #ffaa00;
}

.alarm-content {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 4px;
}

.alarm-time {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
}
</style>
