<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>工序模板库</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增模板
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="code" label="模板编码" width="120" />
        <el-table-column prop="name" label="模板名称" width="200" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEditSteps(row)">配置工序</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 模板编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模板' : '新增模板'" width="500px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="编码">
          <el-input v-model="formData.code" placeholder="请输入模板编码" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" />
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

    <!-- 工序配置对话框 -->
    <el-dialog v-model="stepsDialogVisible" title="配置工序" width="800px">
      <div class="steps-header">
        <el-button type="primary" size="small" @click="addStep">
          <el-icon><Plus /></el-icon>添加工序
        </el-button>
      </div>
      <el-table :data="steps" border>
        <el-table-column prop="sortOrder" label="序号" width="60" />
        <el-table-column prop="name" label="工序名称" width="150">
          <template #default="{ row }">
            <el-input v-model="row.name" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="unitPrice" label="计件单价" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.unitPrice" :precision="2" :min="0" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column prop="standardTime" label="标准工时(分)" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.standardTime" :min="0" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column prop="needInspection" label="需要质检" width="90">
          <template #default="{ row }">
            <el-switch v-model="row.needInspection" :active-value="1" :inactive-value="0" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button type="danger" link @click="removeStep($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="stepsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSteps">保存工序</el-button>
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
const dialogVisible = ref(false)
const stepsDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const currentTemplateId = ref<number>(0)
const steps = ref<any[]>([])

const formData = reactive({
  id: 0, code: '', name: '', description: '', status: 1
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/process-templates')
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  Object.assign(formData, { id: 0, code: '', name: '', description: '', status: 1 })
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
      await request.put(`/process-templates/${formData.id}`, formData)
    } else {
      await request.post('/process-templates', formData)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除模板 ${row.name} 吗？`, '提示')
    await request.delete(`/process-templates/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

async function handleEditSteps(row: any) {
  currentTemplateId.value = row.id
  const res = await request.get(`/process-templates/${row.id}/steps`)
  steps.value = res.data
  stepsDialogVisible.value = true
}

function addStep() {
  steps.value.push({
    sortOrder: steps.value.length + 1,
    name: '',
    unitPrice: 0,
    standardTime: 0,
    needInspection: 0,
    status: 1
  })
}

function removeStep(index: number) {
  steps.value.splice(index, 1)
  steps.value.forEach((s, i) => s.sortOrder = i + 1)
}

async function saveSteps() {
  try {
    await request.put(`/process-templates/${currentTemplateId.value}/steps/batch`, steps.value)
    ElMessage.success('保存成功')
    stepsDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.steps-header { margin-bottom: 16px; }
</style>
