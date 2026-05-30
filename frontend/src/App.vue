<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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

const showHeader = computed(() => {
  return loggedIn.value && route.name !== 'login'
})

const logout = () => {
  clearAuth()
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
          <span>HabitLink</span>
        </router-link>

        <nav class="app-nav">
          <router-link to="/">首页</router-link>
          <router-link to="/goals">目标</router-link>
          <router-link to="/teams">小组</router-link>
          <el-button size="small" @click="logout">退出登录</el-button>
        </nav>
      </header>

      <main class="app-main">
        <router-view />
      </main>
    </div>
  </el-config-provider>
</template>
