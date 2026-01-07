const app = getApp()
const { get } = require('../../../utils/request')
const { showLoading, hideLoading, showToast, getStatusInfo } = require('../../../utils/util')

Page({
  data: {
    taskId: null,
    taskInfo: {},
    reports: []
  },

  onLoad(options) {
    if (!app.checkLogin()) {
      app.goToLogin()
      return
    }

    if (options.id) {
      this.setData({ taskId: options.id })
      this.loadTaskInfo(options.id)
      this.loadReports(options.id)
    }
  },

  onShow() {
    if (this.data.taskId) {
      this.loadTaskInfo(this.data.taskId)
      this.loadReports(this.data.taskId)
    }
  },

  // 加载任务详情
  async loadTaskInfo(id) {
    showLoading('加载中...')
    try {
      const res = await get(`/work-order-processes/${id}`)
      hideLoading()

      if (res.code === 200 && res.data) {
        const info = res.data
        const statusInfo = getStatusInfo(info.status)
        const progress = info.planQuantity > 0
          ? Math.round((info.completedQuantity / info.planQuantity) * 100)
          : 0

        this.setData({
          taskInfo: {
            ...info,
            statusLabel: statusInfo.label,
            statusType: statusInfo.type,
            progress,
            remainingQuantity: info.planQuantity - info.completedQuantity
          }
        })
      } else {
        showToast(res.message || '加载失败')
      }
    } catch (error) {
      hideLoading()
      showToast(error.message || '加载失败')
    }
  },

  // 加载报工记录
  async loadReports(processId) {
    try {
      const res = await get(`/work-reports/by-process/${processId}`)

      if (res.code === 200 && res.data) {
        this.setData({ reports: res.data || [] })
      }
    } catch (error) {
      console.error('加载报工记录失败:', error)
    }
  },

  // 跳转到报工
  goToReport() {
    wx.navigateTo({
      url: `/pages/operator/scan-report/scan-report?processId=${this.data.taskId}`
    })
  }
})
