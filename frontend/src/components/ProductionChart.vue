<template>
  <div class="card chart-card">
    <div class="card-title">生产趋势</div>
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { ProductionTrend } from '@/types'

const props = defineProps<{
  data: ProductionTrend[]
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const initChart = () => {
  if (!chartRef.value) return

  chart = echarts.init(chartRef.value)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0, 30, 60, 0.9)',
      borderColor: 'rgba(0, 150, 255, 0.5)',
      textStyle: { color: '#fff' }
    },
    legend: {
      data: ['实际产量', '计划产量'],
      textStyle: { color: 'rgba(255, 255, 255, 0.7)' },
      top: 0
    },
    grid: {
      top: 30,
      left: 45,
      right: 15,
      bottom: 25
    },
    xAxis: {
      type: 'category',
      data: props.data.map(d => d.date),
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.3)' } },
      axisLabel: { color: 'rgba(255, 255, 255, 0.7)' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.3)' } },
      axisLabel: { color: 'rgba(255, 255, 255, 0.7)' },
      splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.1)' } }
    },
    series: [
      {
        name: '实际产量',
        type: 'bar',
        data: props.data.map(d => d.production),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#00d4ff' },
            { offset: 1, color: '#0066cc' }
          ])
        },
        barWidth: '40%'
      },
      {
        name: '计划产量',
        type: 'line',
        data: props.data.map(d => d.plan),
        lineStyle: { color: '#00ff88', width: 2 },
        itemStyle: { color: '#00ff88' },
        symbol: 'circle',
        symbolSize: 8
      }
    ]
  }

  chart.setOption(option)
}

const updateChart = () => {
  if (!chart) return

  chart.setOption({
    xAxis: { data: props.data.map(d => d.date) },
    series: [
      { data: props.data.map(d => d.production) },
      { data: props.data.map(d => d.plan) }
    ]
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

watch(() => props.data, updateChart, { deep: true })
</script>

<style scoped lang="scss">
.chart-card {
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
