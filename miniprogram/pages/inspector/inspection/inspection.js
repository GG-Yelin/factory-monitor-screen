const app = getApp()
const { get, post, uploadFile } = require('../../../utils/request')
const { showLoading, hideLoading, showToast, showSuccess, showModal, parseQRCode } = require('../../../utils/util')

Page({
  data: {
    loading: false,
    submitting: false,
    workOrderInfo: {},
    form: {
      workOrderId: null,
      inspectedQuantity: 0,
      qualifiedQuantity: 0,
      unqualifiedQuantity: 0,
      reason: '',
      reasonDetail: ''
    },
    images: [],
    reasonOptions: ['尺寸不合格', '外观缺陷', '功能异常', '材料问题', '工艺问题', '其他']
  },

  onLoad(options) {
    if (!app.checkLogin()) {
      app.goToLogin()
      return
    }

    if (options.workOrderId) {
      this.loadWorkOrderInfo(options.workOrderId)
    }
  },

  // 加载工单信息
  async loadWorkOrderInfo(workOrderId) {
    this.setData({ loading: true })
    showLoading('加载中...')

    try {
      const res = await get(`/work-orders/${workOrderId}`)
      hideLoading()

      if (res.code === 200 && res.data) {
        const info = res.data
        this.setData({
          workOrderInfo: info,
          form: {
            workOrderId: info.id,
            inspectedQuantity: info.completedQuantity,
            qualifiedQuantity: info.completedQuantity,
            unqualifiedQuantity: 0,
            reason: '',
            reasonDetail: ''
          }
        })
      } else {
        showToast(res.message || '工单不存在')
      }
    } catch (error) {
      hideLoading()
      showToast(error.message || '加载失败')
    } finally {
      this.setData({ loading: false })
    }
  },

  // 扫码
  startScan() {
    wx.scanCode({
      onlyFromCamera: false,
      scanType: ['qrCode'],
      success: (res) => {
        const qrData = parseQRCode(res.result)
        if (qrData && qrData.workOrderId) {
          this.loadWorkOrderInfo(qrData.workOrderId)
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

  // 检验数量操作
  onInspectedInput(e) {
    let value = parseInt(e.detail.value) || 0
    value = Math.max(0, value)
    const qualifiedQuantity = Math.min(this.data.form.qualifiedQuantity, value)
    this.setData({
      'form.inspectedQuantity': value,
      'form.qualifiedQuantity': qualifiedQuantity,
      'form.unqualifiedQuantity': value - qualifiedQuantity
    })
  },

  increaseInspected() {
    const current = this.data.form.inspectedQuantity
    this.setData({ 'form.inspectedQuantity': current + 1 })
    this.updateUnqualified()
  },

  decreaseInspected() {
    const current = this.data.form.inspectedQuantity
    if (current > 0) {
      const newValue = current - 1
      const qualifiedQuantity = Math.min(this.data.form.qualifiedQuantity, newValue)
      this.setData({
        'form.inspectedQuantity': newValue,
        'form.qualifiedQuantity': qualifiedQuantity
      })
      this.updateUnqualified()
    }
  },

  // 合格数量操作
  onQualifiedInput(e) {
    let value = parseInt(e.detail.value) || 0
    value = Math.max(0, Math.min(value, this.data.form.inspectedQuantity))
    this.setData({ 'form.qualifiedQuantity': value })
    this.updateUnqualified()
  },

  increaseQualified() {
    const current = this.data.form.qualifiedQuantity
    if (current < this.data.form.inspectedQuantity) {
      this.setData({ 'form.qualifiedQuantity': current + 1 })
      this.updateUnqualified()
    }
  },

  decreaseQualified() {
    const current = this.data.form.qualifiedQuantity
    if (current > 0) {
      this.setData({ 'form.qualifiedQuantity': current - 1 })
      this.updateUnqualified()
    }
  },

  updateUnqualified() {
    const { inspectedQuantity, qualifiedQuantity } = this.data.form
    this.setData({ 'form.unqualifiedQuantity': inspectedQuantity - qualifiedQuantity })
  },

  // 选择原因
  selectReason(e) {
    const reason = e.currentTarget.dataset.reason
    this.setData({ 'form.reason': reason })
  },

  onReasonDetailInput(e) {
    this.setData({ 'form.reasonDetail': e.detail.value })
  },

  // 选择图片
  chooseImage() {
    wx.chooseMedia({
      count: 4 - this.data.images.length,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const newImages = res.tempFiles.map(f => f.tempFilePath)
        this.setData({
          images: [...this.data.images, ...newImages]
        })
      }
    })
  },

  // 删除图片
  deleteImage(e) {
    const index = e.currentTarget.dataset.index
    const images = [...this.data.images]
    images.splice(index, 1)
    this.setData({ images })
  },

  // 提交质检
  async submitInspection() {
    const { form, images } = this.data

    // 校验
    if (form.inspectedQuantity <= 0) {
      showToast('请输入检验数量')
      return
    }

    if (form.unqualifiedQuantity > 0 && !form.reason) {
      showToast('请选择不合格原因')
      return
    }

    // 确认提交
    const result = form.unqualifiedQuantity === 0 ? '合格' : '不合格'
    const confirmed = await showModal({
      title: '确认提交',
      content: `质检结果: ${result}\n检验数量: ${form.inspectedQuantity}\n合格数量: ${form.qualifiedQuantity}`
    })

    if (!confirmed) return

    this.setData({ submitting: true })
    showLoading('提交中...')

    try {
      const submitData = {
        ...form,
        reason: form.unqualifiedQuantity > 0 ? `${form.reason}${form.reasonDetail ? ': ' + form.reasonDetail : ''}` : '',
        result: form.unqualifiedQuantity === 0 ? 'PASSED' : 'FAILED'
      }

      const res = await post('/quality-inspections', submitData)
      hideLoading()

      if (res.code === 200) {
        // 上传图片（如果有）
        if (images.length > 0 && res.data?.id) {
          for (const imagePath of images) {
            try {
              await uploadFile(`/quality-inspections/${res.data.id}/images`, imagePath)
            } catch (e) {
              console.error('图片上传失败:', e)
            }
          }
        }

        showSuccess('提交成功')
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
