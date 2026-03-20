<template>
  <n-tag :type="typeMap[type] ?? 'default'" size="small" round>
    {{ textMap[type] ?? type }}
  </n-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NTag } from 'naive-ui'

const props = defineProps<{ type: string }>()

const textMap: Record<string, string> = {
  PENDING_PAYMENT: '待支付',
  PAID: '已支付',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REFUNDED: '已退款',
  REFUND_REQUESTED: '退款中',
  WAITING: '待支付',
  SUCCESS: '支付成功',
}

const typeMap = computed<Record<string, 'warning' | 'success' | 'info' | 'error' | 'default'>>(() => ({
  PENDING_PAYMENT: 'warning',
  PAID: 'info',
  SHIPPED: 'info',
  COMPLETED: 'success',
  CANCELLED: 'default',
  REFUNDED: 'error',
  REFUND_REQUESTED: 'warning',
  WAITING: 'warning',
  SUCCESS: 'success',
}))
</script>
