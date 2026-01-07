<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="物料名称/编码" clearable />
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
          <span>物料管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增物料
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="code" label="物料编码" width="120" />
        <el-table-column prop="name" label="物料名称" width="180" />
        <el-table-column prop="specification" label="规格型号" width="150" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="stockQuantity" label="库存数量" width="100">
          <template #default="{ row }">
            <span :class="{ 'low-stock': row.stockQuantity <= row.warningThreshold }">
              {{ row.stockQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="warningThreshold" label="预警阈值" width="90" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleStockIn(row)">入库</el-button>
            <el-button type="warning" link @click="handleStockOut(row)">出库</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 低库存预警 -->
    <el-card v-if="lowStockMaterials.length > 0" class="warning-card">
      <template #header>
        <span style="color: #e6a23c"><el-icon><Warning /></el-icon> 库存预警</span>
      </template>
      <el-tag v-for="m in lowStockMaterials" :key="m.id" type="warning" style="margin: 4px">
        {{ m.name }} ({{ m.stockQuantity }}/{{ m.warningThreshold }})
      </el-tag>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑物料' : '新增物料'" width="500px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="物料编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入物料编码" />
        </el-form-item>
        <el-form-item label="物料名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入物料名称" />
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="formData.specification" placeholder="请输入规格型号" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="formData.unit" placeholder="如：个、kg、米" style="width: 120px" />
        </el-form-item>
        <el-form-item label="初始库存" v-if="!isEdit">
          <el-input-number v-model="formData.stockQuantity" :min="0" />
        </el-form-item>
        <el-form-item label="预警阈值">
          <el-input-number v-model="formData.warningThreshold" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 入库/出库对话框 -->
    <el-dialog v-model="stockDialogVisible" :title="stockType === 'in' ? '物料入库' : '物料出库'" width="400px">
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="物料">
          <span>{{ stockForm.materialName }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <span>{{ stockForm.currentStock }}</span>
        </el-form-item>
        <el-form-item :label="stockType === 'in' ? '入库数量' : '出库数量'">
          <el-input-number v-model="stockForm.quantity" :min="1" :max="stockType === 'out' ? stockForm.currentStock : undefined" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStockSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const stockDialogVisible = ref(false)
const isEdit = ref(false)
const stockType = ref<'in' | 'out'>('in')
const formRef = ref<FormInstance>()

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const formData = reactive({
  id: 0, code: '', name: '', specification: '', unit: '', stockQuantity: 0, warningThreshold: 10, status: 1
})

const stockForm = reactive({
  materialId: 0, materialName: '', currentStock: 0, quantity: 1, remark: ''
})

const formRules: FormRules = {
  code: [{ required: true, message: '请输入物料编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入物料名称', trigger: 'blur' }]
}

const lowStockMaterials = computed(() => tableData.value.filter(m => m.stockQuantity <= m.warningThreshold))

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/materials', {
      params: { ...searchForm, page: pagination.page - 1, size: pagination.size }
    })
    tableData.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.page = 1; loadData() }
function resetSearch() { searchForm.keyword = ''; handleSearch() }

function handleAdd() {
  isEdit.value = false
  Object.assign(formData, { id: 0, code: '', name: '', specification: '', unit: '', stockQuantity: 0, warningThreshold: 10, status: 1 })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await request.put(`/materials/${formData.id}`, formData)
    } else {
      await request.post('/materials', formData)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

function handleStockIn(row: any) {
  stockType.value = 'in'
  Object.assign(stockForm, { materialId: row.id, materialName: row.name, currentStock: row.stockQuantity, quantity: 1, remark: '' })
  stockDialogVisible.value = true
}

function handleStockOut(row: any) {
  stockType.value = 'out'
  Object.assign(stockForm, { materialId: row.id, materialName: row.name, currentStock: row.stockQuantity, quantity: 1, remark: '' })
  stockDialogVisible.value = true
}

async function handleStockSubmit() {
  try {
    const endpoint = stockType.value === 'in' ? '/materials/stock-in' : '/materials/stock-out'
    await request.post(endpoint, stockForm)
    ElMessage.success(stockType.value === 'in' ? '入库成功' : '出库成功')
    stockDialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除物料 ${row.name} 吗？`, '提示')
    await request.delete(`/materials/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

onMounted(() => loadData())
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
.warning-card { margin-top: 16px; }
.low-stock { color: #e6a23c; font-weight: 600; }
</style>
