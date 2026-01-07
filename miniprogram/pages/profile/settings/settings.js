const app = getApp()
const { post } = require('../../../utils/request')
const { showLoading, hideLoading, showToast, showSuccess } = require('../../../utils/util')

Page({
  data: {
    userInfo: {},
    passwordForm: {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    },
    submitting: false
  },

  onLoad() {
    if (!app.checkLogin()) {
      app.goToLogin()
      return
    }

    this.setData({ userInfo: app.globalData.userInfo || {} })
  },

  onOldPasswordInput(e) {
    this.setData({ 'passwordForm.oldPassword': e.detail.value })
  },

  onNewPasswordInput(e) {
    this.setData({ 'passwordForm.newPassword': e.detail.value })
  },

  onConfirmPasswordInput(e) {
    this.setData({ 'passwordForm.confirmPassword': e.detail.value })
  },

  async changePassword() {
    const { oldPassword, newPassword, confirmPassword } = this.data.passwordForm

    // 校验
    if (!oldPassword) {
      showToast('请输入当前密码')
      return
    }

    if (!newPassword) {
      showToast('请输入新密码')
      return
    }

    if (newPassword.length < 6) {
      showToast('新密码至少6位')
      return
    }

    if (newPassword !== confirmPassword) {
      showToast('两次输入的密码不一致')
      return
    }

    this.setData({ submitting: true })
    showLoading('提交中...')

    try {
      const res = await post('/auth/change-password', {
        oldPassword,
        newPassword
      })

      hideLoading()

      if (res.code === 200) {
        showSuccess('密码修改成功')
        this.setData({
          passwordForm: {
            oldPassword: '',
            newPassword: '',
            confirmPassword: ''
          }
        })
      } else {
        showToast(res.message || '修改失败')
      }
    } catch (error) {
      hideLoading()
      showToast(error.message || '修改失败')
    } finally {
      this.setData({ submitting: false })
    }
  }
})
