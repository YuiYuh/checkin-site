<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import { saveAuth } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const enableDemoUser = import.meta.env.VITE_ENABLE_DEMO_USER === 'true'
const form = reactive({
  username: '',
  password: '',
})

const redirectAfterLogin = () => {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
  router.push(redirect === '/login' ? '/' : redirect)
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

    if (!result?.token || !result?.user) {
      ElMessage.error('登录响应缺少 token 或用户信息')
      return
    }

    saveAuth({
      token: result.token,
      user: result.user,
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
  ElMessage.warning('签名登录已启用，请使用测试账号密码登录')
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-panel">
      <div class="section-heading">
        <p class="eyebrow">HabitLink</p>
        <h1>登录</h1>
        <p>登录后进入学习打卡看板，管理目标和小组协作。</p>
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
