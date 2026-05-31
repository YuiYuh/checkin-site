<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { clearAuth, getCurrentUser, isLoggedIn, onAuthChange } from './utils/auth'

const route = useRoute()
const router = useRouter()
const loggedIn = ref(isLoggedIn())
const currentUser = ref(getCurrentUser())

const refreshAuth = () => {
  loggedIn.value = isLoggedIn()
  currentUser.value = getCurrentUser()
}

const stopAuthListener = onAuthChange(refreshAuth)

const showHeader = computed(() => loggedIn.value && route.name !== 'login')

const displayName = computed(() => {
  return currentUser.value?.nickname || currentUser.value?.username || '用户'
})

const avatarText = computed(() => {
  return displayName.value.trim().charAt(0) || '用'
})

const logout = () => {
  clearAuth()
  ElMessage.success('已退出登录')
  router.push('/login')
}

onBeforeUnmount(stopAuthListener)
</script>

<template>
  <el-config-provider>
    <div class="app-shell">
      <header v-if="showHeader" class="app-header">
        <router-link class="brand" to="/">
          <span class="brand-mark">H</span>
          <span class="brand-copy">
            <strong>HabitLink</strong>
            <small>把目标变成每天的行动</small>
          </span>
        </router-link>

        <nav class="app-nav">
          <router-link to="/">首页</router-link>
          <router-link to="/goals">目标</router-link>
          <router-link to="/teams">小组</router-link>
        </nav>

        <el-dropdown trigger="click" placement="bottom-end" class="user-menu">
          <button class="avatar-button" type="button" aria-label="用户菜单">
            <el-avatar :size="38">{{ avatarText }}</el-avatar>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <div class="user-menu-name">{{ displayName }}</div>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="app-main">
        <router-view />
      </main>
    </div>
  </el-config-provider>
</template>
