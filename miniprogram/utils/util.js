/**
 * 格式化日期
 * @param {Date|string|number} date 日期
 * @param {string} format 格式
 * @returns {string}
 */
function formatDate(date, format = 'YYYY-MM-DD') {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 显示加载中
 * @param {string} title 提示文字
 */
function showLoading(title = '加载中...') {
  wx.showLoading({
    title,
    mask: true
  })
}

/**
 * 隐藏加载中
 */
function hideLoading() {
  wx.hideLoading()
}

/**
 * 显示消息提示
 * @param {string} title 提示内容
 * @param {string} icon 图标类型
 */
function showToast(title, icon = 'none') {
  wx.showToast({
    title,
    icon,
    duration: 2000
  })
}

/**
 * 显示成功提示
 * @param {string} title 提示内容
 */
function showSuccess(title) {
  showToast(title, 'success')
}

/**
 * 显示错误提示
 * @param {string} title 提示内容
 */
function showError(title) {
  showToast(title, 'error')
}

/**
 * 显示模态框
 * @param {Object} options 配置
 * @returns {Promise}
 */
function showModal(options) {
  return new Promise((resolve, reject) => {
    wx.showModal({
      title: options.title || '提示',
      content: options.content || '',
      showCancel: options.showCancel !== false,
      cancelText: options.cancelText || '取消',
      confirmText: options.confirmText || '确定',
      success: (res) => {
        if (res.confirm) {
          resolve(true)
        } else {
          resolve(false)
        }
      },
      fail: reject
    })
  })
}

/**
 * 解析二维码内容
 * @param {string} content 二维码内容
 * @returns {Object|null}
 */
function parseQRCode(content) {
  // 格式: WO:{workOrderId}:P:{processId}
  const match = content.match(/WO:(\d+):P:(\d+)/)
  if (match) {
    return {
      workOrderId: parseInt(match[1]),
      processId: parseInt(match[2])
    }
  }
  return null
}

/**
 * 获取状态标签
 * @param {string} status 状态码
 * @returns {Object}
 */
function getStatusInfo(status) {
  const statusMap = {
    PENDING: { label: '待开工', type: 'info' },
    IN_PROGRESS: { label: '进行中', type: 'primary' },
    COMPLETED: { label: '已完成', type: 'success' },
    PAUSED: { label: '暂停', type: 'warning' }
  }
  return statusMap[status] || { label: status, type: 'info' }
}

/**
 * 防抖函数
 * @param {Function} fn 要执行的函数
 * @param {number} delay 延迟时间
 * @returns {Function}
 */
function debounce(fn, delay = 300) {
  let timer = null
  return function(...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 * @param {Function} fn 要执行的函数
 * @param {number} interval 间隔时间
 * @returns {Function}
 */
function throttle(fn, interval = 300) {
  let lastTime = 0
  return function(...args) {
    const now = Date.now()
    if (now - lastTime >= interval) {
      lastTime = now
      fn.apply(this, args)
    }
  }
}

module.exports = {
  formatDate,
  showLoading,
  hideLoading,
  showToast,
  showSuccess,
  showError,
  showModal,
  parseQRCode,
  getStatusInfo,
  debounce,
  throttle
}
