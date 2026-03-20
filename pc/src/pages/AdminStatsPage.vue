<template>
  <div class="page-header">
    <div>
      <h2>运营统计</h2>
      <p>展示订单总览和热销商品排行。</p>
    </div>
    <n-button @click="loadData">刷新</n-button>
  </div>

  <div class="metric-grid">
    <div class="metric-card">
      <div class="label">订单总数</div>
      <div class="value">{{ overview.totalOrders ?? 0 }}</div>
    </div>
    <div class="metric-card">
      <div class="label">已支付订单</div>
      <div class="value">{{ overview.paidOrders ?? 0 }}</div>
    </div>
    <div class="metric-card">
      <div class="label">待支付订单</div>
      <div class="value">{{ overview.pendingPaymentOrders ?? 0 }}</div>
    </div>
    <div class="metric-card">
      <div class="label">退款订单</div>
      <div class="value">{{ overview.refundOrders ?? 0 }}</div>
    </div>
    <div class="metric-card">
      <div class="label">累计销售额</div>
      <div class="value">￥{{ Number(overview.totalSales ?? 0).toFixed(2) }}</div>
    </div>
  </div>

  <n-card class="page-card" title="热销商品 Top 10" style="margin-top: 16px;">
    <n-data-table :columns="columns" :data="topProducts" :bordered="false" />
  </n-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { NButton, NCard, NDataTable, useMessage, type DataTableColumns } from 'naive-ui'
import { api } from '@/api'

const message = useMessage()
const overview = ref<Record<string, number>>({})
const topProducts = ref<Array<{ productId: number; productName: string; salesQuantity: number; salesAmount: number }>>([])

const columns: DataTableColumns<{ productId: number; productName: string; salesQuantity: number; salesAmount: number }> = [
  { title: '商品ID', key: 'productId' },
  { title: '商品名称', key: 'productName' },
  { title: '销量', key: 'salesQuantity' },
  { title: '销售额', key: 'salesAmount', render: (row) => `￥${Number(row.salesAmount).toFixed(2)}` },
]

onMounted(loadData)

async function loadData() {
  try {
    overview.value = await api.getOverview()
    topProducts.value = await api.getTopProducts()
  } catch (error) {
    message.error((error as Error).message)
  }
}
</script>
