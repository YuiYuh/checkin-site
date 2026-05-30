<template>
  <el-config-provider>
    <div class="app-shell">
      <header class="app-header">
        <router-link class="brand" to="/">
          <span class="brand-mark">H</span>
          <span>HabitLink</span>
        </router-link>

        <nav class="app-nav">
          <router-link to="/">首页</router-link>
          <router-link to="/goals">目标</router-link>
          <router-link to="/login">登录</router-link>
        </nav>
      </header>

      <main class="app-main">
        <router-view />
      </main>
    </div>
  </el-config-provider>
</template>
