<template>
  <div class="login-page">
    <!-- Animated Background Elements -->
    <div class="bg-layer">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="grid-overlay"></div>
    </div>

    <div class="login-container">
      <div class="glass-card">
        <div class="card-left">
          <div class="brand-section">
            <div class="logo-circle">
              <el-icon :size="40"><Compass /></el-icon>
            </div>
            <h1>研学旅行平台</h1>
            <p>探索世界 · 智启未来</p>
          </div>
          <div class="illustration-placeholder">
            <!-- Decorative circles representing routes/spots -->
            <div class="circle c1"></div>
            <div class="circle c2"></div>
            <div class="line l1"></div>
          </div>
        </div>

        <div class="card-right">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p class="sub-text">请登录您的管理员账号</p>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" class="login-form" size="large">
            <el-form-item prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="用户名" 
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="密码" 
                show-password
                class="custom-input"
                @keyup.enter="handleLogin"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-button 
                type="primary" 
                :loading="loading" 
                @click="handleLogin" 
                class="login-btn"
                round
              >
                {{ loading ? '登录中...' : '立即登录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span>© 2024 YanXue Platform</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Compass, User, Lock } from '@element-plus/icons-vue'
import { authApi } from '@/api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true

  try {
    const res = await authApi.login({
      account: form.username,
      password: form.password
    })

    const token = res.data?.token
    if (!token) {
      throw new Error('登录失败：未获取到token')
    }

    localStorage.setItem('admin_token', token)
    ElMessage.success({ message: '登录成功', duration: 2000 })
    router.push('/dashboard')
  } catch (e) {
    ElMessage.error(e.message || '用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #0f172a;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', sans-serif;
}

/* Background Effects */
.bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: float 20s infinite ease-in-out;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #0ea5e9 0%, transparent 70%);
  top: -100px;
  left: -100px;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #14b8a6 0%, transparent 70%);
  bottom: -50px;
  right: -50px;
  animation-delay: -5s;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #6366f1 0%, transparent 70%);
  top: 40%;
  left: 30%;
  opacity: 0.4;
  animation-delay: -10s;
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  mask-image: radial-gradient(circle at center, black 40%, transparent 100%);
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  33% { transform: translate(30px, -50px); }
  66% { transform: translate(-20px, 20px); }
}

/* Glass Card */
.glass-card {
  position: relative;
  z-index: 10;
  display: flex;
  width: 900px;
  max-width: 95vw;
  height: 550px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}

.card-left {
  flex: 1;
  background: rgba(0, 0, 0, 0.2);
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  color: white;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.card-right {
  flex: 1;
  background: rgba(255, 255, 255, 0.85); /* Light background for form */
  padding: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* Left Content */
.brand-section h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 20px 0 10px;
  letter-spacing: 1px;
}
.brand-section p {
  opacity: 0.8;
  font-size: 14px;
}

.logo-circle {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #14b8a6, #0ea5e9);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 16px rgba(14, 184, 166, 0.3);
}

.illustration-placeholder {
  position: relative;
  height: 200px;
}
.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}
.c1 { width: 80px; height: 80px; top: 20%; left: 10%; animation: float 8s infinite; }
.c2 { width: 120px; height: 120px; bottom: 10%; right: 10%; animation: float 10s infinite reverse; }

/* Right Content */
.form-header h2 {
  font-size: 24px;
  color: #1e293b;
  margin: 0 0 8px;
}
.sub-text {
  color: #64748b;
  margin-bottom: 32px;
}

.custom-input :deep(.el-input__wrapper) {
  background-color: #f1f5f9;
  border: 1px solid transparent;
  box-shadow: none;
  border-radius: 12px;
  padding: 12px 15px;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: #0ea5e9;
  box-shadow: 0 0 0 4px rgba(14, 165, 233, 0.1);
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(90deg, #0ea5e9, #14b8a6);
  border: none;
  margin-top: 10px;
  transition: transform 0.2s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -5px rgba(14, 165, 233, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.form-footer {
  margin-top: auto;
  text-align: center;
  font-size: 12px;
  color: #94a3b8;
}

@media (max-width: 900px) {
  .glass-card {
    flex-direction: column;
    width: 400px;
    height: auto;
  }
  .card-left {
    display: none;
  }
  .card-right {
    padding: 40px;
  }
}
</style>
