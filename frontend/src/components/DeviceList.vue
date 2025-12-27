<template>
  <div class="card device-list-card">
    <div class="card-title">设备列表</div>
    <div class="table-wrapper">
      <table class="data-table">
        <thead>
          <tr>
            <th>设备名称</th>
            <th>类型</th>
            <th>所属项目</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="device in devices" :key="device.deviceId">
            <td>{{ device.deviceName }}</td>
            <td>{{ device.deviceType }}</td>
            <td>{{ device.itemName || '-' }}</td>
            <td>
              <span :class="getStatusClass(device.status)">
                {{ getStatusText(device.status) }}
              </span>
            </td>
          </tr>
          <tr v-if="devices.length === 0">
            <td colspan="4" style="text-align: center; color: rgba(255,255,255,0.5)">
              暂无设备数据
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { DeviceInfo } from '@/types'

defineProps<{
  devices: DeviceInfo[]
}>()

const getStatusClass = (status: number) => {
  switch (status) {
    case 1: return 'status-online'
    case 0: return 'status-offline'
    case 2: return 'status-alarm'
    default: return ''
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 1: return '在线'
    case 0: return '离线'
    case 2: return '报警'
    default: return '未知'
  }
}
</script>

<style scoped lang="scss">
.device-list-card {
  grid-column: span 2;
  display: flex;
  flex-direction: column;
}

.table-wrapper {
  flex: 1;
  overflow-y: auto;
  margin-top: 10px;
}
</style>
