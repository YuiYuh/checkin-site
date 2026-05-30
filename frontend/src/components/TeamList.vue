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
        @click="emit('select', team)"
      >
        <span class="team-name">{{ team.name }}</span>
        <span class="team-description">{{ team.description || '暂无描述' }}</span>
        <span class="invite-code">邀请码：{{ team.inviteCode || '-' }}</span>
      </button>
    </div>
  </el-card>
</template>
