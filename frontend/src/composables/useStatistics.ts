import { ref, onMounted } from 'vue'
import type { DailyStatistics, MonthlyStatistics, StatisticsOverview } from '@/types'

const API_BASE = 'http://localhost:8080/api/statistics'

export function useStatistics() {
  const loading = ref(false)
  const error = ref<string | null>(null)
  const overview = ref<StatisticsOverview | null>(null)
  const dailyStats = ref<DailyStatistics[]>([])
  const monthlyStats = ref<MonthlyStatistics[]>([])

  // 获取统计概览
  const fetchOverview = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`${API_BASE}/overview`)
      if (!response.ok) throw new Error('Failed to fetch overview')
      overview.value = await response.json()
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unknown error'
      console.error('Failed to fetch overview:', e)
    } finally {
      loading.value = false
    }
  }

  // 获取最近N天的统计
  const fetchRecentDailyStats = async (days: number = 7) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`${API_BASE}/daily/recent?days=${days}`)
      if (!response.ok) throw new Error('Failed to fetch daily stats')
      dailyStats.value = await response.json()
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unknown error'
      console.error('Failed to fetch daily stats:', e)
    } finally {
      loading.value = false
    }
  }

  // 获取日期范围内的统计
  const fetchDailyStats = async (startDate: string, endDate: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(
        `${API_BASE}/daily?startDate=${startDate}&endDate=${endDate}`
      )
      if (!response.ok) throw new Error('Failed to fetch daily stats')
      dailyStats.value = await response.json()
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unknown error'
      console.error('Failed to fetch daily stats:', e)
    } finally {
      loading.value = false
    }
  }

  // 获取某年的月度统计
  const fetchMonthlyStats = async (year?: number) => {
    loading.value = true
    error.value = null
    try {
      const url = year
        ? `${API_BASE}/monthly?year=${year}`
        : `${API_BASE}/monthly`
      const response = await fetch(url)
      if (!response.ok) throw new Error('Failed to fetch monthly stats')
      monthlyStats.value = await response.json()
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unknown error'
      console.error('Failed to fetch monthly stats:', e)
    } finally {
      loading.value = false
    }
  }

  // 获取最近N个月的统计
  const fetchRecentMonthlyStats = async (months: number = 12) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`${API_BASE}/monthly/recent?months=${months}`)
      if (!response.ok) throw new Error('Failed to fetch monthly stats')
      monthlyStats.value = await response.json()
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unknown error'
      console.error('Failed to fetch monthly stats:', e)
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    error,
    overview,
    dailyStats,
    monthlyStats,
    fetchOverview,
    fetchRecentDailyStats,
    fetchDailyStats,
    fetchMonthlyStats,
    fetchRecentMonthlyStats
  }
}
