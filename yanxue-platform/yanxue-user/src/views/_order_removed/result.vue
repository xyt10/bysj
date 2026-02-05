<template>
  <div class="order-result-page">
    <div class="result-card">
      <!-- 成功状态 -->
      <template v-if="status === 'success'">
        <div class="result-icon success">
          <el-icon :size="64"><CircleCheckFilled /></el-icon>
        </div>
        <h2 class="result-title">支付成功</h2>
        <p class="result-desc">您的订单已提交成功，我们将尽快与您联系确认行程</p>
        <div class="order-info">
          <div class="info-item">
            <span class="label">订单号：</span>
            <span class="value">{{ orderId }}</span>
          </div>
        </div>
      </template>

      <!-- 失败状态 -->
      <template v-else>
        <div class="result-icon fail">
          <el-icon :size="64"><CircleCloseFilled /></el-icon>
        </div>
        <h2 class="result-title">支付失败</h2>
        <p class="result-desc">支付过程中出现问题，请稍后重试</p>
      </template>

      <div class="result-actions">
        <el-button type="primary" size="large" @click="goToOrders">
          查看订单
        </el-button>
        <el-button size="large" @click="goToHome">
          返回首页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const orderId = computed(() => route.params.orderId || '')
const status = computed(() => route.query.status || 'success')

const goToOrders = () => {
  router.push('/user/orders')
}

const goToHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.order-result-page {
  padding: 60px 20px;
  display: flex;
  justify-content: center;
}

.result-card {
  background: rgba(255, 255, 255, 0.86);
  border-radius: 20px;
  padding: 48px;
  text-align: center;
  max-width: 480px;
  width: 100%;
  box-shadow: 0 18px 46px -30px rgba(15, 118, 110, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  animation: resultIn 420ms cubic-bezier(0.2, 0.8, 0.2, 1);
}

@keyframes resultIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-icon {
  margin-bottom: 24px;

  &.success {
    color: #34c759;
  }

  &.fail {
    color: #ff3b30;
  }
}

.result-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px;
}

.result-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 24px;
}

.order-info {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 32px;

  .info-item {
    display: flex;
    justify-content: center;
    gap: 8px;

    .label {
      color: #999;
    }

    .value {
      color: #333;
      font-weight: 500;
    }
  }
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
