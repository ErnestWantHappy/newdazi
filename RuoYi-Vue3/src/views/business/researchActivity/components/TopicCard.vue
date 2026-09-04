<template>
  <el-card class="topic-card" shadow="hover" @click="$emit('open', topic)">
    <div class="topic-card__title">
      <div>
        <el-tag v-if="topic.isPinned === 'Y'" type="danger" size="small">置顶</el-tag>
        <el-tag :type="topic.topicType === 'NOTICE' ? 'warning' : 'info'" size="small">
          {{ topic.topicType === 'NOTICE' ? '活动通知' : '交流分享' }}
        </el-tag>
        <strong>{{ topic.title }}</strong>
      </div>
      <div class="topic-card__actions">
        <span v-if="topic.activityTime" class="topic-card__activity-time">活动时间：{{ topic.activityTime }}</span>
        <el-dropdown v-if="topic.owner || manager" trigger="click" @click.stop @command="$emit('action', $event, topic)">
          <el-button link icon="MoreFilled" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="topic.owner" command="edit">编辑</el-dropdown-item>
              <el-dropdown-item v-if="topic.owner || manager" command="delete">隐藏</el-dropdown-item>
              <el-dropdown-item v-if="manager" command="pin">{{ topic.isPinned === 'Y' ? '取消置顶' : '置顶' }}</el-dropdown-item>
              <el-dropdown-item v-if="manager && topic.topicType === 'NOTICE'" command="notify">再次通知</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <p class="topic-card__summary">{{ topic.contentText }}</p>
    <div class="topic-card__meta">
      <span>{{ topic.creatorName }} · {{ topic.deptName || '未标注学校' }}</span>
      <span>{{ topic.createTime }}<template v-if="topic.edited"> · 已编辑</template></span>
      <span>浏览 {{ topic.viewCount || 0 }} · 回复 {{ topic.replyCount || 0 }} · 下载 {{ topic.downloadCount || 0 }}</span>
    </div>
  </el-card>
</template>

<script setup>
defineProps({ topic: { type: Object, required: true }, manager: { type: Boolean, default: false } })
defineEmits(['open', 'action'])
</script>

<style scoped>
.topic-card { margin-bottom: 14px; cursor: pointer; }
.topic-card__title, .topic-card__meta { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.topic-card__title > div { display: flex; align-items: center; gap: 8px; min-width: 0; }
.topic-card__actions { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.topic-card__activity-time { color: var(--el-color-warning-dark-2); font-size: 13px; font-weight: 500; }
.topic-card__title strong { font-size: 17px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.topic-card__summary { margin: 12px 0; color: var(--el-text-color-regular); display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.topic-card__meta { color: var(--el-text-color-secondary); font-size: 13px; flex-wrap: wrap; justify-content: flex-start; }
</style>
