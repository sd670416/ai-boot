<template>
  <div class="page-header">
    <div>
      <h2>购物车</h2>
      <p>勾选商品后可直接创建订单，未勾选商品会继续保留。</p>
    </div>
    <n-space>
      <n-button @click="loadData">刷新</n-button>
      <n-button type="warning" secondary @click="clearCart">清空购物车</n-button>
    </n-space>
  </div>

  <n-card class="page-card" title="购物车明细">
    <n-data-table :columns="columns" :data="cartRows" :bordered="false" />
  </n-card>

  <n-card class="page-card" title="结算信息">
    <div class="metric-grid" style="margin-bottom: 16px;">
      <div class="metric-card">
        <div class="label">已勾选商品数</div>
        <div class="value">{{ summary.checkedCount }}</div>
      </div>
      <div class="metric-card">
        <div class="label">购买总件数</div>
        <div class="value">{{ summary.totalQuantity }}</div>
      </div>
      <div class="metric-card">
        <div class="label">结算总金额</div>
        <div class="value">￥{{ Number(summary.totalAmount || 0).toFixed(2) }}</div>
      </div>
    </div>

    <n-form label-placement="top">
      <div class="form-grid">
        <n-form-item label="收货地址">
          <n-select v-model:value="checkout.addressId" :options="addressOptions" placeholder="请选择收货地址" />
        </n-form-item>
        <n-form-item label="下单备注">
          <n-input v-model:value="checkout.remark" placeholder="如：工作日白天送达" />
        </n-form-item>
      </div>
      <n-button type="primary" size="large" :disabled="summary.checkedCount === 0" @click="createOrderFromCart">提交订单</n-button>
    </n-form>
  </n-card>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref, reactive } from 'vue'
import {
  NButton,
  NCard,
  NCheckbox,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NPopconfirm,
  NSelect,
  NSpace,
  useMessage,
  type DataTableColumns,
} from 'naive-ui'
import { api } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { Address, CartItem, Product } from '@/types'

const auth = useAuthStore()
const message = useMessage()
const cartItems = ref<CartItem[]>([])
const products = ref<Product[]>([])
const addresses = ref<Address[]>([])
const summary = reactive({ items: [] as CartItem[], totalAmount: 0, totalQuantity: 0, checkedCount: 0 })
const checkout = reactive<{ addressId?: number; remark?: string }>({})

const productMap = computed(() => new Map(products.value.map((item) => [item.id, item])))
const cartRows = computed(() => cartItems.value.map((item) => ({ ...item, product: productMap.value.get(item.productId) })))
const addressOptions = computed(() =>
  addresses.value.map((item) => ({
    label: `${item.receiverName} / ${item.receiverPhone} / ${item.province}${item.city}${item.district || ''}${item.detailAddress}`,
    value: item.id,
  })),
)

const columns: DataTableColumns<CartItem & { product?: Product }> = [
  {
    title: '勾选',
    key: 'checked',
    render: (row) =>
      h(NCheckbox, {
        checked: row.checked === 1,
        'onUpdate:checked': (value: boolean) => updateChecked(row.id, value ? 1 : 0),
      }),
  },
  {
    title: '商品',
    key: 'product',
    render: (row) => `${row.product?.name || '未知商品'}${row.product?.subtitle ? ` / ${row.product.subtitle}` : ''}`,
  },
  {
    title: '单价',
    key: 'price',
    render: (row) => `￥${row.product?.price ?? 0}`,
  },
  {
    title: '数量',
    key: 'quantity',
    render: (row) =>
      h(NInputNumber, {
        value: row.quantity,
        min: 1,
        style: 'width: 110px',
        'onUpdate:value': (value: number | null) => value && updateQuantity(row.id, value),
      }),
  },
  {
    title: '小计',
    key: 'subtotal',
    render: (row) => `￥${((row.product?.price ?? 0) * row.quantity).toFixed(2)}`,
  },
  {
    title: '操作',
    key: 'actions',
    render: (row) =>
      h(
        NPopconfirm,
        { onPositiveClick: () => removeItem(row.id) },
        {
          trigger: () => h(NButton, { size: 'small', type: 'error', secondary: true }, { default: () => '删除' }),
          default: () => '确认删除该商品？',
        },
      ),
  },
]

onMounted(loadData)

async function loadData() {
  if (!auth.user) return
  try {
    const [cart, cartSummary, productPage, addressList] = await Promise.all([
      api.listCart(auth.user.id),
      api.cartSummary(auth.user.id),
      api.listProducts({ pageNum: 1, pageSize: 200 }),
      api.listAddresses(auth.user.id),
    ])
    cartItems.value = cart
    summary.items = cartSummary.items
    summary.totalAmount = cartSummary.totalAmount
    summary.totalQuantity = cartSummary.totalQuantity
    summary.checkedCount = cartSummary.checkedCount
    products.value = productPage.records
    addresses.value = addressList
    if (!checkout.addressId) {
      checkout.addressId = addresses.value.find((item) => item.isDefault === 1)?.id || addresses.value[0]?.id
    }
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function updateQuantity(id: number, quantity: number) {
  if (!auth.user) return
  try {
    await api.updateCartQuantity(id, auth.user.id, quantity)
    await loadData()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function updateChecked(id: number, checked: number) {
  if (!auth.user) return
  try {
    await api.updateCartChecked(id, auth.user.id, checked)
    await loadData()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function removeItem(id: number) {
  if (!auth.user) return
  try {
    await api.deleteCart(id, auth.user.id)
    message.success('商品已移除')
    await loadData()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function clearCart() {
  if (!auth.user) return
  try {
    await api.clearCart(auth.user.id)
    message.success('购物车已清空')
    await loadData()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function createOrderFromCart() {
  if (!auth.user) return
  const selected = cartRows.value.filter((item) => item.checked === 1)
  if (!selected.length) {
    message.warning('请至少勾选一个商品')
    return
  }
  if (!checkout.addressId) {
    message.warning('请先选择收货地址')
    return
  }
  try {
    const orderResult = await api.createOrder({
      userId: auth.user.id,
      addressId: checkout.addressId,
      remark: checkout.remark,
      items: selected.map((item) => ({ productId: item.productId, quantity: item.quantity })),
    })
    for (const item of selected) {
      await api.deleteCart(item.id, auth.user.id)
    }
    message.success(`订单创建成功，订单号：${orderResult.order.orderNo}`)
    checkout.remark = ''
    await loadData()
  } catch (error) {
    message.error((error as Error).message)
  }
}
</script>
