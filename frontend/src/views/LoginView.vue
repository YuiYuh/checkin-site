<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import { DEFAULT_TOKEN, DEFAULT_USER, saveAuth } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const enableDemoUser = import.meta.env.VITE_ENABLE_DEMO_USER === 'true'
const form = reactive({
  username: '',
  password: '',
})

const redirectAfterLogin = () => {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/goals'
  router.push(redirect === '/login' ? '/goals' : redirect)
}

const login = async () => {
  if (!form.username.trim() || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const result = await api.post('/api/user/login', {
      username: form.username.trim(),
      password: form.password,
    })

    saveAuth({
      token: result?.token || DEFAULT_TOKEN,
      user: result?.user || { id: 1, username: form.username.trim() },
    })

    ElMessage.success('登录成功')
    redirectAfterLogin()
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const useDemo = () => {
  saveAuth({
    token: DEFAULT_TOKEN,
    user: DEFAULT_USER,
  })
  ElMessage.success('已使用默认用户继续演示')
  router.push('/goals')
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-panel">
      <div class="section-heading">
        <p class="eyebrow">HabitLink MVP</p>
        <h1>登录</h1>
        <p>登录后进入目标和小组演示页面。也可以使用默认用户快速体验课程 MVP。</p>
      </div>

      <el-form class="login-form" label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            autocomplete="username"
          />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            placeholder="请输入密码"
            show-password
            size="large"
            type="password"
            autocomplete="current-password"
            @keyup.enter="login"
          />
        </el-form-item>

        <el-button type="primary" size="large" :loading="loading" @click="login">
          登录
        </el-button>
        <el-button v-if="enableDemoUser" size="large" @click="useDemo">
          使用默认用户
        </el-button>
      </el-form>
    </div>
  </section>
</template>
