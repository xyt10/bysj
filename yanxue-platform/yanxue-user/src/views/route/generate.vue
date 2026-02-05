<template>
  <div class="generate-page">
    <!-- 标题区域 -->
    <div class="card header-card">
      <h1 class="page-title">
        <el-icon><MagicStick /></el-icon>
        AI智能生成研学路线
      </h1>
      <p class="page-desc">输入您的需求，AI将为您智能规划个性化研学路线</p>
    </div>

    <!-- 输入表单 -->
    <div class="card form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="研学主题" prop="theme">
              <el-input v-model="form.theme" placeholder="如：历史文化、科技探索" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出发城市" prop="startCity">
              <el-input v-model="form.startCity" placeholder="如：广州" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="天数" prop="days">
              <el-select v-model="form.days" placeholder="选择天数" style="width: 100%">
                <el-option v-for="d in 7" :key="d" :label="`${d}天`" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学段" prop="grade">
              <el-select v-model="form.grade" placeholder="选择学段" style="width: 100%">
                <el-option label="小学" value="小学" />
                <el-option label="初中" value="初中" />
                <el-option label="高中" value="高中" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="人数" prop="peopleCount">
              <el-input-number v-model="form.peopleCount" :min="1" :max="200" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预算(元/人)" prop="budget">
              <el-input-number v-model="form.budget" :min="100" :max="10000" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="特殊要求">
          <el-input
            v-model="form.requirements"
            type="textarea"
            :rows="3"
            placeholder="如：希望包含博物馆参观、需要手工DIY活动等"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleGenerate"
          >
            <el-icon v-if="!loading"><MagicStick /></el-icon>
            {{ loading ? 'AI生成中...' : '开始生成路线' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 生成结果 -->
    <div v-if="result" class="card result-card">
      <div class="result-header">
        <h2 class="section-title">生成结果</h2>
        <div class="result-actions-top">
          <el-button @click="handleRegenerate">重新生成</el-button>
        </div>
      </div>

      <div class="route-summary">
        <h3 class="route-name">{{ result.route.name }}</h3>
        <p class="route-desc">{{ result.route.description }}</p>
        <div class="route-budget">
          <span class="label">预估费用：</span>
          <span class="price">{{ result.route.totalBudget }}</span>
          <span class="unit">元/人</span>
        </div>
      </div>

      <!-- 行程安排 -->
      <div class="schedule-section">
        <h3 class="schedule-title">详细行程</h3>
        <el-timeline>
          <el-timeline-item
            v-for="day in result.route.schedule"
            :key="day.day"
            :timestamp="`第${day.day}天`"
            placement="top"
            type="primary"
            :hollow="true"
          >
            <div class="day-content">
              <div v-for="spot in day.spots" :key="spot.name" class="spot-item">
                <div class="spot-header">
                  <span class="spot-time">
                    <el-icon><Clock /></el-icon>
                    {{ spot.startTime }} - {{ spot.endTime }}
                  </span>
                  <span class="spot-name">{{ spot.name }}</span>
                </div>
                <div class="spot-activity">{{ spot.activities }}</div>
                <div v-if="spot.knowledgePoints && spot.knowledgePoints.length" class="spot-tags">
                  <el-tag
                    v-for="point in spot.knowledgePoints"
                    :key="point"
                    size="small"
                    type="info"
                  >
                    {{ point }}
                  </el-tag>
                </div>
                <div v-if="spot.tips" class="spot-tips">
                  <el-icon><InfoFilled /></el-icon>
                  <span>{{ spot.tips }}</span>
                </div>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <div class="result-actions">
        <el-button type="primary" size="large" @click="handleSave">
          <el-icon><FolderAdd /></el-icon>
          保存路线
        </el-button>
        <el-button size="large" @click="handleRegenerate">
          <el-icon><Refresh /></el-icon>
          重新生成
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Clock, InfoFilled, FolderAdd, Refresh } from '@element-plus/icons-vue'
import { aiApi } from '@/api'

const formRef = ref(null)
const loading = ref(false)
const result = ref(null)

const form = reactive({
  theme: '',
  startCity: '广州',
  days: 2,
  grade: '初中',
  peopleCount: 30,
  budget: 500,
  requirements: ''
})

const rules = {
  theme: [{ required: true, message: '请输入研学主题', trigger: 'blur' }],
  startCity: [{ required: true, message: '请输入出发城市', trigger: 'blur' }],
  days: [{ required: true, message: '请选择天数', trigger: 'change' }],
  grade: [{ required: true, message: '请选择学段', trigger: 'change' }],
  peopleCount: [{ required: true, message: '请输入人数', trigger: 'blur' }],
  budget: [{ required: true, message: '请输入预算', trigger: 'blur' }]
}

const handleGenerate = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  result.value = null

  try {
    const res = await aiApi.generateRoute({
      theme: form.theme,
      days: form.days,
      budget: form.budget,
      startCity: form.startCity,
      grade: form.grade,
      peopleCount: form.peopleCount,
      requirements: form.requirements
    })

    if (res.data) {
      result.value = res.data
      ElMessage.success('路线生成成功')
    }
  } catch (e) {
    console.log('生成失败', e)
    // 模拟数据
    result.value = {
      route: {
        name: `${form.startCity}${form.theme}${form.days}日研学`,
        description: `根据您的需求，AI为您精心规划的${form.days}天${form.theme}主题研学路线，适合${form.grade}学段学生，预算约${form.budget}元/人。`,
        totalBudget: form.budget,
        schedule: generateMockSchedule(form.days)
      }
    }
    ElMessage.success('路线生成成功（演示数据）')
  } finally {
    loading.value = false
  }
}

