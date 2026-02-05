<template>
  <div class="ai-config-page">
    <!-- AI统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalCalls }}</div>
            <div class="stat-label">总调用次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stats.todayCalls }}</div>
            <div class="stat-label">今日调用</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stats.recommendCalls }}</div>
            <div class="stat-label">推荐调用</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stats.avgDurationMs }}ms</div>
            <div class="stat-label">平均响应时间</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- AI服务配置 -->
    <el-card shadow="hover" class="config-card">
      <template #header>
        <div class="card-header">
          <span>AI服务配置</span>
          <el-tag :type="aiConfig.defaultProvider === 'qwen' ? 'success' : 'primary'" size="small">
            当前: {{ aiConfig.defaultProvider === 'qwen' ? 'Qwen' : 'OpenAI' }}
          </el-tag>
        </div>
      </template>

      <el-form :model="aiConfig" label-width="140px">
        <el-form-item label="默认AI服务">
          <el-radio-group v-model="aiConfig.defaultProvider">
            <el-radio label="qwen">Qwen (通义千问)</el-radio>
            <el-radio label="openai">OpenAI兼容接口</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider content-position="left">Qwen配置</el-divider>

        <el-form-item label="API Key">
          <el-input v-model="aiConfig.qwen.apiKey" type="password" show-password placeholder="请输入Qwen API Key" style="max-width: 400px" />
        </el-form-item>
        <el-form-item label="Base URL">
          <el-input v-model="aiConfig.qwen.baseUrl" placeholder="API地址" style="max-width: 400px" />
        </el-form-item>
        <el-form-item label="模型">
          <el-select v-model="aiConfig.qwen.model" style="width: 200px">
            <el-option label="qwen-turbo" value="qwen-turbo" />
            <el-option label="qwen-plus" value="qwen-plus" />
            <el-option label="qwen-max" value="qwen-max" />
          </el-select>
          <el-button type="success" plain size="small" style="margin-left: 10px" @click="testConnection('qwen')" :loading="testingQwen">
            测试连接
          </el-button>
        </el-form-item>
        <el-form-item label="超时时间(秒)">
          <el-slider v-model="aiConfig.qwen.timeout" :min="10" :max="120" :step="5" show-stops style="width: 300px" />
          <span style="margin-left: 10px; color: #666;">{{ aiConfig.qwen.timeout }}秒</span>
        </el-form-item>

        <el-divider content-position="left">OpenAI兼容接口配置</el-divider>

        <el-form-item label="API Key">
          <el-input v-model="aiConfig.openai.apiKey" type="password" show-password placeholder="请输入OpenAI API Key" style="max-width: 400px" />
        </el-form-item>
        <el-form-item label="Base URL">
          <el-input v-model="aiConfig.openai.baseUrl" placeholder="API地址" style="max-width: 400px" />
        </el-form-item>
        <el-form-item label="模型">
          <el-input v-model="aiConfig.openai.model" placeholder="模型名称" style="width: 200px" />
          <el-button type="success" plain size="small" style="margin-left: 10px" @click="testConnection('openai')" :loading="testingOpenai">
            测试连接
          </el-button>
        </el-form-item>
        <el-form-item label="超时时间(秒)">
          <el-slider v-model="aiConfig.openai.timeout" :min="10" :max="120" :step="5" show-stops style="width: 300px" />
          <span style="margin-left: 10px; color: #666;">{{ aiConfig.openai.timeout }}秒</span>
          <el-tooltip content="大模型(如gpt-oss-120b)可能需要60-120秒" placement="top">
            <el-icon style="margin-left: 5px; color: #909399;"><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- AI测试 -->
    <el-card shadow="hover" class="test-card">
      <template #header>
        <span>AI功能测试</span>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="推荐测试" name="recommend">
          <el-form :model="recommendForm" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="学段">
                  <el-select v-model="recommendForm.grade" style="width: 100%">
                    <el-option label="小学" value="小学" />
                    <el-option label="初中" value="初中" />
                    <el-option label="高中" value="高中" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="16">
                <el-form-item label="兴趣">
                  <el-checkbox-group v-model="recommendForm.interests">
                    <el-checkbox label="历史">历史</el-checkbox>
                    <el-checkbox label="科技">科技</el-checkbox>
                    <el-checkbox label="自然">自然</el-checkbox>
                    <el-checkbox label="红色教育">红色教育</el-checkbox>
                    <el-checkbox label="艺术">艺术</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="testRecommend" :loading="recommendLoading">测试推荐</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="路线生成测试" name="generate">
          <el-form :model="generateForm" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="主题">
                  <el-input v-model="generateForm.theme" placeholder="如：历史文化" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="天数">
                  <el-input-number v-model="generateForm.days" :min="1" :max="7" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="预算(元/人)">
                  <el-input-number v-model="generateForm.budget" :min="100" :step="100" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="出发城市">
                  <el-input v-model="generateForm.startCity" placeholder="如：广州" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="学段">
                  <el-select v-model="generateForm.grade" style="width: 100%">
                    <el-option label="小学" value="小学" />
                    <el-option label="初中" value="初中" />
                    <el-option label="高中" value="高中" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="人数">
                  <el-input-number v-model="generateForm.peopleCount" :min="1" :max="100" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="testGenerate" :loading="generateLoading">生成路线</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <!-- AI响应结果 -->
      <div v-if="aiResponse" class="ai-response">
        <el-divider>AI响应结果</el-divider>
        <pre>{{ JSON.stringify(aiResponse, null, 2) }}</pre>
      </div>
    </el-card>

    <!-- AI调用日志 -->
    <el-card shadow="hover" class="log-card">
      <template #header>
        <div class="card-header">
          <span>AI调用日志</span>
          <div>
            <el-select v-model="logFilter.type" placeholder="类型" clearable size="small" style="width: 120px; margin-right: 10px">
              <el-option label="推荐" value="recommend" />
              <el-option label="生成" value="generate" />
            </el-select>
            <el-button size="small" @click="loadLogs">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="logs" stripe max-height="400">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 'recommend' ? 'success' : 'primary'" size="small">
              {{ row.type === 'recommend' ? '推荐' : '生成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="模型" width="100" />
        <el-table-column prop="durationMs" label="耗时" width="80">
          <template #default="{ row }">{{ row.durationMs }}ms</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewLogDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="logFilter.pageNum"
          :page-size="logFilter.pageSize"
          :total="logTotal"
          layout="total, prev, pager, next"
          @current-change="loadLogs"
        />
      </div>
    </el-card>

    <!-- 日志详情弹窗 -->
    <el-dialog v-model="logDetailVisible" title="日志详情" width="70%">
      <div v-if="currentLog">
        <h4>请求Prompt:</h4>
        <pre class="log-content">{{ currentLog.prompt }}</pre>
        <h4>AI响应:</h4>
        <pre class="log-content">{{ currentLog.response }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { aiApi, aiConfigApi } from '@/api'

const activeTab = ref('recommend')
const recommendLoading = ref(false)
const generateLoading = ref(false)
const aiResponse = ref(null)
const saving = ref(false)
const testingQwen = ref(false)
const testingOpenai = ref(false)
const logDetailVisible = ref(false)
const currentLog = ref(null)

const stats = reactive({
  totalCalls: 0,
  todayCalls: 0,
  recommendCalls: 0,
  generateCalls: 0,
  avgDurationMs: 0
})

const aiConfig = reactive({
  defaultProvider: 'qwen',
  qwen: {
    enabled: true,
    apiKey: '',
    baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    model: 'qwen-turbo',
    timeout: 60
  },
  openai: {
    enabled: true,
    apiKey: '',
    baseUrl: 'https://api.openai.com/v1',
    model: 'gpt-3.5-turbo',
    timeout: 60
  }
})

const recommendForm = reactive({
  grade: '初中',
  interests: ['历史']
})

const generateForm = reactive({
  theme: '历史文化',
  days: 2,
  budget: 500,
  startCity: '广州',
  grade: '初中',
  peopleCount: 30
})

const logFilter = reactive({
  pageNum: 1,
  pageSize: 10,
  type: ''
})
const logs = ref([])
const logTotal = ref(0)

// 加载配置
const loadConfig = async () => {
  try {
    const res = await aiConfigApi.getConfig()
    if (res.data) {
      Object.assign(aiConfig, res.data)
    }
  } catch (e) {
    console.error('加载配置失败', e)
  }
}

// 保存配置
const saveConfig = async () => {
  saving.value = true
  try {
    await aiConfigApi.saveConfig(aiConfig)
    ElMessage.success('配置已保存')
  } catch (e) {
    ElMessage.error('保存失败: ' + e.message)
  }
  saving.value = false
}

// 测试连接
const testConnection = async (provider) => {
  if (provider === 'qwen') {
    testingQwen.value = true
  } else {
    testingOpenai.value = true
  }

  try {
    const res = await aiConfigApi.testConnection(provider)
    if (res.data?.success) {
      ElMessage.success(`${provider}连接成功! 响应: ${res.data.response} (${res.data.durationMs}ms)`)
    } else {
      ElMessage.error(`连接失败: ${res.data?.message}`)
    }
  } catch (e) {
    ElMessage.error('测试失败: ' + e.message)
  }

  testingQwen.value = false
  testingOpenai.value = false
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await aiConfigApi.getStats()
    if (res.data) {
      Object.assign(stats, res.data)
    }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

// 加载日志
const loadLogs = async () => {
  try {
    const params = {
      pageNum: logFilter.pageNum,
      pageSize: logFilter.pageSize
    }
    if (logFilter.type) {
      params.type = logFilter.type
    }
    const res = await aiConfigApi.getLogs(params)
    if (res.data) {
      logs.value = res.data.records || []
      logTotal.value = res.data.total || 0
    }
  } catch (e) {
    console.error('加载日志失败', e)
  }
}

// 查看日志详情
const viewLogDetail = (log) => {
  currentLog.value = log
  logDetailVisible.value = true
}

const testRecommend = async () => {
  recommendLoading.value = true
  aiResponse.value = null
  try {
    const res = await aiApi.recommend({
      userProfile: {
        grade: recommendForm.grade,
        interests: recommendForm.interests,
        budget: '中等',
        preferredDays: 2
      },
      context: '寒假研学'
    })
    aiResponse.value = res.data
    ElMessage.success('推荐完成')
    loadStats()
    loadLogs()
  } catch (e) {
    ElMessage.error('推荐失败: ' + e.message)
  }
  recommendLoading.value = false
}

const testGenerate = async () => {
  generateLoading.value = true
  aiResponse.value = null
  try {
    const res = await aiApi.generateRoute(generateForm)
    aiResponse.value = res.data
    ElMessage.success('生成完成')
    loadStats()
    loadLogs()
  } catch (e) {
    ElMessage.error('生成失败: ' + e.message)
  }
  generateLoading.value = false
}

onMounted(() => {
  loadConfig()
  loadStats()
  loadLogs()
})
</script>

<style scoped>
.ai-config-page {
  padding: 10px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 10px 0;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 8px;
}

.config-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.test-card {
  margin-bottom: 20px;
}

.log-card {
  margin-bottom: 20px;
}

.ai-response {
  margin-top: 20px;
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
}

.ai-response pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  max-height: 400px;
  overflow: auto;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.log-content {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  max-height: 300px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
}

/* ================== 移动端响应式 ================== */
@media (max-width: 768px) {
  .ai-config-page {
    padding: 0;
  }
  
  .glass-card {
    padding: 16px;
    border-radius: 12px;
    margin-bottom: 12px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .config-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  :deep(.el-form-item__label) {
    width: 100px !important;
    font-size: 13px;
  }
  
  :deep(.el-tabs__nav) {
    flex-wrap: wrap;
  }
  
  :deep(.el-tabs__item) {
    font-size: 13px;
    padding: 0 12px;
  }
  
  /* 表格移动端 */
  .log-card {
    overflow-x: auto;
  }
  
  :deep(.el-table) {
    min-width: 600px;
  }
  
  .pagination {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .ai-response pre {
    font-size: 12px;
    max-height: 250px;
  }
  
  .log-content {
    font-size: 11px;
    max-height: 200px;
  }
}
</style>
