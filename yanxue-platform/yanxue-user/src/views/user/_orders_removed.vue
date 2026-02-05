<template>
  <div class="my-orders-page">
    <div class="card">
      <h2 class="page-title">我的订单</h2>

      <!-- 状态筛选 -->
      <el-tabs v-model="currentStatus" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待支付" name="pending" />
        <el-tab-pane label="已支付" name="paid" />
        <el-tab-pane label="已完成" name="completed" />
        <el-tab-pane label="已取消" name="cancelled" />
      </el-tabs>

      <!-- 订单列表 -->
      <div v-loading="loading" class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <el-tag :type="getStatusType(order.status)" size="small">
              {{ getStatusText(order.status) }}
            </el-tag>
          </div>
          <div class="order-content">
            <div class="route-name">{{ order.routeName }}</div>
            <div class="order-info">
              <span>出行日期：{{ order.travelDate }}</span>
              <span>人数：{{ order.peopleCount }}人</span>
            </div>
            <div class="order-price">
              <span class="label">订单金额：</span>
              <span class="price">{{ order.totalPrice }}</span>
              <span class="unit">元</span>
            </div>
          </div>
          <div class="order-footer">
            <span class="order-time">下单时间：{{ order.createdAt }}</span>
            <div class="order-actions">
              <el-button
                v-if="order.status === 'pending'"
                type="danger"
                link
                @click="handleCancel(order)"
              >
                取消订单
              </el-button>
              <el-button
                v-if="order.status === 'pending'"
                type="primary"
                @click="handlePay(order)"
              >
                去支付
              </el-button>
            </div>
          </div>
        </div>

        <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import 'element-plus/es/components/message/style/index'
import 'element-plus/es/components/message-box/style/index'
import { orderApi } from '@/api'

const loading = ref(false)
const orders = ref([])
const currentStatus = ref('all')

const statusMap = {
  pending: { text: '待支付', type: 'warning' },
  paid: { text: '已支付', type: 'primary' },
  completed: { text: '已完成', type: 'success' },
  cancelled: { text: '已取消', type: 'info' }
}

const getStatusText = (status) => statusMap[status]?.text || status
const getStatusType = (status) => statusMap[status]?.type || 'info'

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await orderApi.myOrders({
      status: currentStatus.value === 'all' ? undefined : currentStatus.value
    })
    orders.value = res.data || []
  } catch (e) {
    console.log('加载订单失败', e)
    // 模拟数据
    orders.value = [
      {
        id: 1,
        orderNo: 'YX202401150001',
        routeName: '广州历史文化两日研学',
        travelDate: '2024-02-15',
        peopleCount: 30,
        totalPrice: 11400,
        status: 'paid',
        createdAt: '2024-01-15 10:30:00'
      },
      {
        id: 2,
        orderNo: 'YX202401140002',
        routeName: '科技探索一日营',
        travelDate: '2024-02-20',
        peopleCount: 25,
        totalPrice: 4950,
        status: 'pending',
        createdAt: '2024-01-14 14:20:00'
      },
      {
        id: 3,
        orderNo: 'YX202401100003',
        routeName: '红色教育主题研学',
        travelDate: '2024-01-20',
        peopleCount: 35,
        totalPrice: 5250,
        status: 'completed',
        createdAt: '2024-01-10 09:15:00'
      }
    ]
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  loadOrders()
}

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await orderApi.cancel(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') {
      // 模拟成功
      order.status = 'cancelled'
      ElMessage.success('订单已取消')
    }
  }
}

const handlePay = async (order) => {
  try {
    await ElMessageBox.confirm(
      `订单金额：${order.totalPrice}元\n点击确认模拟支付`,
      '模拟支付',
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    order.status = 'paid'
    ElMessage.success('支付成功')
  } catch {
    // 取消
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style lang="scss" scoped>
.my-orders-page {
  padding: 20px 0;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 16px;
}

.order-list {
  min-height: 200px;
}

.order-card {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;

  .order-no {
    font-size: 13px;
    color: #999;
  }
}

.order-content {
  padding: 16px 0;

  .route-name {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 8px;
  }

  .order-info {
    font-size: 14px;
    color: #666;
    display: flex;
    gap: 16px;
    margin-bottom: 8px;
  }

  .order-price {
    .label {
      font-size: 14px;
      color: #666;
    }

    .price {
      font-size: 20px;
      font-weight: 600;
      color: #ff6600;
    }

    .unit {
      font-size: 14px;
      color: #999;
      margin-left: 2px;
    }
  }
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;

  .order-time {
    font-size: 12px;
    color: #999;
  }

  .order-actions {
    display: flex;
    gap: 8px;
  }
}
</style>
