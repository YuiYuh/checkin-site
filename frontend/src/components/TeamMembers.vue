<script setup>
import { computed } from 'vue'

const props = defineProps({
  team: {
    type: Object,
    default: null,
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
})

const emit = defineEmits(['transfer-owner'])

const teamGoalTitle = computed(() => {
  return props.todayCheckins.find((item) => item.goalTitle)?.goalTitle || props.team?.goalTitle || ''
})

const rows = computed(() => {
  const checkinMap = new Map(
    props.todayCheckins.map((item) => [item.userId, Boolean(item.checkedToday)]),
  )

  return props.members.map((member) => ({
    ...member,
    checkedToday: checkinMap.get(member.userId) || false,
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
  <el-card class="team-members-card" shadow="never">
    <template #header>
      <div class="card-header team-members-header">
        <div>
          <h2>小组成员</h2>
          <p v-if="props.team">
            {{ props.team.name }} · 邀请码：{{ props.team.inviteCode || '-' }}
          </p>
          <p v-if="props.team">
            小组目标：{{ teamGoalTitle || '未绑定目标' }}
          </p>
        </div>
      </div>
    </template>

    <el-empty v-if="!props.team" description="请选择一个小组查看成员" />
    <el-empty v-else-if="rows.length === 0" description="暂无成员" />

    <el-table v-else :data="rows" class="members-table">
      <el-table-column prop="nickname" label="昵称" min-width="130">
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

      <el-table-column prop="checkedToday" label="小组目标状态" min-width="190">
        <template #default="{ row }">
          <el-tag :type="row.checkedToday ? 'success' : 'info'" effect="plain">
            {{ row.checkedToday ? '今日已完成小组目标' : '今日未完成小组目标' }}
          </el-tag>
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
