<template>
  <div class="chart-container">
    <div class="chart-header">
      <h3>月度产量统计</h3>
      <div class="chart-legend">
        <span class="legend-item">
          <span class="dot production"></span>产量
        </span>
        <span class="legend-item">
          <span class="dot plan"></span>计划
        </span>
      </div>
    </div>
    <div ref="chartRef" class="chart"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { MonthlyStatistics } from '@/types'

const props = defineProps<{
  data: MonthlyStatistics[]
}>()

const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

const initChart = () => {
  if (!chartRef.value) return

  chart = echarts.init(chartRef.value)

  updateChart()
}

const updateChart = () => {
  if (!chart) return

  const months = props.data.map((item) => `${item.year}-${String(item.month).padStart(2, '0')}`)
  const productions = props.data.map((item) => item.totalProduction || 0)
  const plans = props.data.map((item) => item.totalPlan || 0)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: '#00d4ff',
      textStyle: { color: '#fff' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      axisLine: { lineStyle: { color: '#1a3a5c' } },
      axisLabel: { color: '#8ec6e8', fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#1a3a5c' } },
      axisLabel: { color: '#8ec6e8' },
      splitLine: { lineStyle: { color: 'rgba(30, 70, 100, 0.3)' } }
    },
    series: [
      {
        name: '产量',
        type: 'bar',
        data: productions,
        barWidth: '40%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#00d4ff' },
            { offset: 1, color: '#0066cc' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '计划',
        type: 'line',
        data: plans,
        smooth: true,
        lineStyle: { color: '#ffd700', width: 2 },
        itemStyle: { color: '#ffd700' },
        symbol: 'circle',
        symbolSize: 6
      }
    ]
  }

  chart.setOption(option)
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

watch(
  () => props.data,
  () => updateChart(),
  { deep: true }
)
</script>

<style scoped lang="scss">
.chart-container {
  background: linear-gradient(135deg, rgba(0, 20, 40, 0.9) 0%, rgba(0, 40, 80, 0.8) 100%);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  h3 {
    color: #00d4ff;
    font-size: 16px;
    font-weight: 500;
    margin: 0;
  }
}

.chart-legend {
  display: flex;
  gap: 16px;

  .legend-item {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #8ec6e8;
    font-size: 12px;

    .dot {
      width: 10px;
      height: 10px;
      border-radius: 2px;

      &.production {
        background: linear-gradient(180deg, #00d4ff 0%, #0066cc 100%);
      }

      &.plan {
        background: #ffd700;
        border-radius: 50%;
      }
    }
  }
}

.chart {
  flex: 1;
  min-height: 200px;
}
</style>
