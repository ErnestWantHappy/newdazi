<template>
  <div class="student-info-card">
    <div v-if="studentInfo" class="info-content">
      <!-- 第一组：姓名和备注 -->
      <div class="info-group main-group">
        <div class="avatar-wrapper">
          <el-avatar :size="64" class="user-avatar" style="background-color: #409EFF">
            <el-icon :size="32"><UserFilled /></el-icon>
          </el-avatar>
        </div>
        <div class="name-info">
          <div class="student-name">{{ studentInfo.studentName }}</div>
          <div class="student-remark" v-if="studentInfo.remark">
            <el-tag size="small" effect="dark" type="info">{{ studentInfo.remark }}</el-tag>
          </div>
        </div>
      </div>

      <!-- 第二组：班级信息 -->
      <div class="info-group class-info">
        <div class="info-item">
          <div class="label">年级</div>
          <div class="value">{{ studentInfo.gradeName || '-' }}</div>
        </div>
        <div class="divider"></div>
        <div class="info-item">
          <div class="label">班级</div>
          <div class="value">{{ studentInfo.classCode }}班</div>
        </div>
        <div class="divider"></div>
        <div class="info-item">
          <div class="label">入学年份</div>
          <div class="value">{{ studentInfo.entryYear }}级</div>
        </div>
      </div>

      <!-- 第三组：核心指标 -->
      <div class="info-group metrics-info">
        <div class="metric-item">
          <div class="metric-icon rank-icon">
            <el-icon><Trophy /></el-icon>
          </div>
          <div class="metric-data">
            <div class="label">班级排名</div>
            <div class="value highlight">{{ studentInfo.currentRank || '-' }} <span class="sub">/ {{ studentInfo.classTotal || '-' }}</span></div>
          </div>
        </div>
        
        <div class="metric-item">
          <div class="metric-icon score-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="metric-data">
            <div class="label">平均成绩</div>
            <div class="value">{{ studentInfo.averageScore || '-' }}</div>
          </div>
        </div>

        <div class="metric-item">
          <div class="metric-icon speed-icon">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="metric-data">
            <div class="label">打字速度</div>
            <div class="value">{{ studentInfo.typingSpeedAvg || '-' }} <span class="unit">字/分</span></div>
          </div>
        </div>
        
        <div class="metric-item">
          <div class="metric-icon performance-icon">
             <el-icon><Star /></el-icon>
          </div>
           <div class="metric-data">
            <div class="label">课堂表现平均</div>
            <div class="value">{{ studentInfo.performanceAvg || '-' }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-state">
      请选择学生查看画像
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Trophy, TrendCharts, Monitor, Star, UserFilled } from '@element-plus/icons-vue'

const props = defineProps({
  studentInfo: { type: Object, default: null }
})
</script>

<style scoped lang="scss">
.student-info-card {
  background: linear-gradient(135deg, #1c2438 0%, #304156 100%);
  border-radius: 12px;
  color: #fff;
  padding: 24px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.info-content {
  display: grid;
  grid-template-columns: 280px 300px 1fr;
  gap: 24px;
  align-items: center;
}

.info-group {
  padding: 0 16px;
  
  &.main-group {
    display: flex;
    align-items: center;
    gap: 16px;
    border-right: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  &.class-info {
    display: flex;
    align-items: center;
    justify-content: space-around;
    border-right: 1px solid rgba(255, 255, 255, 0.1);
    
    .info-item {
      text-align: center;
      .label {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.6);
        margin-bottom: 4px;
      }
      .value {
        font-size: 18px;
        font-weight: 600;
        color: #fff;
      }
    }
    
    .divider {
      width: 1px;
      height: 24px;
      background: rgba(255, 255, 255, 0.1);
    }
  }
  
  &.metrics-info {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    
    .metric-item {
      display: flex;
      align-items: center;
      gap: 10px;
      background: rgba(255, 255, 255, 0.05);
      padding: 10px;
      border-radius: 8px;
      transition: all 0.3s;
      
      &:hover {
        background: rgba(255, 255, 255, 0.1);
        transform: translateY(-2px);
      }
      
      .metric-icon {
        width: 36px;
        height: 36px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
        
        &.rank-icon { background: rgba(255, 193, 7, 0.2); color: #ffc107; }
        &.score-icon { background: rgba(64, 158, 255, 0.2); color: #409eff; }
        &.speed-icon { background: rgba(103, 194, 58, 0.2); color: #67c23a; }
        &.performance-icon { background: rgba(245, 108, 108, 0.2); color: #f56c6c; }
      }
      
      .metric-data {
        .label {
          font-size: 11px;
          color: rgba(255, 255, 255, 0.6);
        }
        .value {
          font-size: 16px;
          font-weight: bold;
          color: #fff;
          
          &.highlight { color: #ffc107; }
          .sub { font-size: 12px; color: rgba(255, 255, 255, 0.5); font-weight: normal; }
          .unit { font-size: 11px; font-weight: normal; color: rgba(255, 255, 255, 0.6); }
        }
      }
    }
  }
}

.student-name {
  font-size: 24px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 4px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.empty-state {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  padding: 40px;
}
</style>
