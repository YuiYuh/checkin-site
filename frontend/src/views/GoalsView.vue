<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
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
const showCreateForm = ref(false)
const checkinLoading = reactive({})
const deleteLoading = reactive({})
const goalStats = reactive({})
const todayStatus = reactive({})

const checkedCount = computed(() => goals.value.filter((goal) => todayStatus[goal.id]).length)
const pendingCount = computed(() => Math.max(goals.value.length - checkedCount.value, 0))

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

const handleCreated = async () => {
  showCreateForm.value = false
  await loadGoals()
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
  <section class="goals-page page-fade">
    <div class="hero-panel page-hero">
      <div>
        <p class="eyebrow">Goals</p>
        <h1>目标管理</h1>
        <p>把学习计划拆成每天可以完成的行动。</p>
      </div>
      <div class="summary-pills">
        <span><strong>{{ goals.length }}</strong> 总目标</span>
        <span><strong>{{ checkedCount }}</strong> 今日完成</span>
        <span><strong>{{ pendingCount }}</strong> 今日待完成</span>
      </div>
    </div>

    <div class="toolbar-row">
      <el-button type="primary" @click="showCreateForm = !showCreateForm">
        {{ showCreateForm ? '收起表单' : '新建目标' }}
      </el-button>
      <el-button :loading="pageLoading" @click="loadGoals">刷新</el-button>
    </div>

    <transition name="soft-expand">
      <GoalForm v-if="showCreateForm" @created="handleCreated" />
    </transition>

    <div class="goals-list goal-card-grid" v-loading="pageLoading">
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
  </section>
</template>
