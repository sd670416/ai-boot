<template>
  <div class="page-header">
    <div>
      <h2>订单中心</h2>
      <p>统一处理支付、取消、发货、收货、退款，以及管理员代查用户订单。</p>
    </div>
    <div class="toolbar">
      <n-input v-if="auth.isAdmin" v-model:value="adminUserIdText" placeholder="管理员可输入用户ID查看订单" style="width: 220px" />
      <n-select v-model:value="status" :options="statusOptions" clearable placeholder="订单状态" style="width: 180px" />
      <n-button type="primary" @click="loadOrders">查询订单</n-button>
    </div>
  </div>

  <n-card class="page-card" title="订单列表">
    <n-data-table :columns="columns" :data="orders" :bordered="false" />
  </n-card>

  <n-drawer v-model:show="showDetail" width="720">
    <n-drawer-content title="订单详情">
      <n-space vertical v-if="currentDetail">
        <n-descriptions bordered label-placement="left" :column="1">
          <n-descriptions-item label="订单号">{{ currentDetail.order.orderNo }}</n-descriptions-item>
          <n-descriptions-item label="订单状态">
            <order-status-tag :type="currentDetail.order.status" />
          </n-descriptions-item>
          <n-descriptions-item label="收货信息">
            {{ currentDetail.order.receiverName }} / {{ currentDetail.order.receiverPhone }} / {{ currentDetail.order.receiverAddress }}
          </n-descriptions-item>
          <n-descriptions-item label="备注">{{ currentDetail.order.remark || '-' }}</n-descriptions-item>
          <n-descriptions-item label="物流信息">
            {{ currentDetail.order.deliveryCompany || '-' }} / {{ currentDetail.order.deliveryNo || '-' }}
          </n-descriptions-item>
        </n-descriptions>

        <n-data-table :columns="detailColumns" :data="currentDetail.items" :bordered="false" />

        <n-card title="支付信息" size="small">
          <div v-if="currentPayment">
            <div>支付单号：{{ currentPayment.payNo }}</div>
            <div>支付方式：{{ currentPayment.payType }}</div>
            <div>支付状态：<order-status-tag :type="currentPayment.status" /></div>
            <div>支付金额：￥{{ currentPayment.payAmount }}</div>
            <n-button v-if="auth.isAdmin && currentPayment.status === 'WAITING'" style="margin-top: 12px" @click="confirmPayment(currentPayment.id)">
              模拟支付成功
            </n-button>
          </div>
          <div v-else>暂无支付单</div>
        </n-card>
      </n-space>
    </n-drawer-content>
  </n-drawer>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NButton,
  NCard,
  NDataTable,
  NDescriptions,
  NDescriptionsItem,
  NDrawer,
  NDrawerContent,
  NInput,
  NPopconfirm,
  NSelect,
  NSpace,
  useMessage,
  type DataTableColumns,
} from 'naive-ui'
import { api } from '@/api'
import OrderStatusTag from '@/components/OrderStatusTag.vue'
import { useAuthStore } from '@/stores/auth'
import type { Order, OrderItem, Payment } from '@/types'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const message = useMessage()

const orders = ref<Order[]>([])
const showDetail = ref(false)
const currentDetail = ref<{ order: Order; items: OrderItem[] } | null>(null)
const currentPayment = ref<Payment | null>(null)
const status = ref<string | null>(null)
const adminUserIdText = ref<string>('')

