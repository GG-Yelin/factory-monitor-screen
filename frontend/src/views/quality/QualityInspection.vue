<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="派工单号">
          <el-input v-model="searchForm.workOrderNo" placeholder="请输入派工单号" clearable />
        </el-form-item>
        <el-form-item label="质检结果">
          <el-select v-model="searchForm.result" placeholder="全部" clearable>
            <el-option label="合格" value="PASSED" />
            <el-option label="不合格" value="FAILED" />
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
          <span>质检记录</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增质检
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="workOrderNo" label="派工单号" width="160" />
        <el-table-column prop="productName" label="产品" width="150" />
        <el-table-column prop="inspectedQuantity" label="检验数量" width="90" />
        <el-table-column prop="qualifiedQuantity" label="合格数量" width="90" />
        <el-table-column prop="unqualifiedQuantity" label="不合格数量" width="100" />
        <el-table-column prop="result" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.result === 'PASSED' ? 'success' : 'danger'" size="small">
              {{ row.result === 'PASSED' ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="inspectorName" label="质检员" width="100" />
        <el-table-column prop="inspectTime" label="质检时间" width="160" />
        <el-table-column prop="reason" label="不合格原因" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增质检对话框 -->
    <el-dialog v-model="dialogVisible" title="新增质检记录" width="600px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="派工单" prop="workOrderId">
          <el-select v-model="formData.workOrderId" placeholder="请选择派工单" filterable @change="onWorkOrderChange">
            <el-option v-for="wo in workOrders" :key="wo.id" :label="`${wo.workOrderNo} - ${wo.productName}`" :value="wo.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="检验数量" prop="inspectedQuantity">
          <el-input-number v-model="formData.inspectedQuantity" :min="1" />
        </el-form-item>
        <el-form-item label="合格数量" prop="qualifiedQuantity">
          <el-input-number v-model="formData.qualifiedQuantity" :min="0" :max="formData.inspectedQuantity" />
        </el-form-item>
        <el-form-item label="不合格原因">
          <el-input v-model="formData.reason" type="textarea" placeholder="如有不合格，请填写原因" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :on-change="handleImageChange"
            :file-list="fileList"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="质检详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="派工单号">{{ currentRecord.workOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="产品">{{ currentRecord.productName }}</el-descriptions-item>
        <el-descriptions-item label="检验数量">{{ currentRecord.inspectedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="合格数量">{{ currentRecord.qualifiedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="不合格数量">{{ currentRecord.unqualifiedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="质检结果">
          <el-tag :type="currentRecord.result === 'PASSED' ? 'success' : 'danger'">
            {{ currentRecord.result === 'PASSED' ? '合格' : '不合格' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="质检员">{{ currentRecord.inspectorName }}</el-descriptions-item>
        <el-descriptions-item label="质检时间">{{ currentRecord.inspectTime }}</el-descriptions-item>
        <el-descriptions-item label="不合格原因" :span="2">{{ currentRecord.reason || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="currentRecord.images?.length" class="image-preview">
        <h4>质检图片</h4>
        <el-image
          v-for="(img, index) in currentRecord.images"
          :key="index"
          :src="img"
          :preview-src-list="currentRecord.images"
          fit="cover"
          style="width: 100px; height: 100px; margin-right: 10px"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const workOrders = ref<any[]>([])
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const currentRecord = ref<any>({})
const fileList = ref<any[]>([])

const searchForm = reactive({ workOrderNo: '', result: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const formData = reactive({
  workOrderId: null as number | null,
  inspectedQuantity: 0,
  qualifiedQuantity: 0,
  reason: ''
})

const formRules: FormRules = {
  workOrderId: [{ required: true, message: '请选择派工单', trigger: 'change' }],
  inspectedQuantity: [{ required: true, message: '请输入检验数量', trigger: 'blur' }],
  qualifiedQuantity: [{ required: true, message: '请输入合格数量', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/quality-inspections', {
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

async function loadWorkOrders() {
  try {
    const res = await request.get('/work-orders', { params: { status: 'IN_PROGRESS', size: 100 } })
    workOrders.value = res.data.content || []
  } catch {}
}

function handleSearch() { pagination.page = 1; loadData() }
function resetSearch() { searchForm.workOrderNo = ''; searchForm.result = ''; handleSearch() }

function handleAdd() {
  Object.assign(formData, { workOrderId: null, inspectedQuantity: 0, qualifiedQuantity: 0, reason: '' })
  fileList.value = []
  dialogVisible.value = true
}

function onWorkOrderChange(id: number) {
  const wo = workOrders.value.find(w => w.id === id)
  if (wo) {
    formData.inspectedQuantity = wo.planQuantity - wo.completedQuantity
    formData.qualifiedQuantity = formData.inspectedQuantity
  }
}

function handleImageChange(file: any) {
  fileList.value.push(file)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await request.post('/quality-inspections', {
      ...formData,
      unqualifiedQuantity: formData.inspectedQuantity - formData.qualifiedQuantity
    })
    ElMessage.success('提交成功')
    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  }
}

function viewDetail(row: any) {
  currentRecord.value = row
  detailDialogVisible.value = true
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm('确定要删除这条质检记录吗？', '提示')
    await request.delete(`/quality-inspections/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

onMounted(() => { loadData(); loadWorkOrders() })
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
.image-preview { margin-top: 20px; }
.image-preview h4 { margin-bottom: 10px; color: #303133; }
</style>
