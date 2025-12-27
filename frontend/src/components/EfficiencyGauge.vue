<template>
  <div class="card gauge-card">
    <div class="card-title">{{ title }}</div>
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  title: string
  value: number
  color?: string
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const getColor = () => {
  if (props.color) return props.color
  if (props.value >= 90) return '#00ff88'
  if (props.value >= 70) return '#00d4ff'
  if (props.value >= 50) return '#ffaa00'
  return '#ff4444'
}

const initChart = () => {
  if (!chartRef.value) return

  chart = echarts.init(chartRef.value)

  const option: echarts.EChartsOption = {
    series: [
      {
        type: 'gauge',
        startAngle: 200,
        endAngle: -20,
        min: 0,
        max: 100,
        splitNumber: 10,
        radius: '90%',
        center: ['50%', '60%'],
        itemStyle: {
          color: getColor()
        },
        progress: {
          show: true,
          width: 15
        },
        pointer: {
          show: false
        },
        axisLine: {
          lineStyle: {
            width: 15,
            color: [[1, 'rgba(255, 255, 255, 0.1)']]
          }
        },
        axisTick: {
          show: false
        },
        splitLine: {
          show: false
        },
        axisLabel: {
          show: false
        },
        anchor: {
          show: false
        },
        title: {
          show: false
        },
        detail: {
          valueAnimation: true,
          fontSize: 24,
          fontWeight: 'bold',
          offsetCenter: [0, 0],
          formatter: '{value}%',
          color: '#fff'
        },
        data: [{ value: props.value }]
      }
    ]
  }

  chart.setOption(option)
}

const updateChart = () => {
  if (!chart) return

  chart.setOption({
    series: [{
      data: [{ value: props.value }],
      itemStyle: { color: getColor() }
    }]
  })
}

const handleResize = () => {
  chart?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})

watch(() => props.value, updateChart)
</script>

<style scoped lang="scss">
.gauge-card {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chart-container {
  flex: 1;
  width: 100%;
  min-height: 0;
}
</style>