const generateMockSchedule = (days) => {
  const schedule = []
  const spots = [
    { name: '广东省博物馆', activities: '参观历史文物展览，了解岭南文化发展历程', knowledgePoints: ['岭南历史', '文物鉴赏'], tips: '需提前预约' },
    { name: '广州科学中心', activities: '体验科技互动展览，参与科学实验', knowledgePoints: ['物理原理', '科技创新'], tips: '建议穿舒适鞋子' },
    { name: '陈家祠', activities: '欣赏岭南建筑艺术，学习木雕、砖雕工艺', knowledgePoints: ['传统建筑', '工艺美术'], tips: '可携带相机' },
    { name: '南越王博物院', activities: '探索南越国历史，观看珍贵出土文物', knowledgePoints: ['考古历史', '古代文明'], tips: '建议携带笔记本' },
    { name: '黄埔军校旧址', activities: '学习革命历史，感受红色精神', knowledgePoints: ['近代史', '革命传统'], tips: '穿着整洁' },
    { name: '华南植物园', activities: '认识植物种类，了解生态系统', knowledgePoints: ['植物学', '生态保护'], tips: '防晒防蚊' }
  ]

  for (let d = 1; d <= days; d++) {
    const daySpots = []
    const spotsPerDay = 2
    for (let s = 0; s < spotsPerDay; s++) {
      const spotIndex = ((d - 1) * spotsPerDay + s) % spots.length
      const startHour = s === 0 ? 9 : 14
      const endHour = s === 0 ? 12 : 17
      daySpots.push({
        ...spots[spotIndex],
        startTime: `${startHour}:00`,
        endTime: `${endHour}:00`
      })
    }
    schedule.push({ day: d, spots: daySpots })
  }
  return schedule
}

const handleRegenerate = () => {
  result.value = null
}

const handleSave = () => {
  ElMessage.success('路线已保存（功能开发中）')
}
</script>

<style lang="scss" scoped>
.generate-page {
  padding: 20px 0;
  animation: genIn 420ms cubic-bezier(0.2, 0.8, 0.2, 1);
}

@keyframes genIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card {
  background: var(--surface-strong);
  border-radius: var(--r-lg);
  padding: 24px;
  margin-bottom: 20px;
  border: 1px solid rgba(15, 118, 110, 0.06);
  box-shadow: var(--shadow-sm);
}

.header-card {
  text-align: center;
  background: var(--gradient-ocean);
  color: #fff;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .page-desc {
    font-size: 14px;
    opacity: 0.9;
    margin: 0;
  }
}

.form-card {
  :deep(.el-form-item__label) {
    font-weight: 500;
  }
}

.result-card {
  .result-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .section-title {
      font-size: 18px;
      font-weight: 700;
      margin: 0;
      color: var(--ink-900);
    }
  }

  .route-summary {
    background: rgba(255, 255, 255, 0.75);
    border-radius: var(--r-lg);
    padding: 20px;
    margin-bottom: 24px;
    border: 1px solid rgba(15, 118, 110, 0.06);
    box-shadow: var(--shadow-sm);

    .route-name {
      font-size: 20px;
      font-weight: 600;
      color: #333;
      margin: 0 0 12px;
    }

    .route-desc {
      font-size: 14px;
      color: #666;
      line-height: 1.6;
      margin: 0 0 16px;
    }

    .route-budget {
      display: flex;
      align-items: baseline;

      .label {
        font-size: 14px;
        color: var(--ink-600);
      }

      .price {
        font-size: 24px;
        font-weight: 800;
        color: #ef4444;
        margin: 0 4px;
      }

      .unit {
        font-size: 14px;
        color: var(--ink-500);
      }
    }
  }

  .schedule-section {
    margin-bottom: 24px;

    .schedule-title {
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 16px;
    }
  }

  .day-content {
    background: #fff;
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }

  .spot-item {
    padding: 12px 0;
    border-bottom: 1px dashed #eee;

    &:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }

    &:first-child {
      padding-top: 0;
    }
  }

  .spot-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 8px;

    .spot-time {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #4A90D9;
    }

    .spot-name {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .spot-activity {
    font-size: 14px;
    color: #666;
    line-height: 1.6;
    margin-bottom: 8px;
  }

  .spot-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;
  }

  .spot-tips {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #ff9500;
    background: #fff8f0;
    padding: 8px 12px;
    border-radius: 6px;
  }

  .result-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    padding-top: 20px;
    border-top: 1px solid #eee;
  }
}
</style>
