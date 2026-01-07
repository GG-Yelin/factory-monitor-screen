const app = getApp()

/**
 * 封装的网络请求
 * @param {Object} options 请求配置
 * @returns {Promise}
 */
function request(options) {
  return new Promise((resolve, reject) => {
    const { url, method = 'GET', data = {}, header = {} } = options

    // 添加token到请求头
    const token = app.globalData.token
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }
    header['Content-Type'] = header['Content-Type'] || 'application/json'

    wx.request({
      url: app.globalData.baseUrl + url,
      method,
      data,
      header,
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data)
        } else if (res.statusCode === 401) {
          // 未授权，跳转登录
          app.clearLoginInfo()
          app.goToLogin()
          reject(new Error('登录已过期，请重新登录'))
        } else if (res.statusCode === 403) {
          reject(new Error('没有权限访问'))
        } else {
          reject(new Error(res.data?.message || '请求失败'))
        }
      },
      fail: (err) => {
        console.error('请求失败:', err)
        reject(new Error('网络请求失败'))
      }
    })
  })
}

/**
 * GET请求
 */
function get(url, data = {}) {
  return request({ url, method: 'GET', data })
}

/**
 * POST请求
 */
function post(url, data = {}) {
  return request({ url, method: 'POST', data })
}

/**
 * PUT请求
 */
function put(url, data = {}) {
  return request({ url, method: 'PUT', data })
}

/**
 * DELETE请求
 */
function del(url, data = {}) {
  return request({ url, method: 'DELETE', data })
}

/**
 * 上传文件
 */
function uploadFile(url, filePath, name = 'file', formData = {}) {
  return new Promise((resolve, reject) => {
    const token = app.globalData.token
    const header = {}
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }

    wx.uploadFile({
      url: app.globalData.baseUrl + url,
      filePath,
      name,
      formData,
      header,
      success: (res) => {
        if (res.statusCode === 200) {
          const data = JSON.parse(res.data)
          resolve(data)
        } else {
          reject(new Error('上传失败'))
        }
      },
      fail: (err) => {
        console.error('上传失败:', err)
        reject(new Error('上传失败'))
      }
    })
  })
}

module.exports = {
  request,
  get,
  post,
  put,
  del,
  uploadFile
}
