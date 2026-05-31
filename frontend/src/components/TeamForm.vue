<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const emit = defineEmits(['created'])

const loading = ref(false)
const form = reactive({
  name: '',
  description: '',
  goalTitle: '',
  goalDescription: '',
  startDate: '',
  endDate: '',
})

const resetForm = () => {
  form.name = ''
  form.description = ''
  form.goalTitle = ''
  form.goalDescription = ''
  form.startDate = ''
  form.endDate = ''
}

const createTeam = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入小组名称')
    return
  }

  loading.value = true
  try {
    await api.post('/api/teams', {
      name: form.name.trim(),
      description: form.description,
      goalTitle: form.goalTitle,
      goalDescription: form.goalDescription,
      startDate: form.startDate || null,
      endDate: form.endDate || null,
    })

    ElMessage.success('小组创建成功')
    resetForm()
    emit('created')
  } catch (error) {
    ElMessage.error(error.message || '创建小组失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-card class="team-action-card lift-card" shadow="never">
    <template #header>
      <div class="card-header">
        <h2>创建小组</h2>
      </div>
    </template>

    <el-form label-position="top" @submit.prevent>
      <el-form-item label="小组名称">
        <el-input v-model="form.name" placeholder="例如：四级打卡小组" />
      </el-form-item>

      <el-form-item label="小组描述">
        <el-input v-model="form.description" :rows="2" placeholder="补充小组说明" type="textarea" />
      </el-form-item>

      <el-divider content-position="left">小组目标</el-divider>

      <el-form-item label="目标标题">
        <el-input v-model="form.goalTitle" placeholder="为空时默认使用小组名称" />
      </el-form-item>

      <el-form-item label="目标描述">
        <el-input
          v-model="form.goalDescription"
          :rows="2"
          placeholder="为空时默认使用小组描述"
          type="textarea"
        />
      </el-form-item>

      <div class="form-grid">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="默认今天"
          />
        </el-form-item>

        <el-form-item label="结束日期">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="可选"
          />
        </el-form-item>
      </div>

      <el-button type="primary" :loading="loading" @click="createTeam">
        创建小组
      </el-button>
    </el-form>
  </el-card>
</template>
