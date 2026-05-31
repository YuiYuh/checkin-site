<script setup>
const props = defineProps({
  teams: {
    type: Array,
    default: () => [],
  },
  selectedTeamId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['select'])
</script>

<template>
  <el-card class="team-list-card lift-card" shadow="never">
    <template #header>
      <div class="card-header">
        <div>
          <h2>我的小组</h2>
          <p>选择一个小组查看成员和今日完成状态。</p>
        </div>
      </div>
    </template>

    <el-empty v-if="props.teams.length === 0" description="暂无小组，请先创建或加入小组" />

    <div v-else class="team-list">
      <div
        v-for="team in props.teams"
        :key="team.id"
        class="team-list-item"
        :class="{ active: team.id === props.selectedTeamId }"
      >
        <div class="team-list-content">
          <span class="team-name">{{ team.name }}</span>
          <span class="team-description">{{ team.description || '暂无描述' }}</span>
          <span class="team-meta">
            <span class="team-goal-name">目标：{{ team.goalTitle || '未绑定目标' }}</span>
            <span class="invite-code">邀请码：{{ team.inviteCode || '-' }}</span>
            <span class="role-chip">我的角色：{{ team.role === 'OWNER' ? '组长' : '成员' }}</span>
          </span>
        </div>
        <el-button size="small" type="primary" plain @click="emit('select', team)">
          查看小组
        </el-button>
      </div>
    </div>
  </el-card>
</template>
