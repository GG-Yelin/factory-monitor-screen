const app = getApp()
const { get } = require('../../../utils/request')
const { showToast, formatDate } = require('../../../utils/util')

Page({
  data: {
    currentPeriod: 'month',
    periodLabel: '本月',
    startDate: '',
    endDate: '',
    summary: {
      totalAmount: '0.00',
      totalQuantity: 0,
      recordCount: 0
    },
    records: [],
    loading: false,
    page: 0,
    size: 20,
    hasMore: true
  },

  onLoad() {
    if (!app.checkLogin()) {
      app.goToLogin()
      return
    }

    this.initDateRange('month')
    this.loadData()
  },

  onPullDownRefresh() {
    this.resetAndLoad().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  // 初始化日期范围
  initDateRange(period) {
    const now = new Date()
    let startDate, endDate

    switch (period) {
      case 'day':
        startDate = endDate = formatDate(now, 'YYYY-MM-DD')
        break
      case 'week':
        const weekStart = new Date(now)
        weekStart.setDate(now.getDate() - now.getDay() + 1)
        startDate = formatDate(weekStart, 'YYYY-MM-DD')
        endDate = formatDate(now, 'YYYY-MM-DD')
        break
      case 'month':
      default:
        const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
        startDate = formatDate(monthStart, 'YYYY-MM-DD')
        endDate = formatDate(now, 'YYYY-MM-DD')
        break
    }

    const periodLabels = {
      day: '今日',
      week: '本周',
      month: '本月',
      custom: '自定义'
    }

    this.setData({
      currentPeriod: period,
      periodLabel: periodLabels[period],
      startDate,
      endDate
    })
  },

  // 切换周期
  changePeriod(e) {
    const period = e.currentTarget.dataset.period
    this.initDateRange(period)
    this.resetAndLoad()
  },

  // 显示日期选择器
  showDatePicker() {
    // 简化处理，使用本月
    this.initDateRange('month')
    this.setData({ currentPeriod: 'custom', periodLabel: '自定义' })
    this.resetAndLoad()
  },

  // 重置并加载
  resetAndLoad() {
    this.setData({
      records: [],
      page: 0,
      hasMore: true
    })
    return this.loadData()
  },

  // 加载数据
  async loadData() {
    if (this.data.loading) return

    this.setData({ loading: true })

    try {
      const params = {
        startDate: this.data.startDate,
        endDate: this.data.endDate,
        page: this.data.page,
        size: this.data.size
      }

      const res = await get('/wages/my', params)

      if (res.code === 200 && res.data) {
        const newRecords = res.data.content || []
        const hasMore = !res.data.last

        // 计算汇总
        let totalAmount = 0
        let totalQuantity = 0
        const allRecords = this.data.page === 0 ? newRecords : [...this.data.records, ...newRecords]

        // 如果是第一页，从响应中获取汇总（假设后端返回）
        if (res.data.summary) {
          totalAmount = res.data.summary.totalAmount || 0
          totalQuantity = res.data.summary.totalQuantity || 0
        } else {
          // 否则累加计算
          allRecords.forEach(r => {
            totalAmount += r.wage || 0
            totalQuantity += r.quantity || 0
          })
        }

        this.setData({
          records: allRecords,
          hasMore,
          summary: {
            totalAmount: totalAmount.toFixed(2),
            totalQuantity,
            recordCount: res.data.totalElements || allRecords.length
          }
        })
      }
    } catch (error) {
      console.error('加载工资数据失败:', error)
      showToast('加载失败')
    } finally {
      this.setData({ loading: false })
    }
  },

  // 加载更多
  loadMore() {
    this.setData({ page: this.data.page + 1 })
    this.loadData()
  }
})
