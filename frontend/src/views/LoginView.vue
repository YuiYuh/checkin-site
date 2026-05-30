<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
})

const login = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const result = await api.post('/api/user/login', {
      username: form.username,
      password: form.password,
    })

    localStorage.setItem('token', result?.token || '')
    localStorage.setItem('user', JSON.stringify(result?.user || { id: 1 }))

    ElMessage.success('登录成功')
    router.push('/goals')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const useDemo = () => {
  localStorage.setItem('user', JSON.stringify({ id: 1, username: 'demo', nickname: '演示用户' }))
  ElMessage.success('已使用默认用户继续演示')
  router.push('/goals')
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-panel">
      <div class="section-heading">
        <p class="eyebrow">Day 7 MVP</p>
        <h1>登录 HabitLink</h1>
        <p>登录后会保存 token 和用户信息；也可以直接使用默认用户体验目标和打卡流程。</p>
      </div>

      <el-form class="login-form" label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            placeholder="请输入密码"
            show-password
            size="large"
            type="password"
            @keyup.enter="login"
          />
        </el-form-item>

        <el-button type="primary" size="large" :loading="loading" @click="login">
          登录
        </el-button>
        <el-button size="large" @click="useDemo">
          使用默认用户
        </el-button>
      </el-form>
    </div>
  </section>
</template>
