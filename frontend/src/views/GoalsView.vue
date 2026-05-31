<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api, {
  cancelTodayCheckin,
  createTodayCheckin,
  getGoalStats,
  getGoals,
  getTodayCheckinStatus,
} from '../api'
import GoalCard from '../components/GoalCard.vue'
import GoalForm from '../components/GoalForm.vue'

const goals = ref([])
const pageLoading = ref(false)
const checkinLoading = reactive({})
const deleteLoading = reactive({})
const goalStats = reactive({})
const todayStatus = reactive({})

const loadGoalDetail = async (goalId) => {
  const [stats, checkedToday] = await Promise.all([
    getGoalStats(goalId),
    getTodayCheckinStatus(goalId),
  ])

  goalStats[goalId] = stats || { totalDays: 0, continuousDays: 0 }
  todayStatus[goalId] = checkedToday
}

const loadGoals = async () => {
  pageLoading.value = true
  try {
    const result = await getGoals()
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
    await createTodayCheckin(goal.id)
    ElMessage.success('打卡成功')
    await refreshGoal(goal.id)
  } catch (error) {
    ElMessage.error(error.message || '打卡失败')
    await refreshGoal(goal.id)
  } finally {
    checkinLoading[goal.id] = false
  }
}

const cancelCheckinToday = async (goal) => {
  try {
    await ElMessageBox.confirm('确定要取消今日打卡吗？', '取消打卡', {
      confirmButtonText: '取消打卡',
      cancelButtonText: '返回',
      type: 'warning',
    })
  } catch {
    return
  }

  checkinLoading[goal.id] = true
  try {
    await cancelTodayCheckin(goal.id)
    ElMessage.success('已取消今日打卡')
    await refreshGoal(goal.id)
  } catch (error) {
    ElMessage.error(error.message || '取消打卡失败')
    await refreshGoal(goal.id)
  } finally {
    checkinLoading[goal.id] = false
  }
}

const deleteGoal = async (goal) => {
  try {
    await ElMessageBox.confirm(
      `确定删除目标“${goal.title}”吗？该目标的打卡记录也会一起删除。`,
      '删除目标',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  deleteLoading[goal.id] = true
  try {
    await api.delete(`/api/goals/${goal.id}`)
    ElMessage.success('目标删除成功')
    await loadGoals()
  } catch (error) {
    ElMessage.error(error.message || '删除目标失败')
  } finally {
    deleteLoading[goal.id] = false
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
        <p>管理个人目标和小组目标，查看今日打卡状态、累计天数和连续打卡表现。</p>
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
          :deleting="deleteLoading[goal.id]"
          @checkin="checkinToday"
          @cancel-checkin="cancelCheckinToday"
          @delete="deleteGoal"
        />
      </div>
    </div>
  </section>
</template>
