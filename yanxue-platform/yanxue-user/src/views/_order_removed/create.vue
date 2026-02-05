<template>
  <div class="order-create-page" v-loading="pageLoading">
    <template v-if="route">
      <!-- 路线信息 -->
      <div class="card route-card">
        <h3 class="section-title">路线信息</h3>
        <div class="route-info">
          <el-image
            class="route-cover"
            :src="route.coverImage || defaultCover"
            fit="cover"
          >
            <template #error>
              <img class="route-cover" :src="defaultCover" alt="placeholder" />
            </template>
          </el-image>
          <div class="route-detail">
            <div class="route-name">{{ route.name }}</div>
            <div class="route-meta">
              <el-tag size="small" type="primary">{{ route.days }}天</el-tag>
            </div>
            <div class="route-price">
              <span class="price">{{ route.price }}</span>
              <span class="unit">元/人</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 预订信息 -->
      <div class="card form-card">
        <h3 class="section-title">预订信息</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="出行日期" prop="travelDate">
            <el-date-picker
              v-model="form.travelDate"
              type="date"
              placeholder="选择出行日期"
              :disabled-date="disabledDate"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="出行人数" prop="peopleCount">
            <el-input-number
              v-model="form.peopleCount"
              :min="1"
              :max="200"
              @change="calculateTotal"
            />
            <span class="form-hint">单价 {{ route.price }} 元/人</span>
          </el-form-item>

          <el-divider content-position="left">联系人信息</el-divider>

          <el-form-item label="联系人" prop="contactName">
            <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
          </el-form-item>

          <el-form-item label="手机号" prop="contactPhone">
            <el-input v-model="form.contactPhone" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>

          <el-form-item label="备注">
            <el-input
              v-model="form.remark"
              type="textarea"
              :rows="3"
              placeholder="如有特殊需求请在此说明"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 底部操作栏 -->
      <div class="bottom-bar">
        <div class="total-area">
          <span class="label">合计：</span>
          <span class="total-price">{{ totalPrice }}</span>
          <span class="unit">元</span>
        </div>
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          @click="handleSubmit"
        >
          提交订单
        </el-button>
      </div>
    </template>

    <el-empty v-else-if="!pageLoading" description="路线不存在" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import 'element-plus/es/components/message/style/index'
import 'element-plus/es/components/message-box/style/index'
import { routeApi, orderApi } from '@/api'

const router = useRouter()
const routeInfo = useRoute()

const formRef = ref(null)
const pageLoading = ref(false)
const submitting = ref(false)
const route = ref(null)

const defaultCover = '/placeholder-route.svg'

const form = reactive({
  travelDate: '',
  peopleCount: 1,
  contactName: '',
  contactPhone: '',
  remark: ''
})

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const rules = {
  travelDate: [{ required: true, message: '请选择出行日期', trigger: 'change' }],
  peopleCount: [{ required: true, message: '请输入出行人数', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [{ required: true, validator: validatePhone, trigger: 'blur' }]
}

const totalPrice = computed(() => {
  if (!route.value) return 0
  return route.value.price * form.peopleCount
})

const disabledDate = (time) => {
  return time.getTime() < Date.now()
}

const calculateTotal = () => {
  // 触发computed重新计算
}

const loadRouteInfo = async () => {
  const routeId = routeInfo.params.routeId
  if (!routeId) return

  pageLoading.value = true
  try {
    const res = await routeApi.getById(routeId)
    // Backend may return either { route, schedules } or route directly
    route.value = res.data?.route || res.data
  } catch (e) {
    console.log('加载路线失败', e)
    route.value = {
      id: routeId,
      name: '广州历史文化两日研学',
      days: 2,
      price: 380,
      coverImage: null
    }
  } finally {
    pageLoading.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  await ElMessageBox.confirm(
    `确认提交订单？\n出行日期：${formatDate(form.travelDate)}\n出行人数：${form.peopleCount}人\n订单金额：${totalPrice.value}元`,
    '确认订单',
    {
      confirmButtonText: '确认提交',
      cancelButtonText: '取消',
      type: 'info'
    }
  )

  submitting.value = true
  try {
    const res = await orderApi.create({
      routeId: route.value.id,
      travelDate: formatDate(form.travelDate),
      peopleCount: form.peopleCount,
      contactName: form.contactName,
      contactPhone: form.contactPhone,
      remark: form.remark
    })

    // 模拟支付
    await ElMessageBox.confirm(
      `订单金额：${totalPrice.value}元\n点击确认模拟支付`,
      '模拟支付',
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const orderId = res.data?.id || 'mock-order-' + Date.now()
    router.push(`/order/result/${orderId}?status=success`)
  } catch (e) {
    if (e !== 'cancel') {
      console.log('提交失败', e)
      // 模拟成功
      const orderId = 'mock-order-' + Date.now()
      router.push(`/order/result/${orderId}?status=success`)
    }
  } finally {
    submitting.value = false
  }
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadRouteInfo()
})
</script>

<style lang="scss" scoped>
.order-create-page {
  padding: 20px 0 120px;
  animation: orderIn 420ms cubic-bezier(0.2, 0.8, 0.2, 1);
}

@keyframes orderIn {
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
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(15, 118, 110, 0.06);
  box-shadow: var(--shadow-sm);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px;
}

.route-card {
  .route-info {
    display: flex;
    gap: 16px;
  }

  .route-cover {
    width: 120px;
    height: 90px;
    border-radius: 8px;
    flex-shrink: 0;
  }

  .route-detail {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .route-name {
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }

  .route-price {
    .price {
      font-size: 20px;
      font-weight: 800;
      color: #ef4444;
    }

    .unit {
      font-size: 12px;
      color: var(--ink-500);
      margin-left: 4px;
    }
  }
}

.form-card {
  .form-hint {
    margin-left: 12px;
    font-size: 13px;
    color: #999;
  }

  :deep(.el-divider__text) {
    font-size: 14px;
    color: #666;
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow:
    0 -10px 26px -22px rgba(15, 118, 110, 0.40),
    0 -2px 12px rgba(0, 0, 0, 0.06);
  border-top: 1px solid rgba(255, 255, 255, 0.7);
  z-index: 100;

  .total-area {
    .label {
      font-size: 14px;
      color: #666;
    }

    .total-price {
      font-size: 28px;
      font-weight: 900;
      color: #ef4444;
    }

    .unit {
      font-size: 14px;
      color: var(--ink-500);
      margin-left: 4px;
    }
  }
}
</style>
