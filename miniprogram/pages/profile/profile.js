const app = getApp()
const { showModal, showSuccess } = require('../../utils/util')

Page({
  data: {
    userInfo: {}
  },

  onLoad() {
    if (!app.checkLogin()) {
      app.goToLogin()
    }
  },

  onShow() {
    this.setData({ userInfo: app.globalData.userInfo || {} })
  },

  // 获取角色标签
  getRoleLabel(role) {
    const roleMap = {
      ADMIN: '管理员',
      MANAGER: '经理',
      LEADER: '班组长',
      OPERATOR: '操作工',
      INSPECTOR: '质检员'
    }
    return roleMap[role] || role
  },

  // 跳转到我的工资
  goToWage() {
    wx.navigateTo({
      url: '/pages/profile/wage/wage'
    })
  },

  // 跳转到操作指南
  goToGuide() {
    wx.showModal({
      title: '操作指南',
      content: '1. 扫码报工：扫描工序二维码进行报工\n2. 我的任务：查看分配给您的任务\n3. 我的工资：查看工资明细\n4. 质检员：扫描工单进行质检',
      showCancel: false,
      confirmText: '我知道了'
    })
  },

  // 跳转到设置
  goToSettings() {
    wx.navigateTo({
      url: '/pages/profile/settings/settings'
    })
  },

  // 显示关于
  showAbout() {
    wx.showModal({
      title: 'MES生产执行系统',
      content: '版本: 1.0.0\n\n本系统用于工厂生产执行管理，包括订单管理、派工排产、扫码报工、质量检验、工资核算等功能。\n\n如有问题请联系管理员。',
      showCancel: false,
      confirmText: '确定'
    })
  },

  // 退出登录
  async handleLogout() {
    const confirmed = await showModal({
      title: '提示',
      content: '确定要退出登录吗？'
    })

    if (confirmed) {
      app.clearLoginInfo()
      showSuccess('已退出登录')
      setTimeout(() => {
        app.goToLogin()
      }, 1000)
    }
  }
})
