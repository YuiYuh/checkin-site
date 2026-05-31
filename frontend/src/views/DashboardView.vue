<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGoalStats, getGoals, getTeams, getTodayCheckinStatus } from '../api'

const router = useRouter()

const goals = ref([])
const teams = ref([])
const goalDetails = ref([])
const loading = ref(false)

const loadGoalDetail = async (goal) => {
  try {
    const [stats, checkedToday] = await Promise.all([
      getGoalStats(goal.id),
      getTodayCheckinStatus(goal.id),
    ])

    return {
      goal,
      stats: stats || { totalDays: 0, continuousDays: 0 },
      checkedToday,
    }
  } catch (error) {
    return {
      goal,
      stats: { totalDays: 0, continuousDays: 0 },
      checkedToday: false,
      error,
    }
  }
}

const loadDashboard = async () => {
  loading.value = true
  try {
    const [goalResult, teamResult] = await Promise.all([getGoals(), getTeams()])
    goals.value = Array.isArray(goalResult) ? goalResult : []
    teams.value = Array.isArray(teamResult) ? teamResult : []
    goalDetails.value = await Promise.all(goals.value.map((goal) => loadGoalDetail(goal)))
  } catch (error) {
    ElMessage.error(error.message || '加载首页数据失败')
  } finally {
    loading.value = false
  }
}

const hasTeams = computed(() => teams.value.length > 0)

const visibleGoalDetails = computed(() => {
  if (hasTeams.value) {
    return goalDetails.value
  }
  return goalDetails.value.filter((item) => item.goal.goalType !== 'TEAM')
})

const visibleGoals = computed(() => visibleGoalDetails.value.map((item) => item.goal))

const checkedTodayCount = computed(() => {
  return visibleGoalDetails.value.filter((item) => item.checkedToday).length
})

const uncheckedTodayCount = computed(() => {
  return visibleGoalDetails.value.filter((item) => !item.checkedToday).length
})

const totalCheckinDays = computed(() => {
  return visibleGoalDetails.value.reduce((sum, item) => sum + Number(item.stats?.totalDays || 0), 0)
})

const bestContinuousDays = computed(() => {
  return visibleGoalDetails.value.reduce((max, item) => {
    return Math.max(max, Number(item.stats?.continuousDays || 0))
  }, 0)
})

const personalGoalCount = computed(() => {
  return visibleGoals.value.filter((goal) => goal.goalType !== 'TEAM').length
})

const teamGoalCount = computed(() => {
  return visibleGoals.value.filter((goal) => goal.goalType === 'TEAM').length
})

const todayProgress = computed(() => {
  const total = visibleGoalDetails.value.length
  if (total === 0) {
    return 0
  }
  return Math.round((checkedTodayCount.value / total) * 100)
})

const pendingGoals = computed(() => {
  return visibleGoalDetails.value
    .filter((item) => !item.checkedToday)
    .map((item) => item.goal)
    .slice(0, 5)
})

const recentGoals = computed(() => visibleGoals.value.slice(0, 3))

const statCards = computed(() => [
  { label: '我的目标', value: visibleGoalDetails.value.length, suffix: '个', tone: 'blue' },
  { label: '今日已完成', value: checkedTodayCount.value, suffix: '个', tone: 'green' },
  { label: '今日待完成', value: uncheckedTodayCount.value, suffix: '个', tone: 'amber' },
  { label: '累计打卡', value: totalCheckinDays.value, suffix: '天', tone: 'cyan' },
  { label: '最长连续', value: bestContinuousDays.value, suffix: '天', tone: 'violet' },
  { label: '我的小组', value: teams.value.length, suffix: '个', tone: 'slate' },
])

onMounted(loadDashboard)
</script>

