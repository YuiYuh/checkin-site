<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const emit = defineEmits(['joined'])

const loading = ref(false)
const form = reactive({
  inviteCode: '',
})

const joinTeam = async () => {
  if (!form.inviteCode.trim()) {
    ElMessage.warning('请输入邀请码')
    return
  }

  loading.value = true
  try {
    await api.post('/api/teams/join', {
      inviteCode: form.inviteCode.trim(),
    })

    ElMessage.success('加入小组成功')
    form.inviteCode = ''
    emit('joined')
  } catch (error) {
    ElMessage.error(error.message || '加入小组失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-card class="team-action-card compact-panel" shadow="never">
    <el-form class="join-team-form" label-position="top" @submit.prevent>
      <el-form-item label="邀请码">
        <el-input
          v-model="form.inviteCode"
          placeholder="输入小组邀请码"
          @keyup.enter="joinTeam"
        />
      </el-form-item>

      <el-button type="primary" :loading="loading" @click="joinTeam">
        加入小组
      </el-button>
    </el-form>
  </el-card>
</template>
