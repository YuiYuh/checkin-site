<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { getCurrentUser } from '../utils/auth'
import TeamForm from '../components/TeamForm.vue'
import TeamJoinForm from '../components/TeamJoinForm.vue'
import TeamList from '../components/TeamList.vue'
import TeamMembers from '../components/TeamMembers.vue'

const teams = ref([])
const selectedTeamId = ref(null)
const members = ref([])
const todayCheckins = ref([])
const teamsLoading = ref(false)
const detailLoading = ref(false)
const leavingTeamId = ref(null)
const transferringUserId = ref(null)

const currentUserId = computed(() => getCurrentUser()?.id || 1)

const selectedTeam = computed(() => {
  return teams.value.find((team) => team.id === selectedTeamId.value) || null
})

const clearTeamDetail = () => {
  selectedTeamId.value = null
  members.value = []
  todayCheckins.value = []
}

const loadTeamDetail = async (team) => {
  if (!team?.id) {
    clearTeamDetail()
    return
  }

  selectedTeamId.value = team.id
  detailLoading.value = true
  try {
    const [memberResult, checkinResult] = await Promise.all([
      api.get(`/api/teams/${team.id}/members`),
      api.get(`/api/teams/${team.id}/checkins/today`),
    ])

    members.value = Array.isArray(memberResult) ? memberResult : []
    todayCheckins.value = Array.isArray(checkinResult) ? checkinResult : []
  } catch (error) {
    ElMessage.error(error.message || '加载小组详情失败')
  } finally {
    detailLoading.value = false
  }
}

const loadTeams = async ({ keepSelection = true } = {}) => {
  teamsLoading.value = true
  try {
    const result = await api.get('/api/teams/my')
    teams.value = Array.isArray(result) ? result : []

    if (!keepSelection || teams.value.length === 0) {
      clearTeamDetail()
      return
    }

    const nextTeam =
      teams.value.find((team) => team.id === selectedTeamId.value) || teams.value[0]
    await loadTeamDetail(nextTeam)
  } catch (error) {
    ElMessage.error(error.message || '加载我的小组失败')
  } finally {
    teamsLoading.value = false
  }
}

const refreshSelectedTeam = async () => {
  if (selectedTeam.value) {
    await loadTeamDetail(selectedTeam.value)
  }
}

const leaveTeam = async (team) => {
  if (leavingTeamId.value !== null) {
    return
  }

  try {
    await ElMessageBox.confirm(`确定退出小组“${team.name}”吗？`, '退出小组', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  const leavingSelectedTeam = selectedTeamId.value === team.id
  leavingTeamId.value = team.id
  try {
    const message = await api.post(`/api/teams/${team.id}/leave`)
    ElMessage.success(message || '退出小组成功')
    await loadTeams({ keepSelection: !leavingSelectedTeam })
  } catch (error) {
    ElMessage.error(error.message || '退出小组失败')
  } finally {
    leavingTeamId.value = null
  }
}

const transferOwner = async (member) => {
  if (!selectedTeam.value || transferringUserId.value !== null) {
    return
  }

  const memberName = member.nickname || member.username || `用户 ${member.userId}`
  try {
    await ElMessageBox.confirm(
      `确定将小组“${selectedTeam.value.name}”的组长转让给“${memberName}”吗？`,
      '转让组长',
      {
        confirmButtonText: '转让',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  transferringUserId.value = member.userId
  try {
    const message = await api.post(`/api/teams/${selectedTeam.value.id}/transfer-owner`, {
      newOwnerUserId: member.userId,
    })
    ElMessage.success(message || '组长转让成功')
    await loadTeams({ keepSelection: true })
    await refreshSelectedTeam()
  } catch (error) {
    ElMessage.error(error.message || '转让组长失败')
  } finally {
    transferringUserId.value = null
  }
}

onMounted(loadTeams)
</script>

<template>
  <section class="teams-page">
    <div class="page-title-row">
      <div>
        <p class="eyebrow">Teams</p>
        <h1>小组协作</h1>
        <p>创建小组、通过邀请码加入小组，并查看成员今日打卡状态。</p>
      </div>

      <el-button :loading="teamsLoading" @click="loadTeams">
        刷新
      </el-button>
    </div>

    <div class="team-actions-grid">
      <TeamForm @created="loadTeams" />
      <TeamJoinForm @joined="loadTeams" />
    </div>

    <div class="teams-layout">
      <div v-loading="teamsLoading">
        <TeamList
          :teams="teams"
          :selected-team-id="selectedTeamId"
          :leaving-team-id="leavingTeamId"
          @select="loadTeamDetail"
          @leave="leaveTeam"
        />
      </div>

      <div v-loading="detailLoading">
        <TeamMembers
          :team="selectedTeam"
          :members="members"
          :today-checkins="todayCheckins"
          :current-user-id="currentUserId"
          :transferring-user-id="transferringUserId"
          @transfer-owner="transferOwner"
        />
      </div>
    </div>
  </section>
</template>
