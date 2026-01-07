import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '生产看板', icon: 'Monitor' }
      },
      // 系统管理
      {
        path: 'system/users',
        name: 'UserManage',
        component: () => import('@/views/system/UserManage.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'system/teams',
        name: 'TeamManage',
        component: () => import('@/views/system/TeamManage.vue'),
        meta: { title: '班组管理', icon: 'UserFilled' }
      },
      // 基础数据
      {
        path: 'basedata/products',
        name: 'ProductManage',
        component: () => import('@/views/basedata/ProductManage.vue'),
        meta: { title: '产品管理', icon: 'Goods' }
      },
      {
        path: 'basedata/templates',
        name: 'ProcessTemplate',
        component: () => import('@/views/basedata/ProcessTemplate.vue'),
        meta: { title: '工序模板', icon: 'Document' }
      },
      // 订单管理
      {
        path: 'order/sales',
        name: 'SalesOrder',
        component: () => import('@/views/order/SalesOrder.vue'),
        meta: { title: '销售订单', icon: 'List' }
      },
      // 生产管理
      {
        path: 'production/work-orders',
        name: 'WorkOrderList',
        component: () => import('@/views/production/WorkOrderList.vue'),
        meta: { title: '派工单', icon: 'Tickets' }
      },
      {
        path: 'production/progress',
        name: 'ProgressBoard',
        component: () => import('@/views/production/ProgressBoard.vue'),
        meta: { title: '进度看板', icon: 'TrendCharts' }
      },
      // 质量管理
      {
        path: 'quality/inspection',
        name: 'QualityInspection',
        component: () => import('@/views/quality/QualityInspection.vue'),
        meta: { title: '质量检验', icon: 'Checked' }
      },
      // 库存管理
      {
        path: 'inventory/materials',
        name: 'MaterialManage',
        component: () => import('@/views/inventory/MaterialManage.vue'),
        meta: { title: '物料库存', icon: 'Box' }
      },
      // 工资管理
      {
        path: 'wage/calculation',
        name: 'WageCalculation',
        component: () => import('@/views/wage/WageCalculation.vue'),
        meta: { title: '工资核算', icon: 'Wallet' }
      },
      // 报表中心
      {
        path: 'report/center',
        name: 'ReportCenter',
        component: () => import('@/views/report/ReportCenter.vue'),
        meta: { title: '报表中心', icon: 'DataAnalysis' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth === false) {
    next()
    return
  }

  if (!userStore.isLoggedIn) {
    next('/login')
    return
  }

  next()
})

export default router
