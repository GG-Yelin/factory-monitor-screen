const app = getApp()
const { get } = require('../../utils/request')
const { showToast, getStatusInfo } = require('../../utils/util')

Page({
  data: {
    tasks: [],
    loading: false,
    currentStatus: '',
    page: 0,
    size: 10,
    hasMore: true,
    statusLabels: {
      '': '',
      PENDING: '待开工',
      IN_PROGRESS: '进行中',
      COMPLETED: '已完成'
    }
  },

  onLoad() {
    if (!app.checkLogin()) {
      app.goToLogin()
    }
  },

  onShow() {
    this.resetAndLoad()
  },

  onPullDownRefresh() {
    this.resetAndLoad().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMore()
    }
  },

  // 重置并加载
  resetAndLoad() {
    this.setData({
      tasks: [],
      page: 0,
      hasMore: true
    })
    return this.loadTasks()
  },

  // 加载任务列表
  async loadTasks() {
    if (this.data.loading) return

    this.setData({ loading: true })

    try {
      const params = {
        page: this.data.page,
        size: this.data.size
      }

      if (this.data.currentStatus) {
        params.status = this.data.currentStatus
      }

      const res = await get('/work-reports/my-tasks', params)

      if (res.code === 200 && res.data) {
        const newTasks = (res.data.content || []).map(task => {
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

        const hasMore = !res.data.last

        this.setData({
          tasks: this.data.page === 0 ? newTasks : [...this.data.tasks, ...newTasks],
          hasMore
        })
      }
    } catch (error) {
      console.error('加载任务失败:', error)
      showToast('加载失败')
    } finally {
      this.setData({ loading: false })
    }
  },

  // 加载更多
  loadMore() {
    this.setData({ page: this.data.page + 1 })
    this.loadTasks()
  },

  // 状态筛选
  onFilterChange(e) {
    const status = e.currentTarget.dataset.status
    this.setData({ currentStatus: status })
    this.resetAndLoad()
  },

  // 跳转到详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/tasks/detail/detail?id=${id}`
    })
  },

  // 跳转到报工
  goToReport(e) {
    const id = e.currentTarget.dataset.id
    const task = this.data.tasks.find(t => t.id === id)

    if (task && task.status !== 'COMPLETED') {
      wx.navigateTo({
        url: `/pages/operator/scan-report/scan-report?processId=${id}`
      })
    } else {
      wx.navigateTo({
        url: `/pages/tasks/detail/detail?id=${id}`
      })
    }
  }
})
