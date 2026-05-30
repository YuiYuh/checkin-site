<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
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

const selectedTeam = computed(() => {
  return teams.value.find((team) => team.id === selectedTeamId.value) || null
})

const loadTeamDetail = async (team) => {
  if (!team?.id) {
    selectedTeamId.value = null
    members.value = []
    todayCheckins.value = []
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

const loadTeams = async () => {
  teamsLoading.value = true
  try {
    const result = await api.get('/api/teams/my')
    teams.value = Array.isArray(result) ? result : []

    if (teams.value.length === 0) {
      await loadTeamDetail(null)
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
          @select="loadTeamDetail"
        />
      </div>

      <div v-loading="detailLoading">
        <TeamMembers
          :team="selectedTeam"
          :members="members"
          :today-checkins="todayCheckins"
        />
      </div>
    </div>
  </section>
</template>
