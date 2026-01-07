const app = getApp()
const { post } = require('../../utils/request')
const { showLoading, hideLoading, showToast, showSuccess } = require('../../utils/util')

Page({
  data: {
    username: '',
    password: '',
    showPassword: false,
    rememberMe: true,
    loading: false
  },

  onLoad() {
    // 读取保存的账号
    const savedUsername = wx.getStorageSync('savedUsername')
    if (savedUsername) {
      this.setData({ username: savedUsername })
    }

    // 如果已登录，跳转到首页
    if (app.checkLogin()) {
      this.navigateToHome()
    }
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  togglePassword() {
    this.setData({ showPassword: !this.data.showPassword })
  },

  onRememberChange(e) {
    this.setData({ rememberMe: e.detail.value })
  },

  async handleLogin() {
    const { username, password, rememberMe } = this.data

    if (!username.trim()) {
      showToast('请输入工号/手机号')
      return
    }

    if (!password.trim()) {
      showToast('请输入密码')
      return
    }

    this.setData({ loading: true })
    showLoading('登录中...')

    try {
      const res = await post('/auth/login', {
        username: username.trim(),
        password: password.trim()
      })

      hideLoading()

      if (res.code === 200 && res.data) {
        // 保存登录信息 (后端返回 accessToken 和 userInfo)
        app.setLoginInfo(res.data.accessToken, res.data.userInfo)

        // 记住账号
        if (rememberMe) {
          wx.setStorageSync('savedUsername', username.trim())
        } else {
          wx.removeStorageSync('savedUsername')
        }

        showSuccess('登录成功')

        // 跳转到首页
        setTimeout(() => {
          this.navigateToHome()
        }, 1000)
      } else {
        showToast(res.message || '登录失败')
      }
    } catch (error) {
      hideLoading()
      showToast(error.message || '登录失败')
    } finally {
      this.setData({ loading: false })
    }
  },

  navigateToHome() {
    const userInfo = app.globalData.userInfo
    // 根据角色跳转不同页面
    if (userInfo && userInfo.role) {
      if (userInfo.role === 'INSPECTOR') {
        wx.reLaunch({ url: '/pages/inspector/inspector' })
      } else {
        wx.reLaunch({ url: '/pages/operator/operator' })
      }
    } else {
      wx.reLaunch({ url: '/pages/operator/operator' })
    }
  }
})
