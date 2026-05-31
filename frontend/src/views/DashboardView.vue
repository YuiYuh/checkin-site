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

const recentGoals = computed(() => {
  return visibleGoals.value.slice(0, 5)
})

const statCards = computed(() => [
  { label: '我的目标', value: visibleGoalDetails.value.length, suffix: '个' },
  { label: '今日已打卡', value: checkedTodayCount.value, suffix: '个', type: 'success' },
  { label: '今日未打卡', value: uncheckedTodayCount.value, suffix: '个', type: 'warning' },
  { label: '累计打卡', value: totalCheckinDays.value, suffix: '天' },
  { label: '最高连续', value: bestContinuousDays.value, suffix: '天' },
  { label: '加入小组', value: teams.value.length, suffix: '个' },
])

onMounted(loadDashboard)
</script>

<template>
  <section class="dashboard-page page-fade" v-loading="loading">
    <div class="page-title-row dashboard-title">
      <div>
        <p class="eyebrow">Dashboard</p>
        <h1>学习打卡看板</h1>
        <p>查看今日完成情况、目标类型分布和近期需要关注的目标。</p>
      </div>

      <el-button :loading="loading" @click="loadDashboard">刷新</el-button>
    </div>

    <el-empty
      v-if="!loading && visibleGoalDetails.length === 0"
      class="dashboard-empty"
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
          shadow="never"
        >
          <span>{{ card.label }}</span>
          <strong :class="card.type">{{ card.value }}</strong>
          <small>{{ card.suffix }}</small>
        </el-card>
      </div>

      <div class="dashboard-grid">
        <el-card class="dashboard-panel lift-card" shadow="never">
          <template #header>
            <div class="card-header">
              <h2>今日完成情况</h2>
              <el-tag :type="todayProgress === 100 && visibleGoalDetails.length > 0 ? 'success' : 'info'" effect="plain">
                {{ todayProgress }}%
              </el-tag>
            </div>
          </template>

          <el-progress
            :percentage="todayProgress"
            :stroke-width="14"
            :status="todayProgress === 100 && visibleGoalDetails.length > 0 ? 'success' : undefined"
          />
          <p class="panel-note">
            已完成 {{ checkedTodayCount }} 个，剩余 {{ uncheckedTodayCount }} 个。
          </p>
        </el-card>

        <el-card class="dashboard-panel lift-card" shadow="never">
          <template #header>
            <div class="card-header">
              <h2>目标类型分布</h2>
            </div>
          </template>

          <div class="type-bars">
            <div class="type-row">
              <span>个人目标</span>
              <el-progress
                :percentage="visibleGoals.length ? Math.round((personalGoalCount / visibleGoals.length) * 100) : 0"
                :stroke-width="10"
              />
              <strong>{{ personalGoalCount }}</strong>
            </div>
            <div v-if="hasTeams" class="type-row">
              <span>小组目标</span>
              <el-progress
                :percentage="visibleGoals.length ? Math.round((teamGoalCount / visibleGoals.length) * 100) : 0"
                :stroke-width="10"
                color="#f59e0b"
              />
              <strong>{{ teamGoalCount }}</strong>
            </div>
          </div>
        </el-card>

        <el-card class="dashboard-panel dashboard-list-panel lift-card" shadow="never">
          <template #header>
            <div class="card-header">
              <h2>{{ pendingGoals.length ? '今日待完成目标' : '最近目标' }}</h2>
              <el-button link type="primary" @click="router.push('/goals')">查看全部</el-button>
            </div>
          </template>

          <div class="dashboard-goal-list">
            <div
              v-for="goal in pendingGoals.length ? pendingGoals : recentGoals"
              :key="goal.id"
              class="dashboard-goal-item"
            >
              <div>
                <strong>{{ goal.title }}</strong>
                <span>{{ goal.description || '暂无描述' }}</span>
              </div>
              <el-tag :type="goal.goalType === 'TEAM' ? 'warning' : 'primary'" effect="plain">
                {{ goal.goalType === 'TEAM' ? '小组目标' : '个人目标' }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>
    </template>
  </section>
</template>
