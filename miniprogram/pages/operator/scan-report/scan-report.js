const app = getApp()
const { get, post } = require('../../../utils/request')
const { showLoading, hideLoading, showToast, showSuccess, showModal, parseQRCode } = require('../../../utils/util')

Page({
  data: {
    scanned: false,
    processInfo: {},
    reportForm: {
      workOrderProcessId: null,
      completedQuantity: 1,
      scrapQuantity: 0,
      workHours: 0,
      remark: ''
    },
    submitting: false
  },

  onLoad(options) {
    if (!app.checkLogin()) {
      app.goToLogin()
      return
    }

    // 如果从任务详情页传入了工序ID，直接加载工序信息
    if (options.processId) {
      this.loadProcessInfo(options.processId)
    }
  },

  // 开始扫码
  startScan() {
    wx.scanCode({
      onlyFromCamera: false,
      scanType: ['qrCode'],
      success: (res) => {
        console.log('扫码结果:', res.result)
        const qrData = parseQRCode(res.result)
        if (qrData && qrData.processId) {
          this.loadProcessInfo(qrData.processId)
        } else {
          showToast('无效的二维码')
        }
      },
      fail: (err) => {
        console.log('扫码取消或失败:', err)
        if (err.errMsg !== 'scanCode:fail cancel') {
          showToast('扫码失败，请重试')
        }
      }
    })
  },

  // 加载工序信息
  async loadProcessInfo(processId) {
    showLoading('加载中...')
    try {
      const res = await get(`/work-order-processes/${processId}`)
      hideLoading()

      if (res.code === 200 && res.data) {
        const info = res.data
        const remainingQuantity = info.planQuantity - info.completedQuantity

        if (remainingQuantity <= 0) {
          showModal({
            title: '提示',
            content: '该工序已全部完成',
            showCancel: false
          })
          return
        }

        this.setData({
          scanned: true,
          processInfo: {
            ...info,
            remainingQuantity
          },
          reportForm: {
            workOrderProcessId: processId,
            completedQuantity: Math.min(1, remainingQuantity),
            scrapQuantity: 0,
            workHours: info.standardTime || 0,
            remark: ''
          }
        })
      } else {
        showToast(res.message || '工序不存在')
      }
    } catch (error) {
      hideLoading()
      showToast(error.message || '加载失败')
    }
  },

  // 重新扫码
  resetScan() {
    this.setData({
      scanned: false,
      processInfo: {},
      reportForm: {
        workOrderProcessId: null,
        completedQuantity: 1,
        scrapQuantity: 0,
        workHours: 0,
        remark: ''
      }
    })
  },

  // 完成数量输入
  onQuantityInput(e) {
    let value = parseInt(e.detail.value) || 0
    const max = this.data.processInfo.remainingQuantity
    value = Math.max(0, Math.min(value, max))
    this.setData({ 'reportForm.completedQuantity': value })
  },

  increaseQuantity() {
    const current = this.data.reportForm.completedQuantity
    const max = this.data.processInfo.remainingQuantity
    if (current < max) {
      this.setData({ 'reportForm.completedQuantity': current + 1 })
    }
  },

  decreaseQuantity() {
    const current = this.data.reportForm.completedQuantity
    if (current > 0) {
      this.setData({ 'reportForm.completedQuantity': current - 1 })
    }
  },

  // 报废数量输入
  onScrapInput(e) {
    let value = parseInt(e.detail.value) || 0
    value = Math.max(0, value)
    this.setData({ 'reportForm.scrapQuantity': value })
  },

  increaseScrap() {
    const current = this.data.reportForm.scrapQuantity
    this.setData({ 'reportForm.scrapQuantity': current + 1 })
  },

  decreaseScrap() {
    const current = this.data.reportForm.scrapQuantity
    if (current > 0) {
      this.setData({ 'reportForm.scrapQuantity': current - 1 })
    }
  },

  // 工时输入
  onWorkHoursInput(e) {
    let value = parseInt(e.detail.value) || 0
    value = Math.max(0, value)
    this.setData({ 'reportForm.workHours': value })
  },

  // 备注输入
  onRemarkInput(e) {
    this.setData({ 'reportForm.remark': e.detail.value })
  },

  // 提交报工
  async submitReport() {
    const { reportForm, processInfo } = this.data

    // 校验
    if (reportForm.completedQuantity <= 0) {
      showToast('请输入完成数量')
      return
    }

    if (reportForm.completedQuantity > processInfo.remainingQuantity) {
      showToast('完成数量不能超过待完成数量')
      return
    }

    // 确认提交
    const confirmed = await showModal({
      title: '确认提交',
      content: `确定提交报工？\n完成数量: ${reportForm.completedQuantity}\n报废数量: ${reportForm.scrapQuantity}`
    })

    if (!confirmed) return

    this.setData({ submitting: true })
    showLoading('提交中...')

    try {
      const res = await post('/work-reports', reportForm)
      hideLoading()

      if (res.code === 200) {
        showSuccess('报工成功')

        // 延迟返回
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        showToast(res.message || '提交失败')
      }
    } catch (error) {
      hideLoading()
      showToast(error.message || '提交失败')
    } finally {
      this.setData({ submitting: false })
    }
  }
})
