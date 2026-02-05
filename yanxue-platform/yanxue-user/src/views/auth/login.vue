<template>
  <div class="login-page">
    <!-- 背景动画 -->
    <div class="bg-animation">
      <div class="bubble b1"></div>
      <div class="bubble b2"></div>
      <div class="bubble b3"></div>
      <div class="bubble b4"></div>
      <div class="ring r1"></div>
      <div class="ring r2"></div>
    </div>
    
    <div class="login-container">
      <!-- Logo -->
      <div class="brand">
        <div class="logo">
          <el-icon :size="32"><Compass /></el-icon>
        </div>
        <h1>研学旅行</h1>
        <p>探索世界，发现美好</p>
      </div>
      
      <!-- 登录表单 -->
      <div class="glass-form">
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="account">
            <div class="input-wrap">
              <input 
                v-model="form.account" 
                type="text"
                placeholder="用户名/手机号"
              >
            </div>
          </el-form-item>
          
          <el-form-item prop="password">
            <div class="input-wrap">
              <input 
                v-model="form.password" 
                type="password"
                placeholder="密码"
                @keyup.enter="handleLogin"
              >
            </div>
          </el-form-item>
          
          <el-form-item>
            <button 
              type="button"
              class="submit-btn"
              :disabled="loading"
              @click="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登录中...</span>
            </button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 底部链接 -->
      <div class="footer-links">
        <p>还没有账号？ <router-link to="/register">立即注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Compass } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ account: '', password: '' })
const rules = {
  account: [{ required: true, message: '请输入用户名/手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form.account, form.password)
    ElMessage.success({ message: '登录成功', duration: 2000 })

    const redirect = route.query.redirect
    const safeRedirect =
      typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//')
        ? redirect
        : '/'

    router.push(safeRedirect)
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, #60a5fa 0%, #a78bfa 50%, #c084fc 100%);
}

/* Background Animation */
.bg-animation {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bubble {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.b1 {
  width: 100px;
  height: 100px;
  top: 10%;
  left: 10%;
  animation: float 8s ease-in-out infinite;
}

.b2 {
  width: 150px;
  height: 150px;
  top: 60%;
  left: 5%;
  animation: float 10s ease-in-out infinite 1s;
}

.b3 {
  width: 80px;
  height: 80px;
  top: 20%;
  right: 10%;
  animation: float 7s ease-in-out infinite 0.5s;
}

.b4 {
  width: 120px;
  height: 120px;
  bottom: 20%;
  right: 5%;
  animation: float 9s ease-in-out infinite 2s;
}

.ring {
  position: absolute;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.r1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
  animation: pulse 15s ease-in-out infinite;
}

.r2 {
  width: 400px;
  height: 400px;
  bottom: -150px;
  left: -150px;
  animation: pulse 20s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

/* Container */
.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 360px;
  padding: 20px;
}

/* Brand */
.brand {
  text-align: center;
  margin-bottom: 40px;
  
  .logo {
    width: 64px;
    height: 64px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    color: #6366f1;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  }
  
  h1 {
    font-size: 28px;
    font-weight: 700;
    color: white;
    margin: 0 0 8px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  p {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
    margin: 0;
  }
}

/* Glass Form */
.glass-form {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 24px;
  padding: 32px 24px;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

:deep(.el-form-item) {
  margin-bottom: 20px;
  
  &:last-child {
    margin-bottom: 0;
    margin-top: 24px;
  }
}

.input-wrap {
  input {
    width: 100%;
    height: 52px;
    padding: 0 20px;
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 50px;
    background: rgba(255, 255, 255, 0.15);
    color: white;
    font-size: 15px;
    outline: none;
    transition: all 0.3s ease;
    
    &::placeholder {
      color: rgba(255, 255, 255, 0.6);
    }
    
    &:focus {
      background: rgba(255, 255, 255, 0.25);
      border-color: rgba(255, 255, 255, 0.5);
      box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.1);
    }
  }
}

/* Submit Button */
.submit-btn {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 50px;
  background: linear-gradient(90deg, #3b82f6 0%, #8b5cf6 100%);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(139, 92, 246, 0.4);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(139, 92, 246, 0.5);
  }
  
  &:active {
    transform: translateY(0);
  }
  
  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

/* Footer Links */
.footer-links {
  text-align: center;
  margin-top: 24px;
  
  p {
    color: rgba(255, 255, 255, 0.8);
    font-size: 14px;
    margin: 0;
  }
  
  a {
    color: white;
    font-weight: 600;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

/* Responsive */
@media (max-width: 480px) {
  .login-container {
    padding: 16px;
  }
  
  .brand {
    margin-bottom: 32px;
    
    h1 {
      font-size: 24px;
    }
  }
  
  .glass-form {
    padding: 24px 20px;
    border-radius: 20px;
  }
}
</style>
