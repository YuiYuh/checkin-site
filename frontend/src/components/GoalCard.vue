<script setup>
const props = defineProps({
  goal: {
    type: Object,
    required: true,
  },
  stats: {
    type: Object,
    default: () => ({ totalDays: 0, continuousDays: 0 }),
  },
  checkedToday: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  deleting: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['checkin', 'cancel-checkin', 'delete'])
</script>

<template>
  <el-card class="goal-card lift-card" shadow="never">
    <div class="goal-card-top">
      <div class="goal-main">
        <div class="goal-title-line">
          <h3>{{ props.goal.title }}</h3>
          <el-tag v-if="props.goal.goalType === 'TEAM'" type="warning" effect="plain">
            小组目标
          </el-tag>
          <el-tag v-else effect="plain">个人目标</el-tag>
        </div>
        <p>{{ props.goal.description || '暂无描述' }}</p>
        <p v-if="props.goal.goalType === 'TEAM'" class="goal-team-name">
          所属小组：{{ props.goal.teamName || '-' }}
        </p>
      </div>

      <el-tag :type="props.checkedToday ? 'success' : 'info'" effect="plain" round>
        {{ props.checkedToday ? '今日已打卡' : '今日未打卡' }}
      </el-tag>
    </div>

    <div class="goal-dates">
      <span>开始：{{ props.goal.startDate || '-' }}</span>
      <span>结束：{{ props.goal.endDate || '-' }}</span>
    </div>

    <div class="stats-grid">
      <div>
        <span>累计打卡</span>
        <strong>{{ props.stats?.totalDays || 0 }}</strong>
        <small>天</small>
      </div>
      <div>
        <span>连续打卡</span>
        <strong>{{ props.stats?.continuousDays || 0 }}</strong>
        <small>天</small>
      </div>
    </div>

    <div class="goal-actions">
      <el-button
        :type="props.checkedToday ? 'warning' : 'primary'"
        :plain="props.checkedToday"
        :loading="props.loading"
        @click="props.checkedToday ? emit('cancel-checkin', props.goal) : emit('checkin', props.goal)"
      >
        {{ props.checkedToday ? '取消打卡' : '今日打卡' }}
      </el-button>
      <el-button
        type="danger"
        plain
        :disabled="props.goal.goalType === 'TEAM'"
        :loading="props.deleting"
        @click="emit('delete', props.goal)"
      >
        删除目标
      </el-button>
    </div>
  </el-card>
</template>
