<template>
  <div class="card chart-card">
    <div class="card-title">设备状态</div>
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  online: number
  offline: number
  alarm: number
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const initChart = () => {
  if (!chartRef.value) return

  chart = echarts.init(chartRef.value)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0, 30, 60, 0.9)',
      borderColor: 'rgba(0, 150, 255, 0.5)',
      textStyle: { color: '#fff' }
    },
    legend: {
      orient: 'vertical',
      right: 20,
      top: 'center',
      textStyle: { color: 'rgba(255, 255, 255, 0.7)' }
    },
    series: [
      {
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        label: {
          show: true,
          position: 'center',
          formatter: () => {
            const total = props.online + props.offline + props.alarm
            return `{value|${total}}\n{label|设备总数}`
          },
          rich: {
            value: {
              fontSize: 28,
              fontWeight: 'bold',
              color: '#fff'
            },
            label: {
              fontSize: 14,
              color: 'rgba(255, 255, 255, 0.7)',
              padding: [5, 0, 0, 0]
            }
          }
        },
        labelLine: { show: false },
        data: [
          {
            value: props.online,
            name: '在线',
            itemStyle: { color: '#00ff88' }
          },
          {
            value: props.offline,
            name: '离线',
            itemStyle: { color: '#666' }
          },
          {
            value: props.alarm,
            name: '报警',
            itemStyle: { color: '#ff4444' }
          }
        ]
      }
    ]
  }

  chart.setOption(option)
}

const updateChart = () => {
  if (!chart) return

  chart.setOption({
    series: [{
      data: [
        { value: props.online, name: '在线' },
        { value: props.offline, name: '离线' },
        { value: props.alarm, name: '报警' }
      ],
      label: {
        formatter: () => {
          const total = props.online + props.offline + props.alarm
          return `{value|${total}}\n{label|设备总数}`
        }
      }
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

watch(() => [props.online, props.offline, props.alarm], updateChart)
</script>

<style scoped lang="scss">
.chart-container {
  width: 100%;
  height: calc(100% - 40px);
  min-height: 200px;
}
</style>
