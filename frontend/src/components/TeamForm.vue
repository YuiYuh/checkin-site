<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const emit = defineEmits(['created'])

const loading = ref(false)
const form = reactive({
  name: '',
  description: '',
})

const resetForm = () => {
  form.name = ''
  form.description = ''
}

const createTeam = async () => {
  if (!form.name) {
    ElMessage.warning('请输入小组名称')
    return
  }

  loading.value = true
  try {
    await api.post('/api/teams', {
      name: form.name,
      description: form.description,
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
  <el-card class="team-action-card" shadow="never">
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
        <el-input
          v-model="form.description"
          :rows="3"
          placeholder="补充小组说明"
          type="textarea"
        />
      </el-form-item>

      <el-button type="primary" :loading="loading" @click="createTeam">
        创建小组
      </el-button>
    </el-form>
  </el-card>
</template>
