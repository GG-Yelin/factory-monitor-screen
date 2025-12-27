<template>
  <div class="card stat-card" :class="type">
    <div class="value">{{ formattedValue }}</div>
    <div class="label">{{ label }}</div>
    <div class="sub-info" v-if="subInfo">{{ subInfo }}</div>
    <div class="progress-bar" v-if="showProgress">
      <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  value: number | string
  label: string
  subInfo?: string
  type?: 'default' | 'warning' | 'danger' | 'success'
  showProgress?: boolean
  progressPercent?: number
  suffix?: string
}>()

const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    if (props.suffix) {
      return props.value.toLocaleString() + props.suffix
    }
    return props.value.toLocaleString()
  }
  return props.value
})
</script>
