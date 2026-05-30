<script setup>
import { onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser, onAuthChange } from '../utils/auth'

const router = useRouter()
const currentUser = ref(getCurrentUser())

const stopAuthListener = onAuthChange(() => {
  currentUser.value = getCurrentUser()
})

onBeforeUnmount(stopAuthListener)
</script>

<template>
  <section class="dashboard-page">
    <div class="dashboard-copy">
      <p class="eyebrow">HabitLink MVP</p>
      <h1>目标打卡课程演示</h1>
      <p>
        当前前端支持登录、目标列表、创建目标、今日打卡、统计展示和小组协作。
        登录后可以在顶部导航切换首页、目标和小组页面。
      </p>

      <div class="dashboard-actions">
        <el-button type="primary" size="large" @click="router.push('/goals')">
          查看目标
        </el-button>
        <el-button size="large" @click="router.push('/teams')">
          查看小组
        </el-button>
      </div>
    </div>

    <div class="status-panel">
      <span class="status-label">当前用户</span>
      <strong>{{ currentUser?.nickname || currentUser?.username || '演示用户' }}</strong>
      <span>User ID: {{ currentUser?.id || 1 }}</span>
    </div>
  </section>
</template>
