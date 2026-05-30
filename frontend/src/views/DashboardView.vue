<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const currentUser = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('user')) || { id: 1 }
  } catch {
    return { id: 1 }
  }
})
</script>

<template>
  <section class="dashboard-page">
    <div class="dashboard-copy">
      <p class="eyebrow">HabitLink MVP</p>
      <h1>目标打卡课程演示</h1>
      <p>
        当前前端聚焦目标列表、创建目标、今日打卡和统计展示。未登录时仍可使用默认
        userId=1 的后端 MVP 数据继续演示。
      </p>

      <div class="dashboard-actions">
        <el-button type="primary" size="large" @click="router.push('/goals')">
          查看目标
        </el-button>
        <el-button size="large" @click="router.push('/login')">
          登录
        </el-button>
      </div>
    </div>

    <div class="status-panel">
      <span class="status-label">当前用户</span>
      <strong>{{ currentUser.nickname || currentUser.username || '默认演示用户' }}</strong>
      <span>User ID: {{ currentUser.id || 1 }}</span>
    </div>
  </section>
</template>
