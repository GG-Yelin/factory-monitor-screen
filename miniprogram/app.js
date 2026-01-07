App({
  globalData: {
    userInfo: null,
    token: null,
    baseUrl: 'http://localhost:8080/api'  // 开发环境，生产环境需更换
  },

  onLaunch() {
    // 检查登录状态
    const token = wx.getStorageSync('token')
    const userInfo = wx.getStorageSync('userInfo')

    if (token && userInfo) {
      this.globalData.token = token
      this.globalData.userInfo = userInfo
    }
  },

  // 检查是否登录
  checkLogin() {
    return !!this.globalData.token
  },

  // 设置登录信息
  setLoginInfo(token, userInfo) {
    this.globalData.token = token
    this.globalData.userInfo = userInfo
    wx.setStorageSync('token', token)
    wx.setStorageSync('userInfo', userInfo)
  },

  // 清除登录信息
  clearLoginInfo() {
    this.globalData.token = null
    this.globalData.userInfo = null
    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
  },

  // 跳转到登录页
  goToLogin() {
    wx.reLaunch({
      url: '/pages/login/login'
    })
  }
})
