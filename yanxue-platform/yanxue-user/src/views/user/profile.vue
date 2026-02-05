<template>
  <div class="profile-edit-page">
    <div class="page-header">
      <div class="title">个人资料</div>
      <div class="subtitle">完善信息后，我们会给你更匹配的研学路线</div>
    </div>

    <div class="card">
      <div class="avatar-line">
        <el-avatar :size="68" :src="userStore.userInfo?.avatar || '/placeholder-avatar.svg'">
          <el-icon :size="28"><User /></el-icon>
        </el-avatar>
        <div class="avatar-meta">
          <div class="name">{{ userStore.nickname || '游客用户' }}</div>
          <div class="hint">头像暂不支持上传</div>
        </div>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        label-position="top"
        class="profile-form"
      >
        <el-form-item label="年级" prop="grade">
          <el-select v-model="form.grade" placeholder="请选择" clearable>
            <el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>

        <el-form-item label="学校" prop="school">
          <el-input v-model="form.school" placeholder="请输入学校名称" clearable />
        </el-form-item>

        <el-form-item label="兴趣方向" prop="interests">
          <el-input
            v-model="form.interests"
            type="textarea"
            :rows="3"
            placeholder="例如：历史、自然、科技、红色教育…（可用逗号分隔）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <div class="actions">
          <el-button class="btn" @click="reset">重置</el-button>
          <el-button class="btn primary" type="primary" :loading="saving" @click="save">保存</el-button>
        </div>
      </el-form>
    </div>

    <div class="tip">
      备注：资料仅用于推荐路线展示；后续可以再完善。
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const saving = ref(false)
const formRef = ref()

const gradeOptions = [
  '小学一年级', '小学二年级', '小学三年级', '小学四年级', '小学五年级', '小学六年级',
  '初中一年级', '初中二年级', '初中三年级',
  '高中一年级', '高中二年级', '高中三年级',
  '大学',
  '其他'
]

const form = reactive({
  grade: '',
  school: '',
  interests: ''
})

const hydrate = () => {
  form.grade = userStore.userInfo?.grade || ''
  form.school = userStore.userInfo?.school || ''
  form.interests = userStore.userInfo?.interests || ''
}

const reset = () => {
  hydrate()
  ElMessage.info('已重置')
}

const save = async () => {
  if (!userStore.userInfo?.id) {
    ElMessage.error('用户信息异常，请重新登录')
    router.push('/login')
    return
  }

  try {
    saving.value = true
    await userApi.updateProfile(userStore.userInfo.id, {
      grade: form.grade || null,
      school: form.school || null,
      interests: form.interests || null
    })

    await userStore.fetchUserInfo()
    ElMessage.success('保存成功')
    router.back()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  hydrate()
})
</script>

<style scoped lang="scss">
.profile-edit-page {
  padding: 18px 16px 40px;
  max-width: 600px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 14px;
}

.title {
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
}

.subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #64748b;
}

.card {
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  border: 1px solid #eef2ff;
  border-radius: 18px;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.06);
  padding: 16px;
}

.avatar-line {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 2px 12px;
  border-bottom: 1px dashed #e2e8f0;
  margin-bottom: 12px;
}

.avatar-meta .name {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.avatar-meta .hint {
  margin-top: 4px;
  font-size: 12px;
  color: #94a3b8;
}

.profile-form {
  margin-top: 12px;
}

.actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 10px;
}

.btn {
  border-radius: 12px;
  height: 42px;
}

.btn.primary {
  box-shadow: 0 10px 22px rgba(99, 102, 241, 0.18);
}

.tip {
  margin-top: 14px;
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
}
</style>
