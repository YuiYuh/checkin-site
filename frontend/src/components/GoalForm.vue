<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const emit = defineEmits(['created'])

const loading = ref(false)
const form = reactive({
  title: '',
  description: '',
  startDate: '',
  endDate: '',
})

const resetForm = () => {
  form.title = ''
  form.description = ''
  form.startDate = ''
  form.endDate = ''
}

const createGoal = async () => {
  if (!form.title || !form.startDate || !form.endDate) {
    ElMessage.warning('请填写目标标题、开始日期和结束日期')
    return
  }

  loading.value = true
  try {
    await api.post('/api/goals', {
      title: form.title,
      description: form.description,
      startDate: form.startDate,
      endDate: form.endDate,
    })

    ElMessage.success('目标创建成功')
    resetForm()
    emit('created')
  } catch (error) {
    ElMessage.error(error.message || '创建目标失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-card class="goal-form-card lift-card" shadow="never">
    <template #header>
      <div class="card-header">
        <h2>创建目标</h2>
      </div>
    </template>

    <el-form label-position="top" @submit.prevent>
      <el-form-item label="目标标题">
        <el-input v-model="form.title" placeholder="例如：每天背 20 个单词" />
      </el-form-item>

      <el-form-item label="目标描述">
        <el-input
          v-model="form.description"
          :rows="3"
          placeholder="补充目标说明"
          type="textarea"
        />
      </el-form-item>

      <div class="form-grid">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择开始日期"
          />
        </el-form-item>

        <el-form-item label="结束日期">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择结束日期"
          />
        </el-form-item>
      </div>

      <el-button type="primary" :loading="loading" @click="createGoal">
        创建目标
      </el-button>
    </el-form>
  </el-card>
</template>
