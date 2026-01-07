<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" @submit.prevent="handleSearch">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="姓名/工号/手机号" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部" clearable>
            <el-option label="管理员" value="ADMIN" />
            <el-option label="厂长/主管" value="MANAGER" />
            <el-option label="班组长" value="LEADER" />
            <el-option label="操作工" value="OPERATOR" />
            <el-option label="质检员" value="INSPECTOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工具栏 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="employeeNo" label="工号" width="100" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">{{ getRoleName(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleResetPassword(row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="工号" prop="employeeNo">
          <el-input v-model="formData.employeeNo" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" placeholder="请选择角色">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="厂长/主管" value="MANAGER" />
            <el-option label="班组长" value="LEADER" />
            <el-option label="操作工" value="OPERATOR" />
            <el-option label="质检员" value="INSPECTOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'

interface User {
  id: number
  username: string
  employeeNo: string
  realName: string
  phone: string
  role: string
  status: number
  lastLoginTime: string
}

const loading = ref(false)
const tableData = ref<User[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  keyword: '',
  role: '',
  status: undefined as number | undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: 0,
  username: '',
  employeeNo: '',
  realName: '',
  phone: '',
  password: '',
  role: 'OPERATOR',
  status: 1
})

const formRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur', min: 6 }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getRoleName = (role: string) => {
  const map: Record<string, string> = {
    ADMIN: '管理员',
    MANAGER: '厂长/主管',
    LEADER: '班组长',
    OPERATOR: '操作工',
    INSPECTOR: '质检员'
  }
  return map[role] || role
}

const getRoleTagType = (role: string) => {
  const map: Record<string, string> = {
    ADMIN: 'danger',
    MANAGER: 'warning',
    LEADER: 'primary',
    OPERATOR: 'success',
    INSPECTOR: 'info'
  }
  return map[role] || ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/users', {
      params: {
        ...searchForm,
        page: pagination.page - 1,
        size: pagination.size
      }
    })
    tableData.value = res.data.content
    pagination.total = res.data.totalElements
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.role = ''
  searchForm.status = undefined
  handleSearch()
}

function handleAdd() {
  isEdit.value = false
  Object.assign(formData, {
    id: 0,
    username: '',
    employeeNo: '',
    realName: '',
    phone: '',
    password: '',
    role: 'OPERATOR',
    status: 1
  })
  dialogVisible.value = true
}

function handleEdit(row: User) {
  isEdit.value = true
  Object.assign(formData, {
    ...row,
    password: ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/users/${formData.id}`, formData)
      ElMessage.success('更新成功')
    } else {
      await request.post('/users', formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleResetPassword(row: User) {
  try {
    await ElMessageBox.confirm(`确定要重置用户 ${row.realName} 的密码吗？密码将重置为 123456`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.put(`/users/${row.id}/reset-password`, { password: '123456' })
    ElMessage.success('密码重置成功')
  } catch (error) {
    // 用户取消
  }
}

async function handleDelete(row: User) {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${row.realName} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/users/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.page-container {
  .search-card {
    margin-bottom: 16px;

    :deep(.el-card__body) {
      padding-bottom: 0;
    }
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
