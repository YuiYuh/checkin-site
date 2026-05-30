<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import GoalCard from '../components/GoalCard.vue'
import GoalForm from '../components/GoalForm.vue'

const goals = ref([])
const pageLoading = ref(false)
const checkinLoading = reactive({})
const goalStats = reactive({})
const todayStatus = reactive({})

const loadGoalDetail = async (goalId) => {
  const [stats, checkedToday] = await Promise.all([
    api.get(`/api/goals/${goalId}/stats`),
    api.get(`/api/checkins/today/${goalId}`),
  ])

  goalStats[goalId] = stats || { totalDays: 0, continuousDays: 0 }
  todayStatus[goalId] = Boolean(checkedToday)
}

const loadGoals = async () => {
  pageLoading.value = true
  try {
    const result = await api.get('/api/goals')
    goals.value = Array.isArray(result) ? result : []

    await Promise.all(goals.value.map((goal) => loadGoalDetail(goal.id)))
  } catch (error) {
    ElMessage.error(error.message || '加载目标失败')
  } finally {
    pageLoading.value = false
  }
}

const refreshGoal = async (goalId) => {
  try {
    await loadGoalDetail(goalId)
  } catch (error) {
    ElMessage.error(error.message || '刷新目标状态失败')
  }
}

const checkinToday = async (goal) => {
  checkinLoading[goal.id] = true
  try {
    await api.post('/api/checkins', {
      goalId: goal.id,
      content: '前端 MVP 今日打卡',
    })

    ElMessage.success('打卡成功')
    await refreshGoal(goal.id)
  } catch (error) {
    ElMessage.error(error.message || '打卡失败')
    await refreshGoal(goal.id)
  } finally {
    checkinLoading[goal.id] = false
  }
}

onMounted(loadGoals)
</script>

<template>
  <section class="goals-page">
    <div class="page-title-row">
      <div>
        <p class="eyebrow">Goals</p>
        <h1>目标列表</h1>
        <p>展示所有目标、今日打卡状态，以及累计和连续打卡天数。</p>
      </div>

      <el-button :loading="pageLoading" @click="loadGoals">
        刷新
      </el-button>
    </div>

    <div class="goals-layout">
      <GoalForm @created="loadGoals" />

      <div class="goals-list" v-loading="pageLoading">
        <el-empty v-if="!pageLoading && goals.length === 0" description="暂无目标，请先创建一个目标" />

        <GoalCard
          v-for="goal in goals"
          :key="goal.id"
          :goal="goal"
          :stats="goalStats[goal.id]"
          :checked-today="todayStatus[goal.id]"
          :loading="checkinLoading[goal.id]"
          @checkin="checkinToday"
        />
      </div>
    </div>
  </section>
</template>
