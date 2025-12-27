<template>
  <div class="card datapoint-list-card">
    <div class="card-title">实时数据</div>
    <div class="datapoint-wrapper">
      <div class="datapoint-grid">
        <div
          v-for="point in displayPoints"
          :key="point.id"
          class="datapoint-item"
        >
          <div class="point-name">{{ point.name }}</div>
          <div class="point-value" :class="{ bool: point.isCoil }">
            {{ formatValue(point) }}
          </div>
          <div class="point-device">{{ point.deviceName }}</div>
        </div>
        <div v-if="dataPoints.length === 0" class="no-data">
          暂无数据
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DataPoint } from '@/types'

const props = defineProps<{
  dataPoints: DataPoint[]
}>()

// 只显示前8个数据点
const displayPoints = computed(() => {
  return props.dataPoints.slice(0, 8)
})

const formatValue = (point: DataPoint) => {
  if (point.isCoil) {
    return point.value === '1' || point.valueString === 'on' ? 'ON' : 'OFF'
  }
  if (point.unit) {
    return `${point.valueString || point.value} ${point.unit}`
  }
  return point.valueString || point.value
}
</script>

<style scoped lang="scss">
.datapoint-list-card {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.datapoint-wrapper {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.datapoint-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.datapoint-item {
  background: rgba(0, 100, 200, 0.1);
  border: 1px solid rgba(0, 150, 255, 0.2);
  border-radius: 4px;
  padding: 8px;
  text-align: center;
  transition: all 0.3s;

  &:hover {
    background: rgba(0, 100, 200, 0.2);
    border-color: rgba(0, 150, 255, 0.4);
  }
}

.point-name {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.point-value {
  font-size: 16px;
  font-weight: bold;
  color: #00d4ff;
  margin-bottom: 4px;

  &.bool {
    color: #00ff88;
  }
}

.point-device {
  font-size: 9px;
  color: rgba(255, 255, 255, 0.4);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.no-data {
  grid-column: span 2;
  text-align: center;
  padding: 20px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}
</style>