const statusOptions = [
  { label: '待支付', value: 'PENDING_PAYMENT' },
  { label: '已支付', value: 'PAID' },
  { label: '已发货', value: 'SHIPPED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '已退款', value: 'REFUNDED' },
]

const currentUserId = computed(() => {
  if (auth.isAdmin && adminUserIdText.value.trim()) {
    const parsed = Number(adminUserIdText.value)
    return Number.isNaN(parsed) ? 0 : parsed
  }
  return auth.user?.id || 0
})

const columns: DataTableColumns<Order> = [
  { title: '订单号', key: 'orderNo' },
  { title: '金额', key: 'totalAmount', render: (row) => `￥${row.totalAmount}` },
  { title: '收货人', key: 'receiverName' },
  { title: '状态', key: 'status', render: (row) => h(OrderStatusTag, { type: row.status }) },
  { title: '创建时间', key: 'createTime' },
  {
    title: '操作',
    key: 'actions',
    render: (row) => h(NSpace, { wrapItem: true }, {
      default: () => {
        const actions = [
          h(NButton, { size: 'small', onClick: () => showOrderDetail(row) }, { default: () => '详情' }),
        ]

        if (row.status === 'PENDING_PAYMENT') {
          actions.push(h(NButton, { size: 'small', type: 'primary', onClick: () => createPayment(row) }, { default: () => '创建支付单' }))
          actions.push(
            h(
              NPopconfirm,
              { onPositiveClick: () => cancelOrder(row) },
              {
                trigger: () => h(NButton, { size: 'small', type: 'warning', secondary: true }, { default: () => '取消订单' }),
                default: () => '确认取消订单？',
              },
            ),
          )
        }

        if (row.status === 'PAID' && auth.isAdmin) {
          actions.push(h(NButton, { size: 'small', type: 'primary', secondary: true, onClick: () => deliverOrder(row) }, { default: () => '发货' }))
        }

        if (row.status === 'SHIPPED') {
          actions.push(h(NButton, { size: 'small', type: 'success', secondary: true, onClick: () => receiveOrder(row) }, { default: () => '确认收货' }))
        }

        if (['PAID', 'SHIPPED', 'COMPLETED'].includes(row.status)) {
          actions.push(h(NButton, { size: 'small', type: 'error', secondary: true, onClick: () => refundOrder(row) }, { default: () => '退款' }))
        }

        return actions
      },
    }),
  },
]

const detailColumns: DataTableColumns<OrderItem> = [
  { title: '商品名称', key: 'productName' },
  { title: '单价', key: 'productPrice', render: (row) => `￥${row.productPrice}` },
  { title: '数量', key: 'quantity' },
  { title: '小计', key: 'totalAmount', render: (row) => `￥${row.totalAmount}` },
]

onMounted(async () => {
  const queryUserId = route.query.userId
  if (auth.isAdmin && queryUserId) {
    adminUserIdText.value = String(queryUserId)
  }
  await loadOrders()
})

async function loadOrders() {
  if (!currentUserId.value) return
  try {
    await router.replace({ query: auth.isAdmin ? { userId: String(currentUserId.value) } : {} })
    orders.value = await api.listOrders(currentUserId.value, status.value || undefined)
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function showOrderDetail(order: Order) {
  try {
    currentDetail.value = await api.orderDetail(order.id, currentUserId.value)
    currentPayment.value = await api.getPayment(order.id, currentUserId.value).catch(() => null)
    showDetail.value = true
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function createPayment(order: Order) {
  try {
    const result = await api.createPayment(order.id, currentUserId.value, 'ALIPAY')
    message.success(`${result.message}，支付单号：${result.payment.payNo}`)
    currentPayment.value = result.payment
    if (auth.isAdmin) {
      await confirmPayment(result.payment.id)
    }
    await loadOrders()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function confirmPayment(paymentId: number) {
  try {
    const tradeNo = `ALI${Date.now()}`
    await api.confirmPayment(paymentId, tradeNo)
    message.success('模拟支付成功')
    await loadOrders()
    if (currentDetail.value) {
      await showOrderDetail(currentDetail.value.order)
    }
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function cancelOrder(order: Order) {
  try {
    await api.cancelOrder(order.id, currentUserId.value, '前端取消订单')
    message.success('订单已取消')
    await loadOrders()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function deliverOrder(order: Order) {
  const deliveryCompany = window.prompt('请输入物流公司', '顺丰速运')
  const deliveryNo = window.prompt('请输入物流单号', `SF${Date.now()}`)
  if (!deliveryCompany || !deliveryNo) return
  try {
    await api.deliverOrder(order.id, { deliveryCompany, deliveryNo })
    message.success('订单已发货')
    await loadOrders()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function receiveOrder(order: Order) {
  try {
    await api.receiveOrder(order.id, currentUserId.value)
    message.success('已确认收货')
    await loadOrders()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function refundOrder(order: Order) {
  const reason = window.prompt('请输入退款原因', '用户申请退款') || '用户申请退款'
  try {
    await api.refundOrder(order.id, currentUserId.value, reason)
    message.success('订单已退款')
    await loadOrders()
  } catch (error) {
    message.error((error as Error).message)
  }
}
</script>
