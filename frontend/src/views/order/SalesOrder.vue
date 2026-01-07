<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="订单号/产品名称/客户" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待排产" value="PENDING" />
            <el-option label="已排产" value="SCHEDULED" />
            <el-option label="生产中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>销售订单</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增订单
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="productName" label="产品名称" width="180" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="completedQuantity" label="已完成" width="80" />
        <el-table-column prop="deliveryDate" label="交货日期" width="110" />
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ getPriorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" v-if="row.status === 'DRAFT'">编辑</el-button>
            <el-button type="success" link @click="handleSchedule(row)" v-if="['DRAFT', 'PENDING'].includes(row.status)">排产</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="row.status === 'DRAFT'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑订单' : '新增订单'" width="600px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="产品" prop="productId">
          <el-select v-model="formData.productId" placeholder="请选择产品" filterable>
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="formData.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="交货日期" prop="deliveryDate">
          <el-date-picker v-model="formData.deliveryDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="formData.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="formData.priority">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
            <el-option label="紧急" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button @click="handleSaveDraft">暂存</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 排产对话框 -->
    <el-dialog v-model="scheduleDialogVisible" title="订单排产" width="500px">
      <el-form :model="scheduleForm" label-width="100px">
        <el-form-item label="订单号">
          <span>{{ scheduleForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="产品">
          <span>{{ scheduleForm.productName }}</span>
        </el-form-item>
        <el-form-item label="数量">
          <span>{{ scheduleForm.quantity }}</span>
        </el-form-item>
        <el-form-item label="分配班组">
          <el-select v-model="scheduleForm.teamId" placeholder="请选择班组">
            <el-option v-for="t in teams" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划开始">
          <el-date-picker v-model="scheduleForm.planStartDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="计划结束">
          <el-date-picker v-model="scheduleForm.planEndDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateWorkOrder">生成派工单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const products = ref([])
const teams = ref([])
const dialogVisible = ref(false)
const scheduleDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const searchForm = reactive({ keyword: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const formData = reactive({
  id: 0,
  productId: null as number | null,
  quantity: 1,
  deliveryDate: '',
  customerName: '',
  priority: 2,
  remark: '',
  status: 'DRAFT'
})

const scheduleForm = reactive({
  salesOrderId: 0,
  orderNo: '',
  productName: '',
  quantity: 0,
  teamId: null as number | null,
  planStartDate: '',
  planEndDate: ''
})

const formRules: FormRules = {
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  deliveryDate: [{ required: true, message: '请选择交货日期', trigger: 'change' }]
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: '草稿', PENDING: '待排产', SCHEDULED: '已排产',
    IN_PROGRESS: '生产中', COMPLETED: '已完成', CANCELLED: '已取消'
  }
  return map[status] || status
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: 'info', PENDING: 'warning', SCHEDULED: 'primary',
    IN_PROGRESS: '', COMPLETED: 'success', CANCELLED: 'danger'
  }
  return map[status] || ''
}

const getPriorityLabel = (p: number) => ['', '低', '中', '高', '紧急'][p] || ''
const getPriorityType = (p: number) => ['', 'info', '', 'warning', 'danger'][p] || ''

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/sales-orders', {
      params: { ...searchForm, page: pagination.page - 1, size: pagination.size }
    })
    tableData.value = res.data.content
    pagination.total = res.data.totalElements
  } finally {
    loading.value = false
  }
}

async function loadProducts() {
  const res = await request.get('/products/all')
  products.value = res.data
}

async function loadTeams() {
  const res = await request.get('/teams/active')
  teams.value = res.data
}

function handleSearch() { pagination.page = 1; loadData() }
function resetSearch() { searchForm.keyword = ''; searchForm.status = ''; handleSearch() }

function handleAdd() {
  isEdit.value = false
  Object.assign(formData, { id: 0, productId: null, quantity: 1, deliveryDate: '', customerName: '', priority: 2, remark: '', status: 'DRAFT' })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleSaveDraft() {
  formData.status = 'DRAFT'
  await doSubmit()
}

async function handleSubmit() {
  formData.status = 'PENDING'
  await doSubmit()
}

async function doSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await request.put(`/sales-orders/${formData.id}`, formData)
    } else {
      await request.post('/sales-orders', formData)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

function handleSchedule(row: any) {
  Object.assign(scheduleForm, {
    salesOrderId: row.id,
    orderNo: row.orderNo,
    productName: row.productName,
    quantity: row.quantity,
    teamId: null,
    planStartDate: '',
    planEndDate: row.deliveryDate
  })
  scheduleDialogVisible.value = true
}

async function handleCreateWorkOrder() {
  try {
    await request.post('/work-orders', scheduleForm)
    ElMessage.success('派工单创建成功')
    scheduleDialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '创建失败')
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除订单 ${row.orderNo} 吗？`, '提示')
    await request.delete(`/sales-orders/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

onMounted(() => { loadData(); loadProducts(); loadTeams() })
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.search-card :deep(.el-card__body) { padding-bottom: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
