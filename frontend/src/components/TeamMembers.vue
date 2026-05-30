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
        </div>
      </div>
    </template>

    <el-empty v-if="!props.team" description="请选择一个小组查看成员" />
    <el-empty v-else-if="rows.length === 0" description="暂无成员" />

    <el-table v-else :data="rows" class="members-table">
      <el-table-column prop="nickname" label="昵称" min-width="140">
        <template #default="{ row }">
          {{ row.nickname || row.username || `用户 ${row.userId}` }}
        </template>
      </el-table-column>

      <el-table-column prop="username" label="用户名" min-width="140" />

      <el-table-column prop="role" label="角色" width="120">
        <template #default="{ row }">
          <el-tag size="small" effect="plain">
            {{ row.role || 'member' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="checkedToday" label="今日状态" width="150">
        <template #default="{ row }">
          <el-tag :type="row.checkedToday ? 'success' : 'info'" effect="plain">
            {{ row.checkedToday ? '今日已打卡' : '今日未打卡' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>
