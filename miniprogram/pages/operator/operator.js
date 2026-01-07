const app = getApp()
const { get } = require('../../utils/request')
const { showToast, getStatusInfo } = require('../../utils/util')

Page({
  data: {
    userInfo: {},
    todayStats: {
      completedCount: 0,
      totalQuantity: 0,
      wage: '0.00'
    },
    tasks: [],
    loading: false
  },

  onLoad() {
    if (!app.checkLogin()) {
      app.goToLogin()
      return
    }
  },

  onShow() {
    this.setData({ userInfo: app.globalData.userInfo || {} })
    this.loadTodayStats()
    this.loadTasks()
  },

  onPullDownRefresh() {
    Promise.all([this.loadTodayStats(), this.loadTasks()])
      .finally(() => {
        wx.stopPullDownRefresh()
      })
  },

  // 加载今日统计
  async loadTodayStats() {
    try {
      const res = await get('/work-reports/today-stats')
      if (res.code === 200 && res.data) {
        this.setData({
          todayStats: {
            completedCount: res.data.completedCount || 0,
            totalQuantity: res.data.totalQuantity || 0,
            wage: (res.data.wage || 0).toFixed(2)
          }
        })
      }
    } catch (error) {
      console.error('加载今日统计失败:', error)
    }
  },

  // 加载进行中的任务
  async loadTasks() {
    this.setData({ loading: true })
    try {
      const res = await get('/work-reports/my-tasks', { status: 'IN_PROGRESS', size: 5 })
      if (res.code === 200 && res.data) {
        const tasks = (res.data.content || []).map(task => {
          const statusInfo = getStatusInfo(task.status)
          const progress = task.planQuantity > 0
            ? Math.round((task.completedQuantity / task.planQuantity) * 100)
            : 0
          return {
            ...task,
            statusLabel: statusInfo.label,
            statusType: statusInfo.type,
            progress
          }
        })
        this.setData({ tasks })
      }
    } catch (error) {
      console.error('加载任务失败:', error)
      showToast('加载任务失败')
    } finally {
      this.setData({ loading: false })
    }
  },

  // 跳转到扫码报工
  goToScanReport() {
    wx.navigateTo({
      url: '/pages/operator/scan-report/scan-report'
    })
  },

  // 跳转到我的任务
  goToTasks() {
    wx.switchTab({
      url: '/pages/tasks/tasks'
    })
  },

  // 跳转到我的工资
  goToWage() {
    wx.navigateTo({
      url: '/pages/profile/wage/wage'
    })
  },

  // 跳转到个人中心
  goToProfile() {
    wx.switchTab({
      url: '/pages/profile/profile'
    })
  },

  // 跳转到任务详情
  goToTaskDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/tasks/detail/detail?id=${id}`
    })
  }
})