<template>
  <section class="dashboard-page page-fade" v-loading="loading">
    <div class="hero-panel dashboard-hero">
      <div>
        <p class="eyebrow">今日概览</p>
        <h1>今天也把进度往前推一点。</h1>
        <p>看清目标、完成打卡，把学习节奏稳定下来。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="router.push('/goals')">去目标页</el-button>
        <el-button :loading="loading" @click="loadDashboard">刷新数据</el-button>
      </div>
    </div>

    <el-empty
      v-if="!loading && visibleGoalDetails.length === 0"
      class="dashboard-empty glass-card"
      description="还没有目标，先创建一个学习目标开始打卡"
    >
      <el-button type="primary" @click="router.push('/goals')">创建目标</el-button>
    </el-empty>

    <template v-else>
      <div class="dashboard-stats">
        <el-card
          v-for="card in statCards"
          :key="card.label"
          class="stat-card lift-card"
          :class="`tone-${card.tone}`"
          shadow="never"
        >
          <span>{{ card.label }}</span>
          <div>
            <strong>{{ card.value }}</strong>
            <small>{{ card.suffix }}</small>
          </div>
        </el-card>
      </div>

      <div class="dashboard-grid">
        <el-card class="dashboard-panel progress-panel lift-card" shadow="never">
          <template #header>
            <div class="card-header">
              <h2>今日完成进度</h2>
              <el-tag :type="todayProgress === 100 && visibleGoalDetails.length > 0 ? 'success' : 'info'" effect="plain">
                {{ checkedTodayCount }} / {{ visibleGoalDetails.length }}
              </el-tag>
            </div>
          </template>

          <div class="progress-value">{{ todayProgress }}%</div>
          <el-progress
            :percentage="todayProgress"
            :stroke-width="16"
            :show-text="false"
            :status="todayProgress === 100 && visibleGoalDetails.length > 0 ? 'success' : undefined"
          />
          <p class="panel-note">
            已完成 {{ checkedTodayCount }} 个目标，剩余 {{ uncheckedTodayCount }} 个。
          </p>
        </el-card>

        <el-card class="dashboard-panel lift-card" shadow="never">
          <template #header>
            <div class="card-header">
              <h2>目标类型分布</h2>
            </div>
          </template>

          <div class="goal-type-summary">
            <div class="type-stat">
              <span>个人目标</span>
              <strong>{{ personalGoalCount }}</strong>
              <el-progress
                :percentage="visibleGoals.length ? Math.round((personalGoalCount / visibleGoals.length) * 100) : 0"
                :stroke-width="9"
                :show-text="false"
              />
            </div>
            <div v-if="hasTeams" class="type-stat warm">
              <span>小组目标</span>
              <strong>{{ teamGoalCount }}</strong>
              <el-progress
                :percentage="visibleGoals.length ? Math.round((teamGoalCount / visibleGoals.length) * 100) : 0"
                :stroke-width="9"
                :show-text="false"
                color="#f59e0b"
              />
            </div>
          </div>
        </el-card>

        <el-card class="dashboard-panel lift-card" shadow="never">
          <template #header>
            <div class="card-header">
              <h2>今日待完成</h2>
              <el-button link type="primary" @click="router.push('/goals')">处理目标</el-button>
            </div>
          </template>

          <el-empty v-if="pendingGoals.length === 0" description="今天的目标都完成了" />
          <div v-else class="dashboard-goal-list">
            <div v-for="goal in pendingGoals" :key="goal.id" class="dashboard-goal-item">
              <div>
                <strong>{{ goal.title }}</strong>
                <span>{{ goal.description || '暂无描述' }}</span>
              </div>
              <el-tag :type="goal.goalType === 'TEAM' ? 'warning' : 'primary'" effect="plain">
                {{ goal.goalType === 'TEAM' ? '小组' : '个人' }}
              </el-tag>
            </div>
          </div>
        </el-card>

        <el-card class="dashboard-panel lift-card" shadow="never">
          <template #header>
            <div class="card-header">
              <h2>最近目标</h2>
              <el-button link type="primary" @click="router.push('/goals')">查看全部</el-button>
            </div>
          </template>

          <div class="quick-goal-grid">
            <div v-for="goal in recentGoals" :key="goal.id" class="quick-goal-card">
              <el-tag :type="goal.goalType === 'TEAM' ? 'warning' : 'primary'" effect="plain" size="small">
                {{ goal.goalType === 'TEAM' ? '小组目标' : '个人目标' }}
              </el-tag>
              <strong>{{ goal.title }}</strong>
              <span>{{ goal.startDate || '-' }} 至 {{ goal.endDate || '-' }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </template>
  </section>
</template>
