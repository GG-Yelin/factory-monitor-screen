<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <img src="/favicon.ico" alt="logo" />
        <span v-show="!appStore.sidebarCollapsed">MES系统</span>
      </div>

      <el-menu
        :default-active="$route.path"
        :collapse="appStore.sidebarCollapsed"
        :unique-opened="true"
        background-color="#1a1a2e"
        text-color="#a0a0a0"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <template #title>生产看板</template>
        </el-menu-item>

        <el-sub-menu index="system" v-if="userStore.isManager">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/system/teams">
            <el-icon><UserFilled /></el-icon>
            <span>班组管理</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="basedata">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>基础数据</span>
          </template>
          <el-menu-item index="/basedata/products">
            <el-icon><Goods /></el-icon>
            <span>产品管理</span>
          </el-menu-item>
          <el-menu-item index="/basedata/templates">
            <el-icon><Document /></el-icon>
            <span>工序模板</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="order">
          <template #title>
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </template>
          <el-menu-item index="/order/sales">
            <el-icon><List /></el-icon>
            <span>销售订单</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="production">
          <template #title>
            <el-icon><Tickets /></el-icon>
            <span>生产管理</span>
          </template>
          <el-menu-item index="/production/work-orders">
            <el-icon><Tickets /></el-icon>
            <span>派工单</span>
          </el-menu-item>
          <el-menu-item index="/production/progress">
            <el-icon><TrendCharts /></el-icon>
            <span>进度看板</span>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/quality/inspection">
          <el-icon><Checked /></el-icon>
          <template #title>质量检验</template>
        </el-menu-item>

        <el-menu-item index="/inventory/materials">
          <el-icon><Box /></el-icon>
          <template #title>物料库存</template>
        </el-menu-item>

        <el-menu-item index="/wage/calculation">
          <el-icon><Wallet /></el-icon>
          <template #title>工资核算</template>
        </el-menu-item>

        <el-menu-item index="/report/center">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>报表中心</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="appStore.toggleSidebar">
            <component :is="appStore.sidebarCollapsed ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.realName || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <el-icon><User /></el-icon>
                  {{ userStore.userInfo?.roleName }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { logout } from '@/api/auth'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await logout().catch(() => {})
    userStore.logout()
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #1a1a2e;
  transition: width 0.3s;
  overflow: hidden;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    img {
      width: 32px;
      height: 32px;
    }
  }

  :deep(.el-menu) {
    border-right: none;
  }
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  padding: 0 20px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      transition: transform 0.3s;

      &:hover {
        color: #409eff;
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;

      &:hover {
        background: #f5f7fa;
      }

      .user-name {
        font-size: 14px;
        color: #333;
      }
    }
  }
}

.layout-main {
  background: #f5f7fa;
  padding: 20px;
  overflow: auto;
}
</style>
