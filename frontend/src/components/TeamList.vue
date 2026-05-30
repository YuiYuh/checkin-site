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
  leavingTeamId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['select', 'leave'])
</script>

<template>
  <el-card class="team-list-card" shadow="never">
    <template #header>
      <div class="card-header">
        <h2>我的小组</h2>
      </div>
    </template>

    <el-empty v-if="props.teams.length === 0" description="暂无小组，请先创建或加入小组" />

    <div v-else class="team-list">
      <button
        v-for="team in props.teams"
        :key="team.id"
        class="team-list-item"
        :class="{ active: team.id === props.selectedTeamId }"
        type="button"
        :disabled="props.leavingTeamId !== null"
        @click="emit('select', team)"
      >
        <span class="team-name">{{ team.name }}</span>
        <span class="team-description">{{ team.description || '暂无描述' }}</span>
        <span class="invite-code">邀请码：{{ team.inviteCode || '-' }}</span>
        <span class="team-goal-name">绑定目标：{{ team.goalTitle || '未绑定目标' }}</span>
        <span class="team-item-actions">
          <el-button
            size="small"
            type="danger"
            plain
            :disabled="props.leavingTeamId !== null"
            :loading="props.leavingTeamId === team.id"
            @click.stop="emit('leave', team)"
          >
            退出小组
          </el-button>
        </span>
      </button>
    </div>
  </el-card>
</template>
