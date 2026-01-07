const app = getApp()
const { get } = require('../../utils/request')
const { showToast, parseQRCode } = require('../../utils/util')

Page({
  data: {
    userInfo: {},
    todayStats: {
      inspectedCount: 0,
      passedCount: 0,
      passRate: '0'
    },
    pendingList: [],
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
    this.loadPendingList()
  },

  onPullDownRefresh() {
    Promise.all([this.loadTodayStats(), this.loadPendingList()])
      .finally(() => {
        wx.stopPullDownRefresh()
      })
  },

  // 加载今日统计
  async loadTodayStats() {
    try {
      const res = await get('/quality-inspections/today-stats')
      if (res.code === 200 && res.data) {
        const { inspectedCount = 0, passedCount = 0 } = res.data
        const passRate = inspectedCount > 0
          ? Math.round((passedCount / inspectedCount) * 100)
          : 0
        this.setData({
          todayStats: {
            inspectedCount,
            passedCount,
            passRate
          }
        })
      }
    } catch (error) {
      console.error('加载今日统计失败:', error)
    }
  },

  // 加载待质检列表
  async loadPendingList() {
    this.setData({ loading: true })
    try {
      const res = await get('/quality-inspections/pending', { size: 5 })
      if (res.code === 200 && res.data) {
        this.setData({ pendingList: res.data.content || [] })
      }
    } catch (error) {
      console.error('加载待质检列表失败:', error)
      showToast('加载失败')
    } finally {
      this.setData({ loading: false })
    }
  },

  // 扫码质检
  goToScanInspection() {
    wx.scanCode({
      onlyFromCamera: false,
      scanType: ['qrCode'],
      success: (res) => {
        const qrData = parseQRCode(res.result)
        if (qrData && qrData.workOrderId) {
          wx.navigateTo({
            url: `/pages/inspector/inspection/inspection?workOrderId=${qrData.workOrderId}`
          })
        } else {
          showToast('无效的二维码')
        }
      },
      fail: (err) => {
        if (err.errMsg !== 'scanCode:fail cancel') {
          showToast('扫码失败')
        }
      }
    })
  },

  // 质检记录列表
  goToInspectionList() {
    wx.navigateTo({
      url: '/pages/inspector/inspection/inspection?mode=list'
    })
  },

  // 进入质检
  goToInspection(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/inspector/inspection/inspection?workOrderId=${id}`
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
  }
})
