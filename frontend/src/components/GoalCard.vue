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

const emit = defineEmits(['checkin', 'delete'])
</script>

<template>
  <el-card class="goal-card" shadow="never">
    <div class="goal-card-top">
      <div>
        <h3>{{ props.goal.title }}</h3>
        <p>{{ props.goal.description || '暂无描述' }}</p>
      </div>

      <el-tag :type="props.checkedToday ? 'success' : 'info'" effect="plain">
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
        type="primary"
        :disabled="props.checkedToday"
        :loading="props.loading"
        @click="emit('checkin', props.goal)"
      >
        {{ props.checkedToday ? '今日已打卡' : '今日打卡' }}
      </el-button>
      <el-button
        type="danger"
        plain
        :loading="props.deleting"
        @click="emit('delete', props.goal)"
      >
        删除目标
      </el-button>
    </div>
  </el-card>
</template>
