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
      <div
        v-for="team in props.teams"
        :key="team.id"
        class="team-list-item"
        :class="{ active: team.id === props.selectedTeamId }"
      >
        <div class="team-list-content">
          <span class="team-name">{{ team.name }}</span>
          <span class="team-description">{{ team.description || '暂无描述' }}</span>
        </div>
        <el-button size="small" type="primary" plain @click="emit('select', team)">
          查看小组
        </el-button>
      </div>
    </div>
  </el-card>
</template>
