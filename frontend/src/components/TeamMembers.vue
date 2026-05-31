<script setup>
import { computed } from 'vue'

const props = defineProps({
  team: {
    type: Object,
    required: true,
  },
  members: {
    type: Array,
    default: () => [],
  },
  todayCheckins: {
    type: Array,
    default: () => [],
  },
  currentUserId: {
    type: Number,
    default: 1,
  },
  transferringUserId: {
    type: Number,
    default: null,
  },
  leavingTeamId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['transfer-owner', 'leave'])

const teamGoalTitle = computed(() => {
  return props.todayCheckins.find((item) => item.goalTitle)?.goalTitle || props.team?.goalTitle || ''
})

const hasTeamGoal = computed(() => Boolean(teamGoalTitle.value))

const rows = computed(() => {
  const checkinMap = new Map(
    props.todayCheckins.map((item) => [item.userId, Boolean(item.checkedToday)]),
  )

  return props.members.map((member) => ({
    ...member,
    checkedToday: hasTeamGoal.value ? checkinMap.get(member.userId) || false : null,
  }))
})

const currentMember = computed(() => {
  return rows.value.find((member) => member.userId === props.currentUserId) || null
})

const currentUserIsOwner = computed(() => {
  return currentMember.value?.role === 'OWNER'
})
</script>

<template>
  <el-card class="team-members-card lift-card" shadow="never">
    <template #header>
      <div class="team-detail-header">
        <div>
          <p class="eyebrow">Team detail</p>
          <h2>{{ props.team.name }}</h2>
          <p>{{ props.team.description || '暂无小组描述' }}</p>
        </div>
        <el-button
          type="danger"
          plain
          :loading="props.leavingTeamId === props.team.id"
          :disabled="props.leavingTeamId !== null"
          @click="emit('leave', props.team)"
        >
          退出小组
        </el-button>
      </div>
    </template>

    <div class="team-detail-meta">
      <div>
        <span>共同目标</span>
        <strong>{{ teamGoalTitle || '无共同目标' }}</strong>
      </div>
      <div>
        <span>邀请码</span>
        <strong>{{ props.team.inviteCode || '-' }}</strong>
      </div>
      <div>
        <span>成员数</span>
        <strong>{{ rows.length }}</strong>
      </div>
    </div>

    <el-empty v-if="rows.length === 0" description="暂无成员" />

    <el-table v-else :data="rows" class="members-table">
      <el-table-column prop="nickname" label="成员" min-width="130">
        <template #default="{ row }">
          {{ row.nickname || row.username || `用户 ${row.userId}` }}
        </template>
      </el-table-column>

      <el-table-column prop="username" label="用户名" min-width="120" />

      <el-table-column prop="role" label="角色" width="110">
        <template #default="{ row }">
          <el-tag :type="row.role === 'OWNER' ? 'warning' : 'info'" size="small" effect="plain">
            {{ row.role === 'OWNER' ? '组长' : '成员' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="checkedToday" label="今日状态" min-width="140">
        <template #default="{ row }">
          <el-tag v-if="row.checkedToday === null" type="info" effect="plain">无共同目标</el-tag>
          <el-tag v-else-if="row.checkedToday" type="success" effect="plain">已完成</el-tag>
          <el-tag v-else type="warning" effect="plain">未完成</el-tag>
        </template>
      </el-table-column>

      <el-table-column v-if="currentUserIsOwner" label="操作" width="140">
        <template #default="{ row }">
          <el-button
            v-if="row.userId !== props.currentUserId"
            size="small"
            type="primary"
            plain
            :disabled="props.transferringUserId !== null"
            :loading="props.transferringUserId === row.userId"
            @click="emit('transfer-owner', row)"
          >
            转让组长
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>
